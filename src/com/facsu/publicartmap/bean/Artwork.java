package com.facsu.publicartmap.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class Artwork implements Parcelable{

	public String Address;
	public String Artist;
	public String ArtistCountry;
	public String ArtworkDesc;
	public String ArtworkID;
	public String ArtworkName;
	public String City;
	public String Country;
	public String CreationDate;
	public String EndMonth;
	public String EndYear;
	public String Latitude;
	public String Longitude;
	public int RetweetCount;
	public String SerialNum;
	public String StartMonth;
	public String StartYear;
	public String Status;
	public String SubmitterID;
	public String SubmitterName;
	public String Type;
	public int VoteCount;
	
	public Artwork(Parcel source) {
		Address = source.readString();
		Artist = source.readString();
		ArtistCountry = source.readString();
		ArtworkDesc = source.readString();
		ArtworkID = source.readString();
		ArtworkName = source.readString();
		City = source.readString();
		Country = source.readString();
		CreationDate = source.readString();
		EndMonth = source.readString();
		EndYear = source.readString();
		Latitude = source.readString();
		Longitude = source.readString();
		RetweetCount = source.readInt();
		SerialNum = source.readString();
		StartMonth = source.readString();
		StartYear = source.readString();
		Status = source.readString();
		SubmitterID = source.readString();
		SubmitterName = source.readString();
		Type = source.readString();
		VoteCount = source.readInt();
    }  
	
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(Address);
		dest.writeString(Artist);
		dest.writeString(ArtistCountry);
		dest.writeString(ArtworkDesc);
		dest.writeString(ArtworkID);
		dest.writeString(ArtworkName);
		dest.writeString(City);
		dest.writeString(Country);
		dest.writeString(CreationDate);
		dest.writeString(EndMonth);
		dest.writeString(EndYear);
		dest.writeString(Latitude);
		dest.writeString(Longitude);
		dest.writeInt(RetweetCount);
		dest.writeString(SerialNum);
		dest.writeString(StartMonth);
		dest.writeString(StartYear);
		dest.writeString(Status);
		dest.writeString(SubmitterID);
		dest.writeString(SubmitterName);
		dest.writeString(Type);
		dest.writeInt(VoteCount);
	}
	
	public static final Parcelable.Creator<Artwork> CREATOR = new Creator<Artwork>() {  
        
        @Override  
        public Artwork[] newArray(int size) {  
            return new Artwork[size];  
        }  
          
        @Override  
        public Artwork createFromParcel(Parcel source) {  
            return new Artwork(source);  
        }  
    };  

}
