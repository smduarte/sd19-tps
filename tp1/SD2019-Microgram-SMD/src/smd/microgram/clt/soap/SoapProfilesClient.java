package smd.microgram.clt.soap;

import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import microgram.api.Profile;
import microgram.api.java.Result;
import smd.discovery.Discovery;
import smd.microgram.api.java.ProfilesV2;
import smd.microgram.api.soap.SoapProfilesV2;
import smd.microgram.srv.soap.ProfilesSoapServer;
import utils.Url;

public class SoapProfilesClient extends SoapClient implements ProfilesV2 {

	SoapProfilesV2 impl;
	
	public SoapProfilesClient() {
		this( Discovery.findUrisOf( ProfilesSoapServer.SERVICE, 1)[0]);
	}
	
	public SoapProfilesClient(URI serverUri) {
		super(serverUri);
	}

	@Override
	public Result<Profile> getProfile(String userId) {
		return super.tryCatchResult( () -> impl().getProfile(userId) );
	}

	@Override
	public Result<Void> createProfile(Profile profile) {
		return super.tryCatchVoid( () -> impl().createProfile(profile) );
	}
	
	@Override
	public Result<Void> deleteProfile(String userId) {
		return super.tryCatchVoid( () -> impl().deleteProfile(userId) );
	}

	@Override
	public Result<List<Profile>> search(String prefix) {
		return super.tryCatchResult( () -> impl().search(prefix) );
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		return super.tryCatchVoid( () -> impl().follow(userId1, userId2, isFollowing) );
	}

	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2) {
		return super.tryCatchResult( () -> impl().isFollowing(userId1, userId2) );
	}

	@Override
	public Result<Void> updateProfile(Profile profile) {
		return super.tryCatchVoid( () -> impl().updateProfile(profile) );
	}

	@Override
	public Result<List<String>> following(String userId) {
		return super.tryCatchResult( () -> impl().following(userId) );
	}

	
	private SoapProfilesV2 impl() {
		if( impl == null ) {
			QName QNAME = new QName(SoapProfilesV2.NAMESPACE, SoapProfilesV2.NAME);

			Service service = Service.create( Url.from(super.uri + WSDL), QNAME);
			
			this.impl = service.getPort( smd.microgram.api.soap.SoapProfilesV2.class );
		}
		return impl;
	}
}
