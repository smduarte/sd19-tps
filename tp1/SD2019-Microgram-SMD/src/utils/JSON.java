package utils;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

final public class JSON {
	private static final Gson gson = new Gson();

	synchronized public static final String encode(Object obj) {
		return gson.toJson(obj);
	}

	synchronized public static final <T> T decode(String json, Class<T> classOf) {
		return gson.fromJson(json, classOf);
	}

	synchronized public static <T> T decode(String key, Type typeOf) {
		return gson.fromJson(key, typeOf);
	}

	synchronized public static <T> T decode(String key) {
		return gson.fromJson(key, new TypeToken<T>() {
		}.getType());
	}
}
