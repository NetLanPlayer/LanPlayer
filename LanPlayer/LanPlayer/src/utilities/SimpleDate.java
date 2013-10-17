package utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleDate implements Comparable<SimpleDate> {

	private Date date;
	
	public Date getDate() {
		return date;
	}
	
	public SimpleDate(Date date) {
		this.date = date;
		if(date == null) {
			date = new Date();
		}
	}
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	
	public static String formattedDate(Date date) {
		return sdf.format(date);
	}
	
	public static Date parseDate(String dateStr) {
		try {
			return dateStr == null || dateStr.isEmpty() ? null : sdf.parse(dateStr);
		} catch (ParseException | NumberFormatException e) {
			return null;
		}
	}
	
	public String toString() {
		return formattedDate(this.date);
	}

	@Override
	public int compareTo(SimpleDate o) {
		return this.date.compareTo(o.getDate());
	}
	
}
