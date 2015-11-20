package io.github.lumue.pojolager;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * serialize and deserialize pojos to and from json
 *
 * Created by lm on 20.11.15.
 */
public class JsonPojoSerializer<T> implements PojoSerializer<T> {

	@Override
	public T deserialize(InputStream inputStream) {
		return null;
	}

	@Override
	public void serialize(T value, OutputStream outputStream) {

	}

}
