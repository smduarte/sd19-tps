package tests.microgram.base.posts_profiles;

import static microgram.api.java.Result.ErrorCode.OK;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import loops.Loop;
import microgram.api.Profile;
import tests.TestFailedException;
import tests.microgram.MicrogramTestOperations;

public class d_SearchProfiles extends MicrogramTestOperations {

	public d_SearchProfiles(boolean parallel, int restProfilesServers, int soapProfilesServers, int restPostsServers, int soapPostsServers, String description) {
		super(parallel, false, restProfilesServers, soapProfilesServers, restPostsServers, soapPostsServers, description);
	}

	@Override
	protected void prepare() throws Exception {
		println(String.format("Testing Profiles Service [ search Profiles... ] %s ", description));
		super.prepare();

	}

	@Override
	protected void execute() throws Exception {

		super.generateProfiles(200, false, false);

		Set<String> prefixes = lProfiles.search("").value().stream().map(p -> p.getUserId().substring(0, Math.max(1, random().nextInt(p.getUserId().length())))).collect(Collectors.toSet());

		super.sleep(true);

		Loop.items(prefixes, parallel).forEach((prefix) -> {

			List<Profile> obtained = doOrThrow(() -> anyProfilesClient().search(prefix), OK, "Profiles.search() failed test... Expected %s got: [%s]");

			Set<String> expected = lProfiles.search(prefix).value().stream().map(p -> p.getUserId()).collect(Collectors.toSet());
			Set<String> resulted = obtained.stream().map(p -> p.getUserId()).collect(Collectors.toSet());

			if (!expected.equals(resulted))
				throw new TestFailedException("Profiles.search() failed test...<search did not produce the expected results>, wanted:" + expected + " got: " + resulted);
		});
	}

}
