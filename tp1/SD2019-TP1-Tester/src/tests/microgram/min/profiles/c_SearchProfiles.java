package tests.microgram.min.profiles;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import loops.Loop;
import microgram.api.Profile;
import microgram.api.java.Result;
import tests.TestFailedException;
import tests.microgram.MicrogramTestOperations;

public class c_SearchProfiles extends MicrogramTestOperations {

	public c_SearchProfiles(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Profiles Service [ search Profiles... ] %s ", description));
		super.prepare();

	}

	@Override
	protected void execute() throws Exception {

		super.generateProfiles(250, false, false);

		Set<String> prefixes = lProfiles.search("").value().stream().map(p -> p.getUserId().substring(0, Math.max(1, random().nextInt(p.getUserId().length())))).collect(Collectors.toSet());

		Loop.items(prefixes, parallel).forEach((prefix) -> {

			Result<List<Profile>> expected = lProfiles.search(prefix);
			List<Profile> obtained = doOrThrow(() -> anyProfilesClient().search(prefix), expected.error(), "Profiles.search() failed test... Expected %s got: [%s]");

			if (expected.isOK()) {
				Set<String> s1 = expected.value().stream().map(Profile::getUserId).collect(Collectors.toSet());
				Set<String> s2 = obtained.stream().map(Profile::getUserId).collect(Collectors.toSet());
				if (!s1.equals(s2))
					throw new TestFailedException("Profiles.search() failed test...<search did not produce the expected results>, wanted:" + s1 + " got: " + s2);
			}
		});
	}

}
