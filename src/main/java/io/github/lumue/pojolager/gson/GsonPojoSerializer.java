package io.github.lumue.pojolager.gson;

import com.google.gson.Gson;
import io.github.lumue.pojolager.PojoSerializer;

import java.io.*;

/**
 * serialize and deserialize pojos to and from json
 *
 * Created by lm on 20.11.15.
 */
public class GsonPojoSerializer<T> implements PojoSerializer<T> {

	private final Gson gson=new Gson();

	private final Class<T> targetType;

	public GsonPojoSerializer(Class<T> targetType) {
		this.targetType = targetType;
	}

	@Override
	public T deserialize(InputStream inputStream) {
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		T result= gson.fromJson(br,targetType);
		try {
			br.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	@Override
	public void serialize(T value, OutputStream outputStream) {
		BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(outputStream));
		gson.toJson(value,bw);
		try {
			bw.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

}
