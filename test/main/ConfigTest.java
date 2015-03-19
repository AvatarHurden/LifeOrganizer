package main;

import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConfigTest {

	private File configFile, apiFile;
	
	@Before
	public void createFile() throws IOException {
		apiFile = File.createTempFile("lifeOrganizer", ".tmp");
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(apiFile));
		writer.write("[Fitbit]\n");
		writer.write("key = fitbitkey\n");
		writer.write("secret = fitbitsecret\n");
		writer.write("[Google]\n");
		writer.write("key = googlekey\n");
		writer.close();
		
		configFile = File.createTempFile("lifeOrganizer", ".tmp");
		configFile.delete();
	}
	
	@After
	public void deleteFile() {
		configFile.delete();
	}
	
	@Test
	public void testAPILoading() {
		Config g = null;
		try {
			g = new Config(apiFile, configFile);
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
			
		Properties fitbit = g.getAPIProperties("Fitbit");
		Properties google = g.getAPIProperties("Google");	
		
		assertEquals("fitbitkey", fitbit.get("key"));
		assertEquals("fitbitsecret", fitbit.get("secret"));
		
		assertEquals("googlekey", google.get("key"));
	}
	
	@Test
	public void testConfigCreation() throws IOException {
		Config g = new Config(apiFile, configFile);
		
		File defaultFolder = new File("data").getAbsoluteFile();
		
		assertEquals(defaultFolder, g.getDataFolder());
		assertEquals(new File(defaultFolder, "todo.txt"), g.getFile("todo"));
		assertEquals(new File(defaultFolder, "done.txt"), g.getFile("done"));
		assertEquals(new File(defaultFolder, "sleep.txt"), g.getFile("sleep"));
		assertEquals(new File(defaultFolder, "locations.txt"), g.getFile("locations"));
	}
	
	@Test
	public void testSetFilesAndFolder() throws IOException {
		Config g = new Config(apiFile, configFile);
		
		g.setDataFolder("do\\data");
		assertEquals(new File("do\\data").getAbsoluteFile(), g.getDataFolder());
		
		g.setDataFolder("C:\\data");
		assertEquals(new File("C:\\data"), g.getDataFolder());
		
		g.setFile("todo", "todo.txt");
		assertEquals(new File("C:\\data\\todo.txt"), g.getFile("todo"));
		
		g.setFile("sleep", "C:\\sleep.bin");
		assertEquals(new File("C:\\sleep.bin"), g.getFile("sleep"));
	}
	
	@Test
	public void testSetUserAPI() {
		Config g = null;
		try {
			g = new Config(apiFile, configFile);
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
		
		g.setAPIUserProperty("Fitbit", "key", "userkey");
		g.setAPIUserProperty("Fitbit", "secret", "usersecret");
		
		Properties p = g.getAPIUserProperties("Fitbit");
		
		Assert.assertEquals("userkey", p.get("key"));
		Assert.assertEquals("usersecret", p.get("secret"));
	}	
}
