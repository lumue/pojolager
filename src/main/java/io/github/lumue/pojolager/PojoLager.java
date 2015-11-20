package io.github.lumue.pojolager;

import java.io.IOException;
import java.util.Map;

/**
 * one PojoLager per Java type.
 *
 * Created by lm on 20.11.15.
 */
public interface PojoLager<K,V> extends Map<K,V>,AutoCloseable {

	static <K,V> PojoLager<K,V> create(Class<V> valueType){
		return new  OnePojoOneFileLager();
	};

	void connect(String lagerLocation) throws IOException;
}
