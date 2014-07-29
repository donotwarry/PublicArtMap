package com.facsu.publicartmap.utils;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MapUtils {

	private static boolean enableGoogleMap;
	
	public static void setEnableGoogleMap(boolean enable) {
		enableGoogleMap = enable;
	}

	private static final double EARTH_RADIUS = 6378.137;

	public static double getDistance(double lat1, double lng1, double lat2,
			double lng2) {
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000;
		return s;
	}

	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}

	/**
	 * 判断是否存在支持Google Map API v2
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isSupportGoogleMap(Context context) {
		if (!enableGoogleMap) {
			return false;
		}

		return GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
	}

}
