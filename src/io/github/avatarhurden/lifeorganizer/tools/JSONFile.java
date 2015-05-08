package io.github.avatarhurden.lifeorganizer.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONFile {

	public static JSONObject loadJSONObject(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuffer buffer = new StringBuffer();
		
		int read;
		while ((read = reader.read()) != -1)
			buffer.append((char) read);
		reader.close();
		
		return new JSONObject(buffer.toString());
	}
	
	public static JSONArray loadJSONArray(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuffer buffer = new StringBuffer();
		
		int read;
		while ((read = reader.read()) != -1)
			buffer.append((char) read);
		reader.close();
		
		return new JSONArray(buffer.toString());
	}
	
	public static void saveJSONObject(JSONObject object, File f, int indent) throws JSONException, IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(f));
		writer.write(object.toString(indent));
		writer.close();
	}
	
	public static void saveJSONArray(JSONArray array, File f, int indent) throws JSONException, IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(f));
		writer.write(array.toString(indent));
		writer.close();
	}
	
}
