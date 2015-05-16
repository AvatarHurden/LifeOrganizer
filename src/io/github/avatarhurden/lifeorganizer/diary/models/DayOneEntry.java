package io.github.avatarhurden.lifeorganizer.diary.models;

import io.github.avatarhurden.lifeorganizer.diary.managers.EntryManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javafx.beans.property.Property;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.scene.image.Image;

import org.joda.time.DateTime;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;

public class DayOneEntry implements Comparable<DayOneEntry> {

	public static DayOneEntry loadFromFile(EntryManager manager, File file) throws Exception {
		return new DayOneEntry(manager, (NSDictionary) PropertyListParser.parse(file), file);
	}
	
	public static DayOneEntry createNewEntry(EntryManager manager) {
		NSDictionary dict = new NSDictionary();
		
		dict.put("UUID", UUID.randomUUID().toString().replace("-", "").toUpperCase());
		
		dict.put("Creation Date", new Date());
		
		NSDictionary creatorDict = new NSDictionary();
	    creatorDict.put("Device Agent","PC");
	    creatorDict.put("Generation Date", new Date());
	    try {
			creatorDict.put("Host Name", InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			creatorDict.put("Host Name", "PC");
		}
	    creatorDict.put("OS Agent", System.getProperty("os.name"));
	    creatorDict.put("Software Agent", "LifeOrganizer");
	    dict.put("Creator", creatorDict);
	    
	    dict.put("Time Zone", System.getProperty("user.timezone"));
	    
	    File file = new File(manager.getEntryFolder(), dict.get("UUID").toString() + ".doentry");
	    
		return new DayOneEntry(manager, dict, file);
	}

	private NSDictionary dictionary;
	
	private EntryManager manager;
	private File file, imageFile;
	private Image image;
	
	private boolean isSaving = false;
	
	private DayOneEntry(EntryManager manager, NSDictionary dictionary, File file) {
		this.manager = manager;
		this.dictionary = dictionary;
		this.file = file;
	}
	
	public void setImageFile(File imageFile) {
		this.imageFile = imageFile;
	}
	
	public void readFile() throws Exception {
		if (!file.canRead())
			return;
		
		dictionary = (NSDictionary) PropertyListParser.parse(file);
	}
	
	public void delete() {
		manager.ignoreForAction(getUUID(), () -> file.delete());
	}
	
	public void save() {
		if (isSaving) return;
		
		new Thread(() ->
			manager.ignoreForAction(getUUID(), () -> {
					try {
						Thread.sleep(3000);
						PropertyListParser.saveAsXML(dictionary, file);
					} catch (Exception e) { 
					} finally {
						isSaving = false;
					}
		})).start();
	
		isSaving = true;
	}
	
	public NSDictionary getDictionary() {
		return dictionary;
	}
	
	public Image getImage() {
		if (image == null && imageFile != null)
			try {
				image = new Image(new FileInputStream(imageFile));
			} catch (FileNotFoundException e) { }
		return image;
	}

	public String getUUID() {
		return dictionary.get("UUID").toString();
	}
	
	public List<String> getTags() {
		List<String> list = new ArrayList<String>();
		for (Object s : (Object[]) dictionary.getOrDefault("Tags", new NSArray(0)).toJavaObject())
			list.add(s.toString());
		return list;
	}
	
	public void setTags(List<String> tags) {
		dictionary.put("Tags", tags);
		save();
	}
	
	public boolean addTag(String tag) {
		List<String> tags = getTags();
		tags.add(tag);
		manager.addTag(tag, this);
		setTags(tags);
		return true;
	}
	
	public boolean removeTag(String tag) {
		List<String> tags = getTags();
		boolean ret = tags.remove(tag);
		manager.removeTag(tag, this);
		setTags(tags);
		return ret;
	}
	
	public String getEntryText() {
		return dictionary.getOrDefault("Entry Text", NSObject.wrap("")).toString();
	}

	public void setEntryText(String text) {
		dictionary.put("Entry Text", text);
		save();
	}
	
	public boolean isStarred() {
		return (Boolean) dictionary.get("Starred").toJavaObject();
	}
	
	public void setStarred(boolean starred) {
		dictionary.put("Starred", starred);
		save();
	}
	
	public DateTime getCreationDate() {
		return new DateTime((Date) dictionary.get("Creation Date").toJavaObject());
	}

	/**
	 * Compares two DayOneEntries for ordering
	 * 
	 *  @return
	 *   the value 0 if the entries were made on the same date, a value greater
	 *   than 0 if this entry was created after the DayOneEntry argument and a 
	 *   value less than 0 if it was created before.
	 */
	@Override
	public int compareTo(DayOneEntry o) {
		return getCreationDate().compareTo(o.getCreationDate());
	}
	
	// Properties
	
	Property<String> entryTextProperty;
	Property<Boolean> starredProperty;
	
	@SuppressWarnings("unchecked")
	public Property<String> entryTextProperty() {
		if (entryTextProperty == null)
			try {
				entryTextProperty = JavaBeanObjectPropertyBuilder.create().bean(this).name("entryText").build();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		return entryTextProperty;
	}
	
	@SuppressWarnings("unchecked")
	public Property<Boolean> starredProperty() {
		if (starredProperty == null)
			try {
				starredProperty = JavaBeanObjectPropertyBuilder.create().bean(this).name("starred").build();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		return starredProperty;
	}
	
}
