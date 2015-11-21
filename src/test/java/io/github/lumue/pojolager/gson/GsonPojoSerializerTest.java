package io.github.lumue.pojolager.gson;

import io.github.lumue.pojolager.PojoSerializer;
import io.github.lumue.pojolager.testbeans.Person;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

/**
 * Test gson object serialization
 *
 * Created by lm on 21.11.15.
 */
public class GsonPojoSerializerTest {

	@Test
	public void testSerializeDeserializeCycle() throws Exception {

		PojoSerializer<Person> serializer=new GsonPojoSerializer<>(Person.class);
		Person testperson=new Person("testperson", LocalDate.now());

		ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
		serializer.serialize(testperson,outputStream);
		String serializedTestperson=outputStream.toString();
		outputStream.close();

		assertFalse("empty serialization output",serializedTestperson.isEmpty());

		InputStream inputstream = new ByteArrayInputStream(serializedTestperson.getBytes());
		Person deserializedPerson=serializer.deserialize(inputstream);
		inputstream.close();

		assertNotNull("deserialized person was null",deserializedPerson);
		assertEquals("deserialized object does not equal original",testperson,deserializedPerson);
	}

}
