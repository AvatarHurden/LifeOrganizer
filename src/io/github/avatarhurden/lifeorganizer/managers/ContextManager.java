package io.github.avatarhurden.lifeorganizer.managers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import io.github.avatarhurden.lifeorganizer.objects.Context;

public class ContextManager {

	private ObservableList<Context> contexts;
	
	public ContextManager() {
		contexts = FXCollections.observableArrayList();
	}
	
	public Context getContext(String name, boolean filterActive) {
		for (Context p : contexts.filtered(c -> filterActive ? c.isActive() : true))
			if (p.getName().equals(name))
				return p;
		return null;
	}
	
	public Context createContext(String name, boolean isActive) {
		Context context = new Context(name, isActive);
		contexts.add(context);
		return context;
	}
	
	public ObservableList<Context> getContexts() {
		return contexts;
	}
	
	public ObservableList<Context> getActiveContexts() {
		return contexts.filtered(c -> c.isActive());
	}
	
}
