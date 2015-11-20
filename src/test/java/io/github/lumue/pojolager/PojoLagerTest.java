package io.github.lumue.pojolager;

import static org.junit.Assert.*;

import io.github.lumue.pojolager.testbeans.Person;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class PojoLagerTest {

	private final static String LAGER_LOCATION="./testlager";

	/**
	 * Always start with a clean filesystem location
	 * @throws IOException
	 */
	@Before
	public void removeLagerLocationDirectory() throws IOException {
		Files.deleteIfExists(Paths.get(LAGER_LOCATION));
	}

	@Test
	public void testConnectToEmptyLocation() throws Exception {

		PojoLager<Person> personLager=PojoLager.create(Person.class);
		personLager.connect(LAGER_LOCATION);

		assertTrue(personLager.isConnected());
		assertTrue("directory not created",Files.exists(Paths.get(LAGER_LOCATION)));
	}

}
