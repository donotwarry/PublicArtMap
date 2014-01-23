package com.facsu.publicartmap.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtils {
	
	public static String format(String serverTime) {
		String result = serverTime.replace("/Date(", "").replace(")/", "");
		if (result.contains("+")) {
			result = result.substring(0, result.indexOf("+"));
		}
		
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Long.valueOf(result));
		
		return formatter.format(calendar.getTime());
	}

}
