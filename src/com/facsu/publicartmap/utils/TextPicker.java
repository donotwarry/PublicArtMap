package com.facsu.publicartmap.utils;

import android.content.Context;

import com.facsu.publicartmap.R;

public class TextPicker {
	
	private static final String REG = "#||#";
	
	public static String pick(Context ctx, String source) {
		if (source == null) {
			return "";
		}
		
		if (!source.contains(REG)) {
			return source;
		}
		
		String before = source.substring(0, source.indexOf(REG));
		String after = source.substring(source.indexOf(REG) + REG.length(), source.length());
		
		if (ctx.getResources().getBoolean(R.bool.isEnvZH)) {
			return before;
		} else {
			return after;
		}
	}

}
