package io.github.lumue.pojolager.pojofile;

import io.github.lumue.pojolager.PojoLager;
import io.github.lumue.pojolager.testbeans.Person;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertEquals;


public class PojoFileLagerTest {

	private final static String LAGER_LOCATION="./testlager";

	/**
	 * Always start with a clean filesystem location
	 * @throws IOException
	 */
	@Before
	public void removeLagerLocationDirectory() throws IOException {
		FileUtils.deleteDirectory(new File(LAGER_LOCATION));
	}

	@Test
	public void testConnectToEmptyLocation() throws Exception {

		PojoLager<Person> personLager=PojoLager.create(Person.class);
		personLager.connect(LAGER_LOCATION);

		Assert.assertTrue(personLager.isConnected());
		Assert.assertTrue("directory not created",Files.exists(Paths.get(LAGER_LOCATION)));
	}

	@Test
	public void testConnectToAlreadyConnectedLager() throws Exception {

		PojoLager<Person> personLager=PojoLager.create(Person.class);
		personLager.connect(LAGER_LOCATION);

		boolean exceptionThrown=false;

		try{
			personLager.connect(LAGER_LOCATION);
		}
		catch(RuntimeException e){
			exceptionThrown=true;
		}

		Assert.assertTrue("expected exception on second connect",exceptionThrown);
	}

	@Test
	public void testCallsToDisconnectedLager() throws Exception {

		boolean exceptionThrown=false;

		PojoLager<Person> personLager=PojoLager.create(Person.class);

		try{
			personLager.size();
		}
		catch(RuntimeException e){
			exceptionThrown=true;
		}

		Assert.assertTrue("expected exception on call to disconnected lager",exceptionThrown);
	}



	@Test
	public void testPutGetCycle() throws Exception {

		PojoLager<Person> personLager=PojoLager.create(Person.class);
		personLager.connect(LAGER_LOCATION);

		Assert.assertNull("get before put should return null",personLager.get("1"));

		Person testperson=new Person("1",LocalDate.now());
		personLager.put("1",testperson);

		Assert.assertTrue("expected file does not exist",Paths.get(LAGER_LOCATION).resolve("data").resolve("1.pojo").toFile().exists());

		Person returnedPerson = personLager.get("1");
		Assert.assertNotNull("get after put should not return null", returnedPerson);

		assertEquals("get should return an object equal to the object put",testperson,returnedPerson);

	}

	@Test
	public void testKeySet() throws Exception {

		PojoLager<Person> personLager=PojoLager.create(Person.class);
		personLager.connect(LAGER_LOCATION);

		Person testperson=new Person("1",LocalDate.now());
		personLager.put("1",testperson);

		Set<String> keySet = personLager.keySet();
		Assert.assertNotNull("keySet should not return null", keySet);
		Assert.assertEquals("keySet size should be 1.",1,keySet.size());
		Assert.assertTrue("keySet should contain \"1\" ",keySet.contains("1"));
	}

	@Test
	public void testValues() throws Exception {

		PojoLager<Person> personLager=PojoLager.create(Person.class);
		personLager.connect(LAGER_LOCATION);

		Person testperson=new Person("1",LocalDate.now());
		personLager.put("1",testperson);

		Collection<Person> values = personLager.values();
		Assert.assertNotNull("values() should not return null", values);
		Assert.assertEquals("values size should be 1.",1,values.size());
		Assert.assertTrue("values should contain inserted person ",values.contains(testperson));
	}
}
