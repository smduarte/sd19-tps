package docker;

import static com.google.common.base.Charsets.UTF_8;
import static utils.Log.Log;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ExecCreateParam;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.DockerClient.RemoveContainerParam;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ExecCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.NetworkConfig;

import utils.IO;

public class Docker {

	static Docker instance;

	final DockerClient docker;

	public static synchronized Docker get() throws Exception {
		return instance == null ? (instance = new Docker()) : instance;
	}
	
	public boolean validImage( String image ) throws Exception {
		List<String> tags = docker.listImages()
		.stream()
		.map( i -> i.repoTags() )
		.filter( i -> i != null )
		.flatMap( i -> i.stream() )
		.collect( Collectors.toList() );
		
		return tags.stream().anyMatch( i -> i.startsWith(image));
	}
	
	public Docker() throws Exception {
		docker = DefaultDockerClient.fromEnv().build();
	}

	public String id() throws Exception {
		return docker.info().id();
	}

	public String ip(String id, String network) throws Exception {
		return docker.listContainers(ListContainersParam.withStatusRunning()).stream()
				.filter(container -> container.id().equals(id)).map(container -> container.networkSettings().networks())
				.flatMap(map -> map.entrySet().stream()).filter(e -> e.getKey().equals(network))
				.map(e -> e.getValue().ipAddress()).findFirst().orElse("localhost");
	}

	DockerClient client() {
		return docker;
	}

	public void createNetwork(String name, String driver) throws Exception {
		if (!docker.listNetworks().stream().filter(n -> n.name().equals(name)).findAny().isPresent()) {
			NetworkConfig config = NetworkConfig.builder().driver(driver).name(name).build();
			docker.createNetwork(config);
		}
	}

	public void copyFile(String image, String from, String to) throws Exception {
		String id = run(image, "/bin/cat", from);
		docker.waitContainer(id);
		final String logs;
		try (LogStream stream = docker.logs(id, LogsParam.stdout(), LogsParam.stderr())) {
			logs = stream.readFully();

			File dst = new File(to);
			dst.getParentFile().mkdirs();
			Files.write(dst.toPath(), logs.getBytes());
		}
		docker.removeContainer(id);
	}

	public void copyBinaryFile(String image, String from, String to) throws Exception {
		String id = run(image, "tail", "-f", "/dev/null");
		InputStream tar = docker.archiveContainer(id, from);
		TarArchiveInputStream tis = new TarArchiveInputStream(tar);
		while (tis.getNextTarEntry() != null) {
			File dst = new File(to);
			dst.getParentFile().mkdirs();
			Files.copy(tis, dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		docker.stopContainer(id, 0);
		docker.removeContainer(id);
	}

	public String run(String image, String... command) throws Exception {

		final HostConfig hostConfig = HostConfig.builder().privileged(true).build();

		final ContainerConfig containerConfig = ContainerConfig.builder().image(image).cmd(command)
				.hostConfig(hostConfig).build();

		final ContainerCreation creation = docker.createContainer(containerConfig);

		docker.startContainer(creation.id());
		return creation.id();
	}

	public String exec(String container, String... command) {

		final StringBuilder res = new StringBuilder();
		try {
			String id = findNamedContainer(container);

			final ExecCreation creation = docker.execCreate(id, command, ExecCreateParam.attachStdout(true),
					ExecCreateParam.attachStderr(true));

			try (final LogStream stream = docker.execStart(creation.id())) { 
				stream.forEachRemaining(lm -> {
					res.append(UTF_8.decode(lm.content()));
				});
			}
		} catch (Exception x) {
			//Log.warning("Suppressing exception...Not good...");
		}
		return res.toString();
	}

	public String run(String image, String name, String network, List<String> aliases, String... command)
			throws Exception {

		this.killNamed(name);

		List<String> cmd = new ArrayList<>();
		Collections.addAll(cmd,
				new String[] { "/usr/local/bin/docker", "run", "--privileged", "-d", "--network=" + network, "--name", name, image });
		;
		Collections.addAll(cmd, command);

		Log.finest("CMD: " + cmd.stream().collect( Collectors.joining(" ")));

		ProcessBuilder pb = new ProcessBuilder(cmd.toArray(new String[cmd.size()]));
		Process process = pb.start();

		String id;
		try (Scanner s = new Scanner(process.getInputStream())) {
			id = s.nextLine();
		} catch (Exception x) {
			x.printStackTrace();
			id = "unknown";
		}
		IO.dumpTo(process.getErrorStream(), System.err);

		process.waitFor();
		process.destroy();
		return id;
	}

	public void killNamed(String ... patterns) throws Exception {

		List<Container> containers = docker.listContainers(ListContainersParam.allContainers(true));
		for (Container c : containers) {
			if( anyNameMatches(c, patterns)){
					try {
						Log.finest("killNamed(): Removing container: " + c.names());
						docker.removeContainer(c.id(), RemoveContainerParam.forceKill(true));
					} catch (Exception x) {
					}
			}
		}
	}
	
	public void killByImage(String image) throws Exception {

		List<Container> containers = docker.listContainers(ListContainersParam.allContainers(true));
		for (Container c : containers) {
			if( c.image().equals( image )){
					try {
						Log.finest("killByImage("+ image +"): Removing container: " + c.names());
						docker.removeContainer(c.id(), RemoveContainerParam.forceKill(true));
					} catch (Exception x) {
					}
			}
		}
	}

	public long countNamedContainers(String prefix) throws Exception {
		int count = 0;
		List<Container> containers = docker.listContainers(ListContainersParam.allContainers(true));
		for (Container c : containers) {
			if (c.names().stream().anyMatch(name -> name.contains(prefix)))
				count++;
		}
		return count;
	}

	public String findNamedContainer(final String name) throws Exception {
		return docker.listContainers(ListContainersParam.withStatusRunning()).stream()
				.filter(c -> c.names().contains("/" + name)).map(c -> c.id()).findFirst().orElse("");
	}
	

	private static boolean anyNameMatches(Container c, String ... patterns ) {
		for( String i : c.names() )
			for( String j : patterns )
				if( i.contains( j ))
					return true;
		return false;
	}
}
