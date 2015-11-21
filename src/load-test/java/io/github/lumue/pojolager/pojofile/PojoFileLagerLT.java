package io.github.lumue.pojolager.pojofile;

import io.github.lumue.pojolager.PojoLager;
import io.github.lumue.pojolager.testbeans.Person;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Created by lm on 21.11.15.
 */
public class PojoFileLagerLT {

	private final static String LAGER_LOCATION="./loadtestlager";

	/**
	 * Always start with a clean filesystem location
	 * @throws IOException
	 */
	@Before
	public void removeLagerLocationDirectory() throws IOException {
		FileUtils.deleteDirectory(new File(LAGER_LOCATION));
	}

	@Test
	public void testPutAMillionSmallObjectsAndGetEachOneBack() throws Exception {

		PojoLager<Person> personLager=PojoLager.create(Person.class);
		personLager.connect(LAGER_LOCATION);

		long putStartTime = System.nanoTime();
		int testCycleCount = 1000000;
		for(int i = 1; i<= testCycleCount; i++){
			String key=Integer.toString(i);
			personLager.put(key,new Person(UUID.randomUUID().toString(), LocalDate.now()));
		}
		long putEndTime = System.nanoTime();
		long putExecutionTime = (putEndTime - putStartTime)/1000000;
		long putAverage=((putEndTime - putStartTime)/ (testCycleCount/1000))/1000000;

		System.out.println(testCycleCount+" put operations: " + putExecutionTime+"ms ("+putAverage+"ms per 1000 puts)");


		long getStartTime = System.nanoTime();
		for(int i = 1; i<= testCycleCount; i++){
			String key=Integer.toString(i);
			personLager.get(key);
		}
		long getEndTime = System.nanoTime();
		long getExecutionTime = (getEndTime - getStartTime)/1000000;
		long getAverage=((getEndTime - getStartTime)/ (testCycleCount/1000))/1000000;

		System.out.println(testCycleCount+" get operations: " + getExecutionTime+"ms ("+getAverage+"ms per 1000 gets)");
	}
}
