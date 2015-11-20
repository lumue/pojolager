package io.github.lumue.pojolager;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by lm on 20.11.15.
 */
public interface PojoSerializer<T> {

	T deserialize(InputStream inputStream);

	void serialize(T value, OutputStream outputStream);

}
