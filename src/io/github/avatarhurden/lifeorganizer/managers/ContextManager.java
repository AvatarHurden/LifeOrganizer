package io.github.avatarhurden.lifeorganizer.managers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import io.github.avatarhurden.lifeorganizer.objects.Context;

public class ContextManager {

	private ObservableList<Context> activeContexts;
	
	public ContextManager() {
		activeContexts = FXCollections.observableArrayList();
	}
	
	public Context getContext(String name) {
		for (Context p : activeContexts)
			if (p.getName().equals(name))
				return p;
		return null;
	}
	
	public Context createContext(String name) {
		Context context = new Context(name);
		activeContexts.add(context);
		return context;
	}
	
	public ObservableList<Context> getActiveContexts() {
		return activeContexts;
	}
	
}
