package io.github.lumue.pojolager;

import java.io.IOException;
import java.util.Map;

/**
 * one PojoLager per Java type.
 *
 * Created by lm on 20.11.15.
 */
public interface PojoLager<V> extends Map<String,V>,AutoCloseable {

	static <V> PojoLager<V> create(Class<V> valueType){
		return new  OnePojoOneFileLager(new JsonPojoSerializer());
	};

	void connect(String lagerLocation) throws IOException;

	boolean isConnected();
}
