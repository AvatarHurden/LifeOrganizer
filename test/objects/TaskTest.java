package objects;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

public class TaskTest {
	
	@Test
	public void testCreationDate() {
		Task t = setUp();
		
		Assert.assertEquals(new DateTime().getMillis(), t.getCreationDate().getMillis(), 1000);
		Assert.assertEquals(new DateTime().getMillis(), t.getEditDate().getMillis(), 1000);
	}
	
	@Test
	public void testSetName() {
		Task t = setUp();
		String name = "Test Function";
		
		t.setName(name);
		
		Assert.assertEquals(name, t.getName());
		Assert.assertTrue(wasEdited(t));
	}
	
	@Test
	public void testPriority() {
		Task t = setUp();
		
		t.setPriority('a');
		
		Assert.assertEquals('A', t.getPriority());
		Assert.assertTrue(wasEdited(t));
	}
	
	@Test
	public void testDoTask() {
		Task t = setUp();
		
		t.setState(Task.State.DONE);
		
		Assert.assertEquals(t.getState(), Task.State.DONE);
		Assert.assertEquals(new DateTime().getMillis(), t.getCompletionDate().getMillis(), 1000);
		Assert.assertTrue(wasEdited(t));
	}
	
	@Test
	public void testFailTask() {
		
		Task t = setUp();
		
		t.setState(Task.State.FAILED);
		
		Assert.assertEquals(t.getState(), Task.State.FAILED);
		Assert.assertEquals(new DateTime().getMillis(), t.getCompletionDate().getMillis(), 1000);
		Assert.assertTrue(wasEdited(t));	
	}	
	
	@Test
	public void testProjects() {
		Task t = setUp();
		
		t.addProject("+UFRGS");
		t.addProject("PC");
		
		Assert.assertArrayEquals(new String[] {"+UFRGS", "+PC"}, t.getProjects().toArray());

		t.removeProject("+PC");
		t.removeProject("UFRGS");
		
		Assert.assertArrayEquals(new String[0], t.getProjects().toArray());	
		Assert.assertTrue(wasEdited(t));
	}
	
	@Test
	public void testContexts() {
		Task t = setUp();
		
		t.addContext("@UFRGS");
		t.addContext("Casa");
		
		Assert.assertArrayEquals(new String[] {"@UFRGS", "@Casa"}, t.getContexts().toArray());
	
		t.removeContext("@Casa");
		t.removeContext("UFRGS");
		
		Assert.assertArrayEquals(new String[0], t.getContexts().toArray());
		
		
		Assert.assertTrue(wasEdited(t));
	}

	@Test
	public void testNotes() {
		Task t = setUp();
		
		t.setNote("This is a note");
		
		Assert.assertEquals("This is a note", t.getNote());
		Assert.assertTrue(wasEdited(t));
	}
	
	@Test
	public void testSetDue() {
		Task t = setUp();
		DateTime d = new DateTime().plusDays(2);
		
		t.setDueDate(d);
		
		Assert.assertEquals(d, t.getDueDate());
		Assert.assertTrue(wasEdited(t));
	}
	
	@Test
	public void testEncoding() {
		String s = "[ ] DUE=2015.05.01 MADE=2015.03.12@14:42 NAME=\"Fazer LET#NINJA\" PROJS=+Tema,+CPD CONTEXTS=@Home EDIT=2015.03.12@14:42";
		String s2 = "[x] DONE=2015.02.23@18:22 (B) MADE=2015.02.17@22:50 NAME=\"Add comments to tasks\" PROJS=+LifeOrganizer NOTE=\"Very easy, some difficulty making CLI\" EDIT=2015.02.23@18:22\n";
		String s3 = "[ ] MADE=2015.03.17@14:13 NAME=\"Ler \\\"A rua é bela\\\"\" EDIT=2015.03.17@14:13";
		
		Task t = Task.decode(s);
		Task t2 = Task.decode(s2);
		Task t3 = Task.decode(s3);
		
		Assert.assertEquals(s, t.encode());
		Assert.assertEquals(s2, t2.encode() + "\n");
		Assert.assertEquals(s3, t3.encode());
	}
	
	private Task setUp() {
		return new Task();
	}
	
	private boolean wasEdited(Task t) {
		return Math.abs(new DateTime().getMillis() - t.getEditDate().getMillis()) < 1000
				&& t.getCreationDate() != t.getEditDate();
	}
	
}
