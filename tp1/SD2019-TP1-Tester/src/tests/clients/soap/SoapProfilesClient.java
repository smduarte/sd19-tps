package tests.clients.soap;

import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import com.sun.xml.ws.client.BindingProviderProperties;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.api.soap.SoapProfiles;
import utils.Url;

public class SoapProfilesClient extends SoapClient implements Profiles {

	SoapProfiles impl;

	public SoapProfilesClient(URI serverUri) {
		super(serverUri);
	}

	@Override
	public Result<Profile> getProfile(String userId) {
		return super.tryCatchResult(() -> impl().getProfile(userId));
	}

	@Override
	public Result<Void> createProfile(Profile profile) {
		return super.tryCatchVoid(() -> impl().createProfile(profile));
	}

	@Override
	public Result<Void> deleteProfile(String userId) {
		return super.tryCatchVoid(() -> impl().deleteProfile(userId));
	}

	@Override
	public Result<List<Profile>> search(String prefix) {
		return super.tryCatchResult(() -> impl().search(prefix));
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		return super.tryCatchVoid(() -> impl().follow(userId1, userId2, isFollowing));
	}

	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2) {
		return super.tryCatchResult(() -> impl().isFollowing(userId1, userId2));
	}

	synchronized private SoapProfiles impl() {
		if (impl == null) {
			QName QNAME = new QName(SoapProfiles.NAMESPACE, SoapProfiles.NAME);
			Service service = Service.create(Url.from(super.uri + WSDL), QNAME);
			this.impl = service.getPort(microgram.api.soap.SoapProfiles.class);
			((BindingProvider) this.impl).getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, SOAP_RECV_TIMEOUT);
			((BindingProvider) this.impl).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, SOAP_CONN_TIMEOUT);
		}
		return impl;
	}

}
