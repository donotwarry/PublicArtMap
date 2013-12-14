package com.facsu.publicartmap.explore;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.MyLocationOverlay.LocationMode;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.dennytech.common.service.dataservice.mapi.CacheType;
import com.dennytech.common.service.dataservice.mapi.MApiRequest;
import com.dennytech.common.service.dataservice.mapi.MApiRequestHandler;
import com.dennytech.common.service.dataservice.mapi.MApiResponse;
import com.dennytech.common.service.dataservice.mapi.impl.BasicMApiRequest;
import com.dennytech.common.util.Log;
import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMMapFragment;
import com.facsu.publicartmap.bean.Artwork;
import com.facsu.publicartmap.bean.GetArtworksByGPSResult;
import com.facsu.publicartmap.utils.MapUtils;
import com.facsu.publicartmap.widget.PopupView;

public class ExploreFragment extends PMMapFragment implements
		BDLocationListener, OnClickListener, MKMapViewListener,
		MApiRequestHandler {

	private View rootView;
	private LocationClient locClient;
	private LocationData locData;
	private MyLocationOverlay myLocationOverlay;
	private MyOverlay resultOverlay;
	private PopupOverlay pop;
	private PopupView popView;

	boolean isRequest = false;// 是否手动触发请求定位
	boolean isFirstLoc = true;// 是否首次定位

	private MApiRequest request;
	private Artwork[] data;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_explore, null);
			rootView.findViewById(R.id.mylocation).setOnClickListener(this);
		}
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mapView().getController().setZoom(14);
		mapView().getController().enableClick(true);
		mapView().setBuiltInZoomControls(true);
		mapView().regMapViewListener(mapManager(), this);

		pop = new PopupOverlay(mapView(), null);
		popView = (PopupView) getActivity().getLayoutInflater().inflate(
				R.layout.layout_pop, null);

		// 定位初始化
		locClient = new LocationClient(getActivity());
		locData = new LocationData();
		locClient.registerLocationListener(this);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setCoorType("bd09ll");
		option.setScanSpan(60000);
		locClient.setLocOption(option);
		locClient.start();
		Toast.makeText(getActivity(), getString(R.string.msg_locating),
				Toast.LENGTH_SHORT).show();

		myLocationOverlay = new MyLocationOverlay(mapView());
		myLocationOverlay.setData(locData);
		mapView().getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableCompass();
		mapView().refresh();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		super.onResume();
		setTitle(R.string.title_explore);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.mylocation) {
			if (locData != null) {
				mapController().animateTo(
						new GeoPoint((int) (locData.latitude * 1e6),
								(int) (locData.longitude * 1e6)));
				Toast.makeText(getActivity(),
						locData.latitude + ", " + locData.longitude,
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getActivity(), getString(R.string.msg_locating),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void loadData(double lat, double lng, int rangeKM) {
		if (request != null) {
			mapiService().abort(request, this, true);
		}
		request = BasicMApiRequest.mapiGet(
				"http://web358082.dnsvhost.com/ACservice/ACService.svc/GetArtworksByGPS/"
						+ lng + "/" + lat + "/" + rangeKM, CacheType.NORMAL,
				GetArtworksByGPSResult.class);
		mapiService().exec(request, this);

		Log.i("explore", "load data [lat:" + lat + " lng:" + lng + " range:"
				+ rangeKM + "]");
	}

	private void showData() {
		if (resultOverlay == null) {
			resultOverlay = new MyOverlay(getResources().getDrawable(
					R.drawable.icon_marka), mapView());
			mapView().getOverlays().add(resultOverlay);
		} else {
			resultOverlay.removeAll();
		}
		
		for (Artwork artwork : data) {
			GeoPoint gp = new GeoPoint(
					(int) (Double.valueOf(artwork.Latitude) * 1E6),
					(int) (Double.valueOf(artwork.Longitude) * 1E6));
			OverlayItem item = new OverlayItem(gp, artwork.ArtworkName, "");
			resultOverlay.addItem(item);
		}
		mapView().refresh();
	}

	@Override
	public void onReceiveLocation(BDLocation location) {
		if (location == null
				|| (location.getLatitude() == 0 && location.getLongitude() == 0))
			return;

		locData.latitude = location.getLatitude();
		locData.longitude = location.getLongitude();
		locData.accuracy = location.getRadius();
		locData.direction = location.getDerect();
		myLocationOverlay.setData(locData);
		mapView().refresh();
		if (isRequest || isFirstLoc) {
			// 移动地图到定位点
			Log.d("LocationOverlay", "receive location, animate to it");
			mapController().animateTo(
					new GeoPoint((int) (locData.latitude * 1e6),
							(int) (locData.longitude * 1e6)));
			isRequest = false;
			myLocationOverlay.setLocationMode(LocationMode.NORMAL);
		}
		isFirstLoc = false;
	}

	public void onReceivePoi(BDLocation poiLocation) {
		if (poiLocation == null) {
			return;
		}
	}

	@Override
	public void onClickMapPoi(MapPoi arg0) {
	}

	@Override
	public void onGetCurrentMap(Bitmap arg0) {
	}

	@Override
	public void onMapAnimationFinish() {
		refereshData();
	}

	@Override
	public void onMapLoadFinish() {
	}

	@Override
	public void onMapMoveFinish() {
		refereshData();
	}

	private void refereshData() {
		GeoPoint cp = mapView().getMapCenter();
		// 当前纬线的跨度（从地图的上边缘到下边缘）
		int tbSpan = mapView().getLatitudeSpan();
		double clat = (double) cp.getLatitudeE6() / 1e6;
		double clng = (double) cp.getLongitudeE6() / 1e6;
		double tlat = (double) (cp.getLatitudeE6() - tbSpan / 2) / 1e6;
		double tlng = clng;
		int disKM = (int) (MapUtils.getDistance(clat, clng, tlat, tlng));
		loadData(clat, clng, disKM);
	}

	@Override
	public void onRequestFailed(MApiRequest req, MApiResponse resp) {
	}

	@Override
	public void onRequestFinish(MApiRequest req, MApiResponse resp) {
		if (resp.result() instanceof GetArtworksByGPSResult) {
			GetArtworksByGPSResult result = (GetArtworksByGPSResult) resp
					.result();
			data = result.list();
			showData();
			Toast.makeText(getActivity(), "success " + result.list().length,
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onRequestProgress(MApiRequest arg0, int arg1, int arg2) {
	}

	@Override
	public void onRequestStart(MApiRequest arg0) {
	}

	public class MyOverlay extends ItemizedOverlay {

		public MyOverlay(Drawable defaultMarker, MapView mapView) {
			super(defaultMarker, mapView);
		}

		@Override
		public boolean onTap(int index) {
			Artwork aw = data[index];
			popView.setData(aw, locData);
			GeoPoint pt = new GeoPoint(
					(int) (Double.valueOf(aw.Latitude) * 1E6),
					(int) (Double.valueOf(aw.Longitude) * 1E6));
			// 弹出自定义View
			pop.showPopup(popView, pt, 32);
			return true;
		}

		@Override
		public boolean onTap(GeoPoint pt, MapView mMapView) {
			if (pop != null) {
				pop.hidePop();
				mapView().removeView(popView);
			}
			return false;
		}

	}
}
