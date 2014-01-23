package com.facsu.publicartmap.common;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.facsu.publicartmap.bean.User;

public class Environment {

	private static int userID;
	private static String userName;
	private static String userAvatar;

	private static User user;

	public static int userID() {
		return userID;
	}

	public static void setUserID(int id) {
		userID = id;
	}

	public static String userName() {
		return TextUtils.isEmpty(userName) ? "游客" : userName;
	}

	public static void setUserName(String name) {
		userName = name;
	}

	public static String userAvatar() {
		return userAvatar;
	}

	public static void setUserAvatar(String avatar) {
		userAvatar = avatar;
	}

	public static User user() {
		if (user == null) {
			user = new User(userID, userName, userAvatar);
		}
		return user;
	}

	public static String getSinaAvatar(SharedPreferences pref, String name) {
		String sinaName = pref.getString("sinaname", "");
		if (sinaName.equals(name)) {
			return pref.getString("sinaavatar", null);
		} else {
			return null;
		}
	}

	public static void saveSinaAvatar(SharedPreferences pref, String name,
			String avatar) {
		pref.edit().putString("sinaname", name).putString("sinaavatar", avatar)
				.commit();
	}

	public static String getQQWeiboAvatar(SharedPreferences pref, String name) {
		String sinaName = pref.getString("qqweiboname", "");
		if (sinaName.equals(name)) {
			return pref.getString("qqweiboavatar", null);
		} else {
			return null;
		}
	}

	public static void saveQQWeiboAvatar(SharedPreferences pref, String name,
			String avatar) {
		pref.edit().putString("qqweiboname", name)
				.putString("qqweiboavatar", avatar).commit();
	}
	
	public static String getQQAvatar(SharedPreferences pref, String name) {
		String qqName = pref.getString("qqname", "");
		if (qqName.equals(name)) {
			return pref.getString("qqavatar", null);
		} else {
			return null;
		}
	}

	public static void saveQQAvatar(SharedPreferences pref, String name,
			String avatar) {
		pref.edit().putString("qqname", name)
				.putString("qqavatar", avatar).commit();
	}

}
