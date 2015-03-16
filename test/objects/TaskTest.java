package objects;

import static org.junit.Assert.assertEquals;

import org.joda.time.DateTime;
import org.junit.Test;

public class TaskTest {

	@Test
	public void testSetName() {
		Task t = setUp();
		String name = "Test Function";
		
		t.setName(name);
		
		assertEquals(name, t.getName());
	}
	
	@Test
	public void testCreationDate() {
		Task t = setUp();
		DateTime d = new DateTime();
		
		assertEquals(d.getMillis(), t.getCreationDate().getMillis(), 1000);
	}
	
	@Test
	public void testDoTask() {
		Task t = setUp();
		DateTime d = new DateTime();
		
		t.setState(Task.State.DONE);
		
		assertEquals(t.getState(), Task.State.DONE);
		assertEquals(d.getMillis(), t.getCompletionDate().getMillis(), 1000);
		assertEquals(d.getMillis(), t.getEditDate().getMillis(), 1000);
	}
	
	@Test
	public void testFailTask() {
		Task t = setUp();
		DateTime d = new DateTime();
		
		t.setState(Task.State.FAILED);
		
		assertEquals(t.getState(), Task.State.FAILED);
		assertEquals(d.getMillis(), t.getCompletionDate().getMillis(), 1000);
		assertEquals(d.getMillis(), t.getEditDate().getMillis(), 1000);
	}	

	private Task setUp() {
		return new Task();
	}
	
}
