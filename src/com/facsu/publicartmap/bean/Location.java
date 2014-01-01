package com.facsu.publicartmap.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class Location implements Parcelable {

	public String address;
	public double latitude;
	public double longitude;
	public String city;
	
	public Location(String address, double latitude, double longitude, String city) {
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
		this.city = city;
	}

	public Location(Parcel in) {
		address = in.readString();
		latitude = in.readDouble();
		longitude = in.readDouble();
		city = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(address);
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
		dest.writeString(city);
	}

	public static final Parcelable.Creator<Location> CREATOR = new Creator<Location>() {

		@Override
		public Location[] newArray(int size) {
			return new Location[size];
		}

		@Override
		public Location createFromParcel(Parcel source) {
			return new Location(source);
		}
	};

}
