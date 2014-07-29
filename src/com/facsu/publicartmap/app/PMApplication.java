package com.facsu.publicartmap.app;

import java.util.ArrayList;
import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.dennytech.common.app.CLApplication;
import com.facsu.publicartmap.bean.Location;
import com.facsu.publicartmap.common.LocationListener;
import com.facsu.publicartmap.utils.MapUtils;
import com.umeng.analytics.MobclickAgent;

public class PMApplication extends CLApplication implements BDLocationListener{
	
	private static PMApplication instance;
	
	public PMApplication() {
		instance = this;
	}

	public static PMApplication instance() {
		if (instance == null) {
			throw new IllegalStateException("Application has not been created");
		}

		return instance;
	}
	
	private BMapManager mapManager = null;
	
	public BMapManager mapManager() {
		if (mapManager == null) {
			mapManager = new BMapManager(this);
			mapManager.init("sRIp5TPXhwtPyI0BwaVpA9U0", null);
			mapManager.start();
		}
		return mapManager;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		MobclickAgent.updateOnlineConfig(this);
		String value = MobclickAgent.getConfigParams(this, "enableGoogleMap");
		MapUtils.setEnableGoogleMap(Boolean.valueOf(value));
	}
	
	@Override
	public void onApplicationStart() {
		super.onApplicationStart();
		startLocate();
	}
	
	@Override
	public void onApplicationStop() {
		if (mapManager != null) {
			mapManager.destroy();
		}
		stopLocate();
		super.onApplicationStop();
	}
	
	
	private LocationClient locClient;
	private Location myLocation;
	private List<LocationListener> locationListenerList = new ArrayList<LocationListener>();
	
	public Location myLocation() {
		return myLocation;
	}
	
	private void startLocate() {
		locClient = new LocationClient(this);
		locClient.registerLocationListener(this);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setAddrType("all");
		
		if (MapUtils.isSupportGoogleMap(this)) {
			option.setCoorType("gcj02");//google
		} else {
			option.setCoorType("bd09ll");// baidu
		}
		
		option.setPriority(LocationClientOption.NetWorkFirst);
		option.setScanSpan(60000);
		locClient.setLocOption(option);
		locClient.start();
	}
	
	private void stopLocate() {
		if (locClient != null) {
			locClient.stop();
		}
	}
	
	public void addLocationListener(LocationListener l) {
		locationListenerList.add(l);
	}
	
	public void removeLocationListener(LocationListener l) {
		locationListenerList.remove(l);
	}

	@Override
	public void onReceiveLocation(BDLocation location) {
		if (location == null
				|| (location.getLatitude() == 0 && location.getLongitude() == 0))
			return;

		this.myLocation = new Location(location.getAddrStr(),
				location.getLatitude(), location.getLongitude(),
				location.getCity());
		
		for (LocationListener l : locationListenerList) {
			l.onReceiveLocation(myLocation);
		}
	}

	@Override
	public void onReceivePoi(BDLocation arg0) {
	}

}
