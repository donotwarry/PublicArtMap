package com.facsu.publicartmap.common;

import android.content.SharedPreferences;

public class Environment {

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
