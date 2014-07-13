package com.facsu.publicartmap.utils;

import android.content.Context;
import android.content.pm.PackageManager;

public class MapUtils {

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
	 * 判断是否存在支持Google Map API v1
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isSupportGoogleMap(Context context) {
		PackageManager packageManager = context.getPackageManager();
		boolean hasGoogleMapApi = false;
		String[] sharedLibraryNames = packageManager
				.getSystemSharedLibraryNames();
		if (sharedLibraryNames != null) {
			for (String sharedLibraryName : sharedLibraryNames) {
				if ("com.google.android.maps".equals(sharedLibraryName)) {
					try {
						@SuppressWarnings("rawtypes")
						Class cl = Class
								.forName("com.google.android.maps.MapActivity");
						if (cl != null) {
							hasGoogleMapApi = true;
						}
					} catch (Throwable e) {
						hasGoogleMapApi = false;
					}
					break;
				}
			}
		}
		return hasGoogleMapApi;
	}

}
