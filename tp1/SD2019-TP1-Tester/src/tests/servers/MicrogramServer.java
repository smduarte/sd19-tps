package tests.servers;

import static utils.Log.Log;

import java.net.URI;

import docker.Container;
import docker.ContainerFactory;
import docker.Docker;
import tests.TestFailedException;

public class MicrogramServer<T> {
	
	protected URI uri;
	protected Container container;
	protected String ServerName;
	
	protected MicrogramServer( String ServerName) {
		this.ServerName = ServerName;
	}
	
	public URI uri() {
		return uri;
	}
	
	public String url() {
		return uri.toString();
	}

	public void start(ContainerFactory factory, String mainClass, String name, String extraArgs) throws Exception {	
		if( mainClass == null || mainClass.isEmpty() )
			throw new TestFailedException(String.format("Cannot execute server: %s [check mainclass value in .props file]", name));
		
		this.container = factory.createContainer( name, true );
		this.container.start(factory.image(), String.format("java -cp /home/sd/* %s %s", mainClass, extraArgs));
	}
	
	public Container container() {
		return container;
	}
	
	@SuppressWarnings("unchecked")
	public T stop() throws Exception {
		container.remove();
		Log.fine(String.format("Stopped: %s...", container.name()));
		return (T)this;
	}

	protected String newName() throws Exception {
		return ServerName + (Docker.get().countNamedContainers(ServerName)+1);
	}
}
