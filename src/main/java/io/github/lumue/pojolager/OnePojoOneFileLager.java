package io.github.lumue.pojolager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The default Pojo Lager.
 *  - use a single file in this Lagers directory per object
 *  - use the key as the base filename
 * Created by lm on 20.11.15.
 */
class OnePojoOneFileLager<V> implements PojoLager<String,V> {

	private Path lagerLocation;

	private Boolean connected=false;

	@Override
	public synchronized void connect(String lagerLocation) throws IOException {

		assertDisconnected();

		final Path path = Paths.get(lagerLocation);
		if(!Files.isDirectory(path))
			initializeLocation(lagerLocation);
		this.lagerLocation=path;
		this.connected=true;
	}

	@Override
	public synchronized void close() throws Exception {
		assertConnected();
		this.lagerLocation=null;
		this.connected=false;
	}

	@Override
	public int size() {
		try {
			//potentially dangerous, i know.
			final Stream<Path> pathStream = Files.list(lagerLocation);
			final int count = (int) pathStream.count();
			pathStream.close();
			return count;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isEmpty() {
		return 0==size();
	}

	@Override
	public boolean containsKey(Object key) {
		return Files.exists(getObjectPath(key));
	}

	@Override
	public boolean containsValue(Object value) {
		return values().parallelStream().filter(v -> value.equals(v)).findFirst().isPresent();
	}

	@Override
	public V get(Object key) {

		if(!containsKey(key))
			return null;

		FileInputStream inputStream =null;
		try {
			inputStream = new FileInputStream(getObjectPath(key).toFile());
			V result=getPojoSerializer().deserialize(inputStream);
			return result;
		}
		catch (IOException e){
			throw new RuntimeException(e);
		}
		finally {
			if(inputStream!=null)
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}



	private Path getObjectPath(Object key) {
		return lagerLocation.resolve(key.toString());
	}

	@Override
	public V put(String key, V value) {
		FileOutputStream outputStream =null;
		try {
			outputStream = new FileOutputStream(getObjectPath(key).toFile());
			getPojoSerializer().serialize(value,outputStream);
		}
		catch (IOException e){
			throw new RuntimeException(e);
		}
		finally {
			if(outputStream!=null)
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	@Override
	public V remove(Object key) {
		V result=get(key);
		if(result!=null)
		{
			try {
				Files.delete(getObjectPath(key));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}

	@Override
	public void putAll(Map<? extends String, ? extends V> m) {
		m.forEach((key,value) -> put(key,value));
	}

	@Override
	public void clear() {
		try {
			final Stream<Path> pathStream = Files.list(lagerLocation);
			pathStream.forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			pathStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Set<String> keySet() {
		try {
			final Stream<Path> pathStream = Files.list(lagerLocation);
			final Set<String> keys = pathStream.map(path-> path.toFile().getName()).collect(Collectors.toSet());
			pathStream.close();
			return keys;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Collection<V> values() {
		return null;
	}

	@Override
	public Set<Entry<String, V>> entrySet() {
		return null;
	}

	private void initializeLocation(String lagerLocation) throws IOException {
		Files.createDirectory(Paths.get(lagerLocation));
	}

	private void assertDisconnected() {
		if(connected)
			throw new RuntimeException("already connected");
	}

	private void assertConnected() {
		if(!connected)
			throw new RuntimeException("not connected");
	}
}
