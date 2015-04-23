package io.github.avatarhurden.lifeorganizer.objects;

import java.util.Locale;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;

import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;

public class DueDate {
	
	DateTime dateTime;
	Property<DateTime> dateTimeProperty;
	Property<Boolean> hasTime;
	
	public DueDate(DateTime dateTime, boolean hasTime) {
		super();
		setHasTime(hasTime);
		setDateTime(dateTime);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DueDate() {
		try {
			this.dateTimeProperty = new JavaBeanObjectPropertyBuilder().bean(this).name("dateTime").build();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	
	public void setDateTime(DateTime dateTime) {
		this.dateTime = hasTime.getValue() ? dateTime : dateTime.secondOfDay().setCopy(0);
	}
	
	public DateTime getDateTime() {
		return dateTime;
	}
	
	public Property<DateTime> dateTimeProperty() {
		return dateTimeProperty;
	}
	
	public boolean getHasTime() {
		return hasTime.getValue();
	}
	
	public void setHasTime(boolean hasTime) {
		if (this.hasTime == null)
			this.hasTime = new SimpleBooleanProperty(hasTime);
		this.hasTime.setValue(hasTime);
		if (!hasTime && dateTime != null)
			setDateTime(getDateTime()); // If time is removed, fix the date
	}
	
	public Property<Boolean> hasTimeProperty() {
		return hasTime;
	}
	
	public String getPrettyTime() {
		if (hasTime.getValue())
			return new PrettyTime(Locale.US).format(dateTime.toDate());
		
		String format = "dd/MM/YYYY";
		if (dateTime.toString(format).equals(new DateTime().toString(format)))
			return "today";
		if (dateTime.toString(format).equals(new DateTime().plusDays(1).toString(format)))
			return "tomorrow";
		if (dateTime.toString(format).equals(new DateTime().minusDays(1).toString(format)))
			return "yesterday";
	
		PrettyTime time = new PrettyTime(new DateTime().secondOfDay().setCopy(0).toDate(), Locale.US);
		return time.format(dateTime.toDate());
	}
	
}
