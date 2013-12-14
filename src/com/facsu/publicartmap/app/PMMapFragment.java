package com.facsu.publicartmap.app;

import android.os.Bundle;
import android.view.View;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.facsu.publicartmap.R;

public class PMMapFragment extends PMFragment {

	private BMapManager mapManager = null;
	private MapView mapView = null;
	private MapController mapController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mapManager = new BMapManager(getActivity().getApplicationContext());
		mapManager.init("sRIp5TPXhwtPyI0BwaVpA9U0", null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mapView = (MapView) getView().findViewById(R.id.mapview);
		if (mapView == null) {
			throw new IllegalStateException(
					"You must add a mapView widget with id 'mapview' in the layout.");
		}
		mapView.setBuiltInZoomControls(true);
	}

	public BMapManager mapManager() {
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
		if (mapManager != null) {
			mapManager.destroy();
			mapManager = null;
		}
		super.onDestroy();
	}

	@Override
	public void onPause() {
		mapView.onPause();
		if (mapManager != null) {
			mapManager.stop();
		}
		super.onPause();
	}

	@Override
	public void onResume() {
		mapView.onResume();
		if (mapManager != null) {
			mapManager.start();
		}
		super.onResume();
	}

}
