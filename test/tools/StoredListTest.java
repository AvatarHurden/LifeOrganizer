package tools;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StoredListTest {

	File file;
	
	@Before
	public void createFile() throws IOException {
		file = File.createTempFile("lifeOrganizer", null);
	}
	
	@After
	public void deleteFile() {
		file.delete();
	}
	
	@Test
	public void createList() {
		StoredList<String> list = setUp();
		
		assertEquals(0, list.size());
	}
	
	@Test
	public void ordersElements() {
		StoredList<String> list = setUp();
		
		list.add("3last");
		list.add("1first");
		list.add("2second");
		
		assertArrayEquals(new String[] {"1first", "2second", "3last"}, list.toArray());
	}
	
	@Test
	public void testSave() {
		StoredList<String> list = setUp();
		
		list.add("1first");
		list.add("2second");
		list.add("3last");
		
		try {
			list.save();
		
			BufferedReader reader = new BufferedReader(new FileReader(file));
			assertEquals("1first", reader.readLine());
			assertEquals("2second", reader.readLine());
			assertEquals("3last", reader.readLine());
			assertEquals(null, reader.readLine());
			reader.close();
		} catch (IOException e) {
			Assert.fail();
		}
		
	}
	
	@Test
	public void testSaveLoad() {
		StoredList<String> list = setUp();
		
		list.add("1first");
		list.add("2second");
		list.add("3last");
		
		try {
			list.save();
		} catch (IOException e) {
			Assert.fail();
		}
		StoredList<String> list2 = setUp();
		
		assertEquals(list, list2);
	}
	
	private StoredList<String> setUp() {
		return new StoredList<String>(file, true) {
			@Override
			String decode(String line) {
				return line;
			}

			@Override
			String encode(String object) {
				return object;
			}

		};
	}
	
}
