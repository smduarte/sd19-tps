package docker;

import static utils.Log.Log;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.DockerClient.RemoveContainerParam;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.ExecCreation;

import utils.Sleep;

public class Container {

	private static final int WAIT_BF_KILL = 3;
	
	String id;
	String ip;
	String logs;
	final String name;
	final List<String> aliases;
	final String network; 
	
	public Container(String name) {
		this(name, null);
	}

	public Container(String name, String network, String ... aliases ) {
		this.name = name;
		this.network = network;
		this.aliases = Arrays.asList(aliases);
	}
	
	public String name() {
		return name;
	}

	public String ip() {
		return ip;
	}

//	public Container start(String image) throws Exception {
//		Log.finest(String.format("Starting: %s", image));
//		if(network != null )
//			this.id = Docker.get().run(image, name, network, aliases);
//		else
//			this.id = Docker.get().run(image, command)
//		this.ip = Docker.get().ip(id, network);
//		
//		Log.finest(id + "/" + ip );
//		return this;
//	}
//	
//	public Container start(String image, String cmd) throws Exception {
//		return start( image, cmd.split(" "));
//	}
//	
	public Container start(String image, String cmd) throws Exception {
		
		if( network != null )
			this.id = Docker.get().run(image, name, network, aliases, cmd.split(" "));
		else
			this.id = Docker.get().run(image, cmd.split(" "));
		
		this.ip = Docker.get().ip(id, network);
		Log.fine(String.format("Started: %s : %s", name, cmd));
		Log.finest(id);
		return this;
	}

//	public Container startWithOverride(String image, String cmd) throws Exception {
//		this.id = Docker.get().runViaEntrypoint(image, cmd, name);
//		this.ip = Docker.get().client().inspectContainer(id).networkSettings().ipAddress();
//		Log.fine(String.format("Started: %s [ %s ]", name, cmd));
//		Log.finest(id);
//		return this;
//	}

	
	public void exec(String cmd) throws Exception {
		String[] command = cmd.split(" ");
		
		final ExecCreation execCreation = Docker.get().client().execCreate(id, command,
				DockerClient.ExecCreateParam.attachStdout(), DockerClient.ExecCreateParam.attachStderr());

		final LogStream output = Docker.get().client().execStart(execCreation.id());
		final String execOutput = output.readFully();
		Log.finest(String.format("exec [%s] in %s >>> out:%s", cmd, name, execOutput));
	}

	public Container kill() throws Exception {
		Log.finest(String.format("Stopping: %s", name));
		Docker.get().client().killContainer(id);
		return this;
	}

	public Container remove() throws Exception {
		Log.finest(String.format("Removing: %s", name));
		try (LogStream stream = Docker.get().client().logs(id, LogsParam.stdout(), LogsParam.stderr())) {
			logs = stream.readFully();
		}
		Docker.get().client().removeContainer(id, RemoveContainerParam.forceKill(true));
		return this;
	}

	public void logs( OutputStream os ) throws Exception {
		try (LogStream stream = Docker.get().client().logs(id, LogsParam.stdout(), LogsParam.stderr())) {
			String logs = stream.readFully();
			os.write( logs.getBytes() );
		}
		os.flush();
	}
	
	public String logs() throws Exception {
		if (logs == null )
			remove();
		return logs;
	}
	
	public void networkingFailure( int duration, Integer ... ports ) {
		try {
			for( int port: ports )
				exec(String.format("/sbin/iptables -A INPUT -p tcp --destination-port %s -j DROP", port));
			Sleep.ms(duration);
			exec("/sbin/iptables --flush");			
		} catch( Exception x ) {
		}
	}	
		
	public void dumpLogs( PrintWriter pw) {
		try {
			pw.println("-----------------------------------------------------------------------------------");
			pw.println(name());
			pw.println("-----------------------------------------------------------------------------------");
			pw.println(logs());
		} catch (Exception x) {
		}
	}
	
	public void stop() throws Exception {
		Docker.get().client().stopContainer(id, WAIT_BF_KILL);
	}
	
	public void start() throws Exception {
		Docker.get().client().startContainer(id);
	}
}
