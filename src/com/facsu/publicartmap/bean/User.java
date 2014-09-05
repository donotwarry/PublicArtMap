package com.facsu.publicartmap.bean;

import android.content.SharedPreferences;

public class User {

	public int UID = -1;
	public String UserName;
	public String Password;
	public String AvatarUrl;
	public String Signature;
	public String Role;
	public String CreationDate;
	public String ThirdID;
	public String ThirdType;

	public User(int uid, String name, String avatar) {
		this.UID = uid;
		this.UserName = name;
		this.AvatarUrl = avatar;
	}

	public void save(SharedPreferences pref) {
		if (UID != -1) {
			pref.edit().putInt("uid", UID).putString("uname", UserName)
					.putString("uavatar", AvatarUrl)
					.putString("usignature", Signature)
					.putString("urole", Role).commit();
		}
	}

	public static User read(SharedPreferences pref) {
		if (pref.getInt("uid", -1) != -1) {
			return new User(pref.getInt("uid", -1), pref.getString("uname",
					null), pref.getString("uavatar", null));
		}
		return null;
	}

	public static void remove(SharedPreferences pref) {
		pref.edit().remove("uid").remove("uname").remove("uavatar")
				.remove("usignature").remove("urole").commit();
	}
}
