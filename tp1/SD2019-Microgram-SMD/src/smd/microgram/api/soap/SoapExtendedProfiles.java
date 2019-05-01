package smd.microgram.api.soap;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import microgram.api.Profile;
import microgram.api.soap.MicrogramException;
import microgram.api.soap.SoapProfiles;

@WebService(serviceName=SoapProfilesV2.NAME, targetNamespace=SoapProfilesV2.NAMESPACE, endpointInterface=SoapProfilesV2.INTERFACE)
public interface SoapProfilesV2 extends SoapProfiles {
	
	static final String INTERFACE = "smd.microgram.api.soap.SoapProfilesV2";
	
	@WebMethod
	void updateProfile( Profile profile ) throws MicrogramException;
	
	@WebMethod
	List<String> following( String userId ) throws MicrogramException;
}
