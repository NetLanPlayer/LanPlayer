package utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleDate implements Comparable<SimpleDate> {

	private Date date;
	private String simpleDate;
	
	
	public Date getDate() {
		return date;
	}
	
	public SimpleDate(Date date) {
		this.date = date;
		if(date == null) {
			this.date = new Date();
		}
		simpleDate = formattedDate(this.date);
	}
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	
	private static String formattedDate(Date date) {
		if(date == null) return "";
		return sdf.format(date);
	}
	
	public static Date parseDate(String dateStr) {
		try {
			return dateStr == null || dateStr.isEmpty() ? null : sdf.parse(dateStr);
		} catch (ArrayIndexOutOfBoundsException | ParseException | NumberFormatException e) {
			return null;
		}
	}
	
	public String toString() {
		return simpleDate;
	}

	@Override
	public int compareTo(SimpleDate o) {
		return this.date.compareTo(o.getDate());
	}
	
}
