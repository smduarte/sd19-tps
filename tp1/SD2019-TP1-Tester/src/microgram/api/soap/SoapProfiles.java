package microgram.api.soap;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import microgram.api.Profile;

@WebService(serviceName=SoapProfiles.NAME, targetNamespace=SoapProfiles.NAMESPACE, endpointInterface=SoapProfiles.INTERFACE)
public interface SoapProfiles {
	
	static final String NAME = "profiles";
	static final String NAMESPACE = "http://sd2019";
	static final String INTERFACE = "microgram.api.soap.SoapProfiles";
	
	@WebMethod
	Profile getProfile( String userId ) throws MicrogramException;
		
	@WebMethod
	void createProfile( Profile profile ) throws MicrogramException;

	@WebMethod
	void deleteProfile( String userId ) throws MicrogramException;

	@WebMethod
	List<Profile> search( String prefix ) throws MicrogramException;
	
	@WebMethod
	void follow( String userId1, String userId2, boolean isFollowing) throws MicrogramException;
	
	@WebMethod
	boolean isFollowing( String userId1, String userId2) throws MicrogramException;			
}
