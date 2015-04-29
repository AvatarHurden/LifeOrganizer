package io.github.avatarhurden.lifeorganizer.diary.models;

import java.io.File;

import javafx.beans.property.SimpleObjectProperty;

import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;

public class DayOneEntry extends SimpleObjectProperty<DayOneEntry> {

	private NSDictionary dictionary;
	
	private DayOneEntry(NSDictionary dictionary) {
		this.dictionary = dictionary;
	}
	
	public static DayOneEntry loadFromFile(File file) throws Exception {
		return new DayOneEntry((NSDictionary) PropertyListParser.parse(file));
	}
	
	public static DayOneEntry createNewEntry() {
		NSDictionary dict = new NSDictionary();
		
		return new DayOneEntry(dict);
	}
	
	public NSDictionary getDictionary() {
		return dictionary;
	}
	
	
}
