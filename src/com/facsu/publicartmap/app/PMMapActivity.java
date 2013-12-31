package com.facsu.publicartmap.app;

import android.os.Bundle;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.facsu.publicartmap.R;

public class PMMapActivity extends PMActivity {

	private BMapManager mapManager = null;
	private MapView mapView = null;
	private MapController mapController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_mapview);
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
	}

	public BMapManager mapManager() {
		if (mapManager == null) {
			mapManager = PMApplication.instance().mapManager();
		}
		return mapManager;
	}

	public MapView mapView() {
		return mapView;
	}

	public MapController mapController() {
		if (mapController == null && mapView != null) {
			mapController = mapView.getController();
		}
		return mapController;
	}

	@Override
	public void onDestroy() {
		mapView.destroy();
		super.onDestroy();
	}

	@Override
	public void onPause() {
		mapView.onPause();
		if (mapManager() != null) {
			mapManager().stop();
		}
		super.onPause();
	}

	@Override
	public void onResume() {
		mapView.onResume();
		if (mapManager() != null) {
			mapManager().start();
		}
		super.onResume();
	}
	
}
