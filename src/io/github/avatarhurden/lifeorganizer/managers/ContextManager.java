package io.github.avatarhurden.lifeorganizer.managers;

import io.github.avatarhurden.lifeorganizer.objects.Context;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ContextManager {

	private ObservableList<Context> contexts;
	
	public ContextManager() {
		contexts = FXCollections.observableArrayList();
	}

	public void incrementContexts(boolean isActive, Context... c) {
		for (Context context : c)
			context.incrementCount(isActive);
	}

	public void incrementContexts(boolean isActive, List<Context> c) {
		for (Context context : c)
			context.incrementCount(isActive);
	}
	
	public void decrementContexts(boolean isActive, Context... c) {
		for (Context context : c) {
			context.decrementCount(isActive);
			if (!context.isActive() && context.getInactiveTasks() == 0)
				contexts.remove(context);
		}
	}
	
	public void decrementContexts(boolean isActive, List<Context> c) {
		for (Context context : c) {
			context.decrementCount(isActive);
			if (!context.isActive() && context.getInactiveTasks() == 0)
				contexts.remove(context);
		}
	}
	
	public void moveContexts(boolean toActive, List<Context> c) {
		decrementContexts(!toActive, c);
		incrementContexts(toActive, c);
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
