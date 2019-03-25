package tests._3_services;

import static utils.Log.Log;

import docker.Docker;
import tests.BaseTest;

public class Services extends BaseTest {
	
	public static void clean() throws Exception {
		
		String res = Docker.get().findNamedContainer("cassandra1");
		
		if( res.isEmpty() )
			res = Docker.get().findNamedContainer("cassandra2");
		
		if( res.isEmpty() )
			res = Docker.get().findNamedContainer("cassandra3");
			
		if( res.isEmpty() )
			Log.fine("\n\n\nWARNING. Third party services [zookeeper, cassandra & kafka] containers not found...\n\n\n");

		
		Log.fine("Cleaning third-party-services.... [cassandra, kafka & zookeeper...]");

		res = Docker.get().exec("cassandra1", "/clean-all.sh");
		Log.fine(res);
	}	
}
