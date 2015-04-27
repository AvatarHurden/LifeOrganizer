package io.github.avatarhurden.lifeorganizer.managers;

import io.github.avatarhurden.lifeorganizer.objects.Context;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ContextManager {

	private ObservableList<Context> contexts;
	
	public ContextManager() {
		contexts = FXCollections.observableArrayList();
	}

	public void incrementContext(Context c, boolean isActive) {
		c.incrementCount(isActive);
	}
	
	public void decrementContext(Context c, boolean isActive) {
		c.decrementCount(isActive);
		if (!c.isActive() && c.getInactiveTasks() == 0)
			contexts.remove(c);
	}
	
	public Context createContext(String name, boolean isActive) {
		Context context = getContext(name);
		if (context == null)
			context = addContext(name);
		context.incrementCount(isActive);
		return context;
	}
	
	public Context addContext(String name) {
		Context context = new Context(name);
		contexts.add(context);
		return context;
	}
	
	public ObservableList<Context> getContexts() {
		return contexts;
	}
	
	public ObservableList<Context> getActiveContexts() {
		return contexts.filtered(c -> c.isActive());
	}	

	public Context getContext(String name) {
		for (Context c : contexts)
			if (c.getName().equals(name))
				return c;
		return null;
	}
	
}
