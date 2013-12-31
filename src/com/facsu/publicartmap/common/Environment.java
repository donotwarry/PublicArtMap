package com.facsu.publicartmap.common;

import android.text.TextUtils;

public class Environment {

	private static String userID;

	public static String userID() {
		return TextUtils.isEmpty(userID) ? "-1" : userID;
	}

	public static void setUserID(String id) {
		userID = id;
	}

	private static String userName;

	public static String userName() {
		return TextUtils.isEmpty(userName) ? "游客" : userName;
	}

	public static void setUserName(String name) {
		userName = name;
	}

}
