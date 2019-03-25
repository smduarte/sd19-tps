package docker;

public interface ContainerFactory {
	
	String image();
	
	Container createContainer( String name, boolean network );
	
	Container createContainer( String name, String network, String ... aliases);

}