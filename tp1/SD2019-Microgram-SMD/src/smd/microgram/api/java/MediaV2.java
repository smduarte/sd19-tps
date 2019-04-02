package smd.microgram.api.java;

import microgram.api.java.Media;
import microgram.api.java.Result;

public interface MediaV2 extends Media {

	Result<Void> delete(String id);

}
