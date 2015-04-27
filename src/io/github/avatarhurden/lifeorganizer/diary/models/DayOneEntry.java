package io.github.avatarhurden.lifeorganizer.diary.models;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;

public class DayOneEntry {

	public DayOneEntry(File file) throws IOException, PropertyListFormatException, ParseException, ParserConfigurationException, SAXException {
		
		NSDictionary dic = (NSDictionary) PropertyListParser.parse(file);
		
		for (Entry<String, NSObject> obs : dic.entrySet())
			if (obs.getValue() instanceof NSDictionary)
				for (Entry<String, NSObject> obs2 : ((NSDictionary) obs.getValue()).entrySet())
					System.out.println(obs2.getKey() + ": " +obs2.getValue().toString());
			else
				System.out.println(obs.getKey() + ": " +obs.getValue().toString());
	}
	
}
