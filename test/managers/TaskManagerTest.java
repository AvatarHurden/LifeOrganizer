package managers;

import java.io.File;
import java.io.IOException;

import main.Config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TaskManagerTest {

	Config config;
	File todo, done;
	
	@Before
	public void makeMockConfig() throws IOException {
		config = Mockito.mock(Config.class);
		todo = File.createTempFile("lifeOrganizer", "");
		done = File.createTempFile("lifeOrganizer", "");
		Mockito.when(config.getFile("todo")).thenReturn(todo);
		Mockito.when(config.getFile("done")).thenReturn(done);
	}
	
	@After
	public void deleteFiles() {
		todo.delete();
		done.delete();
	}
	
	@Test
	public void testLoadTasks() {
		TaskManager manager = setUp();
		
	}

	private TaskManager setUp() {
		return new TaskManager(config);
	}
}


