package io.github.lumue.pojolager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The default Pojo Lager.
 *  - use a single file in this Lagers directory per object
 *  - use the key as the base filename
 *
 * Pojos are written to disk on put. There is no dirty checking or other sophisticated stuff. If you want to update the persistent representation of an object, just "put" it again with the same key.
 * Created by lm on 20.11.15.
 */
class OnePojoOneFileLager<V> implements PojoLager<String,V> {


	static final class LagerEntry<V> implements Map.Entry<String,V> {

		private final String key; // non-null
		private V val;       // non-null
		private final OnePojoOneFileLager<V> lager;

		LagerEntry(String key, V val, OnePojoOneFileLager<V> lager) {
			this.key = key;
			this.val = val;
			this.lager=lager;
		}

		public String getKey()        { return key; }
		public V getValue()      { return val; }
		public int hashCode()    { return key.hashCode() ^ val.hashCode(); }
		public String toString() { return key + "=" + val; }

		public boolean equals(Object o) {
			Object k, v; Map.Entry<?,?> e;
			return ((o instanceof Map.Entry) &&
					(k = (e = (Map.Entry<?,?>)o).getKey()) != null &&
					(v = e.getValue()) != null &&
					(k == key || k.equals(key)) &&
					(v == val || v.equals(val)));
		}

		/**
		 * Sets our entry's value and writes through to the lager
		 */
		public V setValue(V value) {
			if (value == null) throw new NullPointerException();
			V v = val;
			val = value;
			lager.put(key, value);
			return v;
		}
	}

	private Path lagerLocation;

	private Boolean connected=false;
	private final PojoSerializer<V> pojoSerializer;

	OnePojoOneFileLager(PojoSerializer<V> pojoSerializer) {
		this.pojoSerializer = pojoSerializer;
	}


	/**
	 * connect to a lager at @param lagerLocation. if @param lagerLocation does not exist, it will be created.
	 * @param lagerLocation path to the filesystem location of a OnePojoOneFileLager
	 */
	@Override
	public synchronized void connect(String lagerLocation) throws IOException {

		assertDisconnected();
		this.lagerLocation=Paths.get(Objects.requireNonNull(lagerLocation,"lagerLocation must not be null"));

		if(!isLocationInitialized())
			initializeLocation();

		this.connected=true;
	}



	/**
	 * close connection to a physical lagerLocation
	 */
	@Override
	public synchronized void close()  {
		assertConnected();
		this.lagerLocation=null;
		this.connected=false;
	}

	@Override
	public int size() {
		assertConnected();
		try {
			final Stream<Path> pathStream = Files.list(getLagerLocation());
			long fileCount = pathStream.count();
			final int size = fileCount<=Integer.MAX_VALUE?(int)fileCount:Integer.MAX_VALUE;
			pathStream.close();
			return size;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isEmpty() {
		assertConnected();
		return 0==size();
	}

	@Override
	public boolean containsKey(Object key) {
		assertConnected();
		return Files.exists(resolveFilePathForKey(key));
	}

	@Override
	public boolean containsValue(Object value) {
		assertConnected();
		return values().parallelStream().filter(value::equals).findFirst().isPresent();
	}

	@Override
	public V get(Object key) {

		assertConnected();

		if (!containsKey(key))
			return null;

		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(resolveFilePathForKey(key).toFile());
			return getPojoSerializer().deserialize(inputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (inputStream != null)
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}



	private Path resolveFilePathForKey(Object key) {
		return resolveDataDirectory().resolve(key.toString());
	}

	@Override
	public V put(String key, V value) {
		FileOutputStream outputStream =null;
		try {
			outputStream = new FileOutputStream(resolveFilePathForKey(key).toFile());
			getPojoSerializer().serialize(value,outputStream);
			return value;
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
				Files.delete(resolveFilePathForKey(key));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}

	@Override
	public void putAll(Map<? extends String, ? extends V> m) {
		m.forEach(this::put);
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
			final Set<String> keys = pathStream
					.map(path-> path.toFile().getName())
					.collect(Collectors.toSet());
			pathStream.close();
			return keys;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<V> values() {
		return keySet()
				.parallelStream()
				.map(this::get)
				.collect(Collectors.toList());
	}

	@Override
	public Set<Entry<String, V>> entrySet() {
		return keySet()
				.parallelStream()
				.map(key -> new LagerEntry<>(key, get(key), this))
				.collect(Collectors.toSet());
	}

	@Override
	public boolean isConnected(){
		return connected;
	}

	private void initializeLocation() throws IOException {

		if(!Files.isDirectory(getLagerLocation())){
			Files.deleteIfExists(getLagerLocation());
			Files.createDirectory(getLagerLocation());
		}

		if(!Files.isDirectory(resolveDataDirectory())){
			Files.deleteIfExists(resolveDataDirectory());
			Files.createDirectory(resolveDataDirectory());
		}
	}

	private boolean isLocationInitialized() {

		if(this.getLagerLocation()==null)
			throw new NullPointerException("OnePojoOneFileLager.lagerLocation must not be null ");

		return  Files.exists(getLagerLocation()) && Files.isDirectory(getLagerLocation())
				&&
				Files.exists(resolveDataDirectory()) && Files.isDirectory(resolveDataDirectory()) ;
	}


	private void assertDisconnected() {
		if(connected)
			throw new RuntimeException("already connected");
	}

	private void assertConnected() {
		if(!connected)
			throw new RuntimeException("not connected");
	}

	private PojoSerializer<V> getPojoSerializer() {
		return pojoSerializer;
	}



	private Path getLagerLocation() {
		return lagerLocation;
	}

	private Path resolveDataDirectory() {
		return Objects.requireNonNull(getLagerLocation(),"can not get data directory without base directory. lagerLocation must not be null").resolve("data");
	}
}
