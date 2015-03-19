package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.ini4j.Ini;

public class Config {

	private File configFile;
	private Ini apiIni;
	private Properties prop;
	
	public Config(File apiFile, File configFile) throws IOException {
		try {
			apiIni = new Ini(apiFile);
		} catch (IOException e) {
			throw new IOException("API File is incorrect");
		}
		prop = new Properties();
		this.configFile = configFile;
		if (!configFile.exists())
			setDefaults();
		else
			prop.load(new FileInputStream(configFile));
	}

	private void setDefaults() throws FileNotFoundException, IOException {
		String def = "$default$";
		
		prop.setProperty("default_folder", new File("data").getAbsolutePath());
		prop.setProperty("todo_file", new File(def, "todo.txt").getPath());
		prop.setProperty("done_file", new File(def, "done.txt").getPath());
		prop.setProperty("sleep_file", new File(def, "sleep.txt").getPath());
		prop.setProperty("locations_file", new File(def, "locations.txt").getPath());
		prop.store(new FileOutputStream(configFile), "");
	}
	
	public Properties getAPIProperties(String provider) {
		Properties p = new Properties();
		
		Iterator<Entry<String, String>> set = apiIni.get(provider).entrySet().iterator();
		while (set.hasNext()) {
			Entry<String, String> e = set.next();
			if (!e.getKey().startsWith("user_"))
				p.put(e.getKey(), e.getValue());
		}
		return p;
	}
	
	public void setAPIUserProperty(String provider, String key, String value) {
		key = "user_" + key;
		apiIni.get(provider).put(key, value);
		try {
			apiIni.store();
		} catch (IOException e) {
			System.out.println("Unable to save");
		}
	}
	
	public Properties getAPIUserProperties(String provider) {
		Properties p = new Properties();
		
		Iterator<Entry<String, String>> set = apiIni.get(provider).entrySet().iterator();
		while (set.hasNext()) {
			Entry<String, String> e = set.next();
			if (e.getKey().startsWith("user_"))
				p.put(e.getKey().replace("user_", ""), e.getValue());
		}
		return p;
	}
	
	public void setDataFolder(String path) {
		prop.setProperty("default_folder", new File(path).getAbsolutePath());	
	}
	
	public File getDataFolder() {
		return new File(prop.getProperty("default_folder"));
	}
	
	public void setFile(String name, String path) {
		name += "_file";
		if (!new File(path).isAbsolute())
			path = new File("$default$", path).getPath();
		prop.setProperty(name, path);
	}
	
	public File getFile(String name) {
		name += "_file";
		String path = prop.getProperty(name);
		if (path.startsWith("$default$"))
			path = path.replace("$default$", prop.getProperty("default_folder"));
		
		return new File(path);
	}

}
