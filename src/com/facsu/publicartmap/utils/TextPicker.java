package com.facsu.publicartmap.utils;

import android.content.Context;

import com.facsu.publicartmap.R;

public class TextPicker {
	
	private static final String REG = "#||#";
	
	public static String pick(Context ctx, String source) {
		if (!source.contains(REG)) {
			return source;
		}
		
		String[] result = source.split(REG);
		if (result.length != 2) {
			return source;
		}
		
		if (ctx.getResources().getBoolean(R.bool.isEnvZH)) {
			return result[0];
		} else {
			return result[1];
		}
	}

}
