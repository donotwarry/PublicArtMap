package com.facsu.publicartmap.common;

import android.text.TextUtils;

public class Environment {
	
	private static String userID;
	
	public static String userID() {
		return TextUtils.isEmpty(userID) ? "123" : userID;
	}
	
	public static void setUserID(String id) {
		userID = id;
	}

}
