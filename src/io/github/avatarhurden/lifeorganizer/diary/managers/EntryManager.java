package io.github.avatarhurden.lifeorganizer.diary.managers;

import io.github.avatarhurden.lifeorganizer.diary.models.DayOneEntry;
import io.github.avatarhurden.lifeorganizer.tools.Config;
import io.github.avatarhurden.lifeorganizer.tools.DirectoryWatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;

public class EntryManager {
	
	private Path entryFolder, imageFolder;
	
	private ObservableMap<String, DayOneEntry> entryMap;
	private ObservableList<DayOneEntry> entryList;
	private ObservableSet<String> tagsList;
	
	private Set<String> ignoredEntries;
	private DirectoryWatcher watcher;
	
	public static boolean isInitiliazed() {
		return Config.get().getProperty("diary_folder") != null;
	}
	
	public EntryManager() {
		Path rootFolder = Paths.get(Config.get().getProperty("diary_folder"));
		entryFolder = rootFolder.resolve("entries");
		imageFolder = rootFolder.resolve("photos");
		
		if (!entryFolder.toFile().exists())
			entryFolder.toFile().mkdirs();
		if (!imageFolder.toFile().exists())
			imageFolder.toFile().mkdirs();
			
		entryList = FXCollections.observableArrayList();
		entryList.addListener((ListChangeListener.Change<? extends DayOneEntry> event) -> {
			while (event.next()) {
				if (event.wasRemoved())
					for (DayOneEntry entry : event.getRemoved())
						entryMap.remove(entry.getUUID());
				if (event.wasAdded())
					for (DayOneEntry entry : event.getAddedSubList())
						entryMap.put(entry.getUUID(), entry);
			}
		});
		
		entryMap = FXCollections.observableHashMap();
		
		tagsList = FXCollections.observableSet();
		ignoredEntries = new HashSet<String>();
	}
	
	public DayOneEntry getEntry(String id) {
		return entryMap.get(id);
	}
	
	public void loadAndWatch() {
		try {
			readFolder();
			loadImages();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		listenToFolder();
	}
	
	public void close() {
		watcher.stopWatching();
	}
	
	public File getEntryFolder() {
		return entryFolder.toFile();
	}
	
	public void loadImages() throws IOException {
		for (DayOneEntry entry : entryList) {
		  	String id = entry.getUUID() + ".jpg";
		  	entry.setImageFile(new File(imageFolder.toFile(), id));
		}
	}
	
	public ObservableList<DayOneEntry> getEntries() {
		return entryList;
	}
	
	public ObservableSet<String> getTags() {
		return tagsList;
	}
	
	public void ignoreForAction(String uuid, Runnable action) {
		ignoreEntry(uuid);
		
		action.run();
		
		new Thread(() -> {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
			removeIgnore(uuid);
		}).run();
	}
	
	public void ignoreEntry(String uuid) {
		ignoredEntries.add(uuid);
	}
	
	public void removeIgnore(String uuid) {
		ignoredEntries.remove(uuid);
	}

	public DayOneEntry addEntry() {
		DayOneEntry t = DayOneEntry.createNewEntry(this);
		entryList.add(t);
		return t;
	}
	
	public void deleteEntry(DayOneEntry entry) {
		entryList.remove(entry);
		entry.delete();
	}
	
	private void readFolder() throws Exception {
		DirectoryStream<Path> stream = Files.newDirectoryStream(entryFolder, 
				path -> Pattern.matches("^[0-9abcdefABCDEF]{32}\\.doentry", path.getFileName().toString()));
		for (Path file: stream) {
			DayOneEntry entry = DayOneEntry.loadFromFile(this, file.toFile());
		   	entryList.add(entry);
		   	for (String tag : entry.getTags())
		   		tagsList.add(tag);
		}
	}
	
	private void listenToFolder() {
		watcher = new DirectoryWatcher(entryFolder);
		watcher.addFilter(path -> Pattern.matches("^[0-9abcdefABCDEF]{32}\\.doentry", path.getFileName().toString()));
		
		watcher.addAction((path, kind) -> readFile(path, kind));
		
		new Thread(() -> {
			try {
				watcher.startWatching();
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}).start();
	}
	
	private void readFile(Path path, WatchEvent.Kind<?> kind) {
		String id = path.getFileName().toString().replace(".doentry", "");
		DayOneEntry entry = getEntry(id);
		
		Platform.runLater(() -> {
			if (kind == StandardWatchEventKinds.ENTRY_CREATE)
				try {
					entryList.add(DayOneEntry.loadFromFile(this, path.toFile()));
				} catch (Exception e) {	
					e.printStackTrace();
				}
			else if (kind == StandardWatchEventKinds.ENTRY_DELETE)
				entryList.remove(entry);
			else if (kind == StandardWatchEventKinds.ENTRY_MODIFY)
				try { // It can happen that a file is created and modified before being read, so test if it is in the map
					if (entry == null)
						entryList.add(DayOneEntry.loadFromFile(this, path.toFile()));
					else
						entry.readFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
		});
	}
	
}
