package tools;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
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
	
	private StoredList<String> setUp() {
		return new StoredList<String>(file, true);
	}
	
}
