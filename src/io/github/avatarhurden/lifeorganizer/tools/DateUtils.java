package io.github.avatarhurden.lifeorganizer.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateUtils {

	public static Duration toToday(DateTime from) {
		return new Duration(new DateTime().getMillis() - from.getMillis());
	}
	
	/**
	 * Parses the string in one of the provided formats, in order. If none match, returns null.
	 */
	public static DateTime parseDateTime(String t, String... formats) {
		DateTimeFormatter[] formatters = Arrays.stream(formats).map(s -> DateTimeFormat.forPattern(s)).toArray(DateTimeFormatter[]::new);
		
		for (DateTimeFormatter f : formatters)
			try {
				return f.parseDateTime(t);
			} catch (Exception e) {}
		return null;
	}
	
	/**
	 * <p>Tries to parse <code>t</code> as any incremental subset of the formats:
	 * <p><code>dd/MM/YYYY</code>
	 * <p><code>dd.MM.YYYY</code>
	 * <p><code>dd-MM-YYYY</code>
	 * <p>It can also handle time added to the end of any of the previous formats, following any incremental subset of the format:
	 * <p><code>@HH:mm</code>
	 * 
	 * <p>After the string is parsed, the parts of the format that were not provided by the string are filled in with the values
	 * provided by the <code>defaults</code> map.
	 * <p>When this is done, the remaining parts are filled with the current date and time. This allows the string <b>3</b> to be
	 * interpreted as the day 3 of the current month and year.
	 * 
	 * @param string The String to be parsed
	 * @param defaults Values that will be set if they are not provided by the string.
	 * <p>The accepted keys are <code>"d", "M", "Y", "H", "m", "s", "ms"</code>, for day, month,
	 * year, hour, minute, second and millisecond, respectively.
	 * 
	 * @return
	 */
	public static DateTime parseMultiFormat(String string, Map<String, Integer> defaults) {
		boolean accepted = false;
		MutableDateTime now = new MutableDateTime();
		
		ArrayList<String> formats = new ArrayList<String>();
		for (String sep :  new String[]{".", "/", "-"}) {
			formats.add("d{sep}M{sep}Y@H:m".replace("{sep}", sep));
			formats.add("d{sep}M{sep}Y@H".replace("{sep}", sep));
			formats.add("d{sep}M{sep}Y".replace("{sep}", sep));
			formats.add("d{sep}M@H:m".replace("{sep}", sep));
			formats.add("d{sep}M@H".replace("{sep}", sep));
			formats.add("d{sep}M".replace("{sep}", sep));
		}
		formats.add("d@H:m");
		formats.add("d@H");
		formats.add("d");
		formats.add("@H:m");
		formats.add("@H");
		
		List<Consumer<MutableDateTime>> deltas = new ArrayList<Consumer<MutableDateTime>>();
		if (string.startsWith("tom")) { // If string starts with "tod" or "tom", sets the day accordingly
			deltas.add(date -> date.setDayOfMonth(DateTime.now().getDayOfMonth() + 1));
			accepted = true;
			string = string.replace("tom", "");
		} else if (string.startsWith("tod")) {
			deltas.add(date -> date.setDayOfMonth(DateTime.now().getDayOfMonth()));
			accepted = true;
			string = string.replace("tod", "");
		} else {
			// If there is a "+num", gets the last char to know what unit to increment and increments the amount on the final date
			Matcher p = Pattern.compile("\\+([0-9]*)([dwmyhDWMYH])").matcher(string);
			
			while (p.find()) {
				accepted = true;
				int number = Integer.parseInt(p.group(1));
				char pattern = p.group(2).toLowerCase().charAt(0);
				string = string.replace(p.group(), "");
				
				if (pattern == 'd') deltas.add(date -> date.addDays(number));
				else if (pattern == 'w') deltas.add(date -> date.addWeeks(number));
				else if (pattern == 'm') deltas.add(date -> date.addMonths(number));
				else if (pattern == 'y') deltas.add(date -> date.addYears(number));
				else if (pattern == 'h') deltas.add(date -> date.addHours(number));
			
			}
		}
		
		for (String p : formats)
			try {
				DateTimeFormat.forPattern(p).parseDateTime(string); // Accepts the first result that is parsed
				String[] parts = p.split("[.-/@:]");
				String[] values = string.split("[.-/@:]");
				for (int i = 0; i < parts.length; i++) {
					     if (parts[i].equals("Y")) now.setYear(Integer.valueOf(values[i]));
					else if (parts[i].equals("M")) now.setMonthOfYear(Integer.valueOf(values[i]));
					else if (parts[i].equals("d")) now.setDayOfMonth(Integer.valueOf(values[i]));
					else if (parts[i].equals("H")) now.setHourOfDay(Integer.valueOf(values[i]));
					else if (parts[i].equals("m")) now.setMinuteOfHour(Integer.valueOf(values[i]));
				}
				accepted = true;
				break;
			} catch (Exception e)  {}

		if (!accepted)
			return null;
		
		for (Consumer<MutableDateTime> delta : deltas)
			delta.accept(now); // Applies the delta that was defined earlier
				
		if (defaults != null) // Applies the default values
			for (Entry<String, Integer> pair : defaults.entrySet())
					 if (pair.getKey().equals("Y")) now.setYear(pair.getValue());
				else if (pair.getKey().equals("M")) now.setMonthOfYear(pair.getValue());
				else if (pair.getKey().equals("d")) now.setDayOfMonth(pair.getValue());
				else if (pair.getKey().equals("H")) now.setHourOfDay(pair.getValue());
				else if (pair.getKey().equals("m")) now.setMinuteOfHour(pair.getValue());
				else if (pair.getKey().equals("s")) now.setSecondOfMinute(pair.getValue());
				else if (pair.getKey().equals("ms")) now.setMillisOfSecond(pair.getValue());
		
		// "now" has all the values that were provided by the user, and the ones that were not modified are
		// already the ones that correspond to the current date and time.
		return now.toDateTime();
	}
	
	public static String format(DateTime time, String format) {
		DateTimeFormatter timeFormat = DateTimeFormat.forPattern(format);
		
		return time.toString(timeFormat);
	}
}
