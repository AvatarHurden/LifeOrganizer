package io.github.avatarhurden.lifeorganizer.diary.managers;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import io.github.avatarhurden.lifeorganizer.diary.models.DayOneEntry;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.ParseException;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.dd.plist.PropertyListFormatException;

public class EntryManager {

	private Path folder;
	
	private ObservableMap<String, DayOneEntry> entryMap;
	private ObservableList<DayOneEntry> entryList;
	
	public EntryManager(Path folder) {
		this.folder = folder;
		
		entryMap = FXCollections.<String, DayOneEntry>observableHashMap();
		entryList = FXCollections.<DayOneEntry>observableArrayList();
		
		entryMap.addListener((MapChangeListener.Change<? extends String, ? extends DayOneEntry> change) ->{
			entryList.remove(change.getValueRemoved());
			if (change.getValueAdded() != null) {
				entryList.add(change.getValueAdded());
				System.out.println(change.getValueAdded().getDictionary().get("Entry Text"));
			} else
				System.out.println(change.getKey());
		});
		
		new Thread(() -> {
			
			WatchService watcher = null;
			try {
				watcher = FileSystems.getDefault().newWatchService();
				folder.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		while (true) {
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException e) { return; }
			
			for (WatchEvent<?> event : key.pollEvents()) {
		        WatchEvent.Kind<?> kind = event.kind();
		        
		        if (kind == OVERFLOW) return;
		        
		        Path file = folder.resolve((Path) event.context());
		        
		        if (!file.getFileName().endsWith(".doentry"))
		        	return;

		    	String id = file.getFileName().toString().replace(".doentry", "");
		        if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY)
					try {
						entryMap.put(id, DayOneEntry.loadFromFile(file.toFile()));
					} catch (Exception e) {	e.printStackTrace(); }
				else if (kind == ENTRY_DELETE)
		        	entryMap.remove(id);
			}
			
			key.reset();
		}
		}).start();
		
		start();
		
	}

	private void start() {
		
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, path -> path.getFileName().toString().endsWith(".doentry"))) {
		    for (Path file: stream) {
		    	String id = file.getFileName().toString().replace(".doentry", "");
		    	entryMap.put(id, DayOneEntry.loadFromFile(file.toFile()));
		    	System.out.println(id);
		    	System.out.println(file.getFileName());
		    }
		} catch (IOException | DirectoryIteratorException | PropertyListFormatException | ParseException | ParserConfigurationException | SAXException x) {
		   System.err.println(x);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
