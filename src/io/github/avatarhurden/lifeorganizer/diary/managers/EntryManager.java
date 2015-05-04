package io.github.avatarhurden.lifeorganizer.diary.managers;

import io.github.avatarhurden.lifeorganizer.diary.models.DayOneEntry;
import io.github.avatarhurden.lifeorganizer.tools.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public class EntryManager {
	
	private Path entryFolder, imageFolder;
	
	private static final long READ_DELAY = 300;
	
	private Thread fileWatcher;
	private HashMap<String, WatchEvent.Kind<?>> filesToRead;
	
	private ObservableMap<String, DayOneEntry> entryMap;
	private ObservableList<DayOneEntry> entryList;
	
	private Set<String> ignoredEntries;
	
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
			
		entryMap = FXCollections.<String, DayOneEntry>observableHashMap();
		entryList = FXCollections.observableArrayList();
		ignoredEntries = new HashSet<String>();
		
		entryMap.addListener((MapChangeListener.Change<? extends String, ? extends DayOneEntry> change) -> 
			Platform.runLater(() -> {
				if (change.wasAdded())
					entryList.add(change.getValueAdded());
				if (change.wasRemoved())
					entryList.remove(change.getValueRemoved());
			})
		);
	}
	
	public void loadAndWatch() {
		try {
			readFolder();
			loadImages();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		filesToRead = new HashMap<String, WatchEvent.Kind<?>>();
		listenToFolder();
	}
	
	public void close() {
		fileWatcher.interrupt();
		fileWatcher = null;
	}
	
	public File getEntryFolder() {
		return entryFolder.toFile();
	}
	
	public void loadImages() throws IOException {
		DirectoryStream<Path> stream = Files.newDirectoryStream(imageFolder, 
				path -> Pattern.matches("^[0-9abcdefABCDEF]{32}\\.jpg", path.getFileName().toString()));
		for (Path file: stream) {
		  	String id = file.getFileName().toString().replace(".jpg", "");
		  	entryMap.get(id).setImageFile(file.toFile());
		}
	}
	
	public ObservableList<DayOneEntry> getEntries() {
		return entryList;
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
		entryMap.put(t.getUUID(), t);
		return t;
	}
	
	public void deleteEntry(DayOneEntry entry) {
		entryMap.remove(entry.getUUID());
		entry.delete();
	}
	
	private void readFolder() throws Exception {
		DirectoryStream<Path> stream = Files.newDirectoryStream(entryFolder, 
				path -> Pattern.matches("^[0-9abcdefABCDEF]{32}\\.doentry", path.getFileName().toString()));
		for (Path file: stream) {
		  	String id = file.getFileName().toString().replace(".doentry", "");
		   	entryMap.put(id, DayOneEntry.loadFromFile(this, file.toFile()));
		}
	}
	
	private void listenToFolder() {
		fileWatcher = new Thread(() -> {
			
			WatchService watcher = null;
			try {
				watcher = FileSystems.getDefault().newWatchService();
				entryFolder.register(watcher, 
						StandardWatchEventKinds.ENTRY_CREATE, 
						StandardWatchEventKinds.ENTRY_DELETE, 
						StandardWatchEventKinds.ENTRY_MODIFY);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			if (watcher == null)
				return;
			
			while (true) {
				WatchKey key;
				try {
					key = watcher.take();
				} catch (InterruptedException e) { return; }
			
				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();
		        
					if (kind == StandardWatchEventKinds.OVERFLOW) continue;
		        
					Path file = entryFolder.resolve((Path) event.context());
		        
					// Only registers for matching filenames
					if (!Pattern.matches("^[0-9abcdefABCDEF]{32}\\.doentry", file.getFileName().toString())) 
						continue;
					
					String id = file.getFileName().toString().replace(".doentry", "");
					if (ignoredEntries.contains(id))
						continue;
					
					if (!filesToRead.containsKey(id))
						new Thread(() -> {
							try {
								Thread.sleep(READ_DELAY);
							} catch (Exception e1) {}
							readFile(id, file);
							filesToRead.remove(id);
						}).start();

					filesToRead.put(id, kind);
				}
			
				key.reset();
			}
		});
		
		fileWatcher.start();
	}
	
	private void readFile(String id, Path file) {
		WatchEvent.Kind<?> latestKind = filesToRead.get(id);
		
		Platform.runLater(() -> {
			if (latestKind == StandardWatchEventKinds.ENTRY_CREATE)
				try {
					entryMap.put(id, DayOneEntry.loadFromFile(this, file.toFile()));
				} catch (Exception e) {	
					e.printStackTrace();
				}
			else if (latestKind == StandardWatchEventKinds.ENTRY_DELETE)
				entryMap.remove(id);
			else if (latestKind == StandardWatchEventKinds.ENTRY_MODIFY)
				try { // It can happen that a file is created and modified before being read, so test if it is in the map
					if (!entryMap.containsKey(id))
						entryMap.put(id, DayOneEntry.loadFromFile(this, file.toFile()));
					else
						entryMap.get(id).readFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
		});
	}
	
}
