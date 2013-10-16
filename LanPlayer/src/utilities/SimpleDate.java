package utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleDate implements Comparable<SimpleDate> {

	private Date dat;
	
	public Date getDate() {
		return dat;
	}
	
	public SimpleDate(Date date) {
		this.dat = date;
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
		} catch (ParseException e) {
			return null;
		}
	}
	
	public String toString() {
		return formattedDate(this.dat);
	}

	@Override
	public int compareTo(SimpleDate o) {
		return this.dat.compareTo(o.getDate());
	}
	
}
