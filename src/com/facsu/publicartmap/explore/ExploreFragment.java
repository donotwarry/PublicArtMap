package com.facsu.publicartmap.explore;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.MyLocationOverlay.LocationMode;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.dennytech.common.service.dataservice.mapi.CacheType;
import com.dennytech.common.service.dataservice.mapi.MApiRequest;
import com.dennytech.common.service.dataservice.mapi.MApiRequestHandler;
import com.dennytech.common.service.dataservice.mapi.MApiResponse;
import com.dennytech.common.service.dataservice.mapi.impl.BasicMApiRequest;
import com.dennytech.common.util.Log;
import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMApplication;
import com.facsu.publicartmap.app.PMMapFragment;
import com.facsu.publicartmap.bean.Artwork;
import com.facsu.publicartmap.bean.GetArtworksByGPSResult;
import com.facsu.publicartmap.bean.Location;
import com.facsu.publicartmap.bean.User;
import com.facsu.publicartmap.common.LocationListener;
import com.facsu.publicartmap.utils.MapUtils;
import com.facsu.publicartmap.utils.TextPicker;
import com.facsu.publicartmap.widget.PopupView;

public class ExploreFragment extends PMMapFragment implements LocationListener,
		OnClickListener, MKMapViewListener, MApiRequestHandler {
	
	private static final int REQUEST_CODE_SHARE = 1;

	private View rootView;
	private Location myLoc;
	private MyLocationOverlay myLocationOverlay;
	private MyOverlay resultOverlay;
	private PopupOverlay pop;
	private PopupView popView;
	private View progress;

	boolean isRequest = false;
	boolean isFirstLoc = true;
	boolean isClickOnPop = false;

	private MApiRequest request;
	private Artwork[] data;
	private Artwork curArtwork;
	private GeoPoint curGP;

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
		progress = rootView.findViewById(R.id.progressbar);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setLeftButton(R.drawable.title_referesh, this);
		setRightButton(R.drawable.title_add, this);

		mapView().getController().setZoom(14);
		mapView().getController().enableClick(true);
		mapView().setBuiltInZoomControls(true);
		mapView().regMapViewListener(mapManager(), this);

		PopupClickListener popListener = new PopupClickListener() {

			@Override
			public void onClickedPopup(int index) {
				if (curArtwork == null) {
					return;
				}

				Artwork aw = curArtwork;
				Intent intent = new Intent(Intent.ACTION_VIEW,
						Uri.parse("pam://artworkinfo?id=" + aw.ArtworkID));
				if (myLoc != null) {
					intent.putExtra("location", myLoc);
				}
				startActivity(intent);
				curArtwork = null;
			}

		};
		pop = new PopupOverlay(mapView(), popListener);
		popView = (PopupView) getActivity().getLayoutInflater().inflate(
				R.layout.layout_pop, null);

		myLocationOverlay = new MyLocationOverlay(mapView());
		myLoc = PMApplication.instance().myLocation();
		if (myLoc != null) {
			onReceiveLocation(myLoc);
		} else {
			PMApplication.instance().addLocationListener(this);
		}

		mapView().getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableCompass();
		mapView().refresh();

		if (myLoc != null) {
			new Handler() {
				public void handleMessage(android.os.Message msg) {
					refereshData();
				};
			}.sendEmptyMessageDelayed(0, 1200);
		}
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
	public void onDestroy() {
		if (request != null) {
			mapiService().abort(request, this, true);
		}
		super.onDestroy();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_SHARE && resultCode == Activity.RESULT_OK) {
			Toast.makeText(getActivity(), "aaaaaa", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.title_left_btn) {
			refereshData();

		} else if (v.getId() == R.id.title_right_btn) {
			User user = User.read(preferences());
			if (user == null) {
				gotoLogin(null);
			} else {
				Intent i = new Intent(Intent.ACTION_VIEW,
						Uri.parse("pam://share"));
				if (myLoc != null) {
					i.putExtra("location", myLoc);
				}
				startActivityForResult(i, REQUEST_CODE_SHARE);
			}

		} else if (v.getId() == R.id.mylocation) {
			if (myLoc != null) {
				mapController().animateTo(
						new GeoPoint((int) (myLoc.latitude * 1e6),
								(int) (myLoc.longitude * 1e6)));
				Toast.makeText(getActivity(),
						myLoc.latitude + ", " + myLoc.longitude,
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getActivity(), getString(R.string.msg_locating),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void loadData(double lat, double lng, int rangeKM) {
		progress.setVisibility(View.VISIBLE);
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
			OverlayItem item = new OverlayItem(gp, TextPicker.pick(
					getActivity(), artwork.ArtworkName), "");
			resultOverlay.addItem(item);
		}
		
		mapView().refresh();
	}

	@Override
	public void onReceiveLocation(Location location) {
		if (location == null
				|| (location.latitude == 0 && location.longitude == 0))
			return;
		this.myLoc = location;
		LocationData locData = new LocationData();
		locData.latitude = location.latitude;
		locData.longitude = location.longitude;
		myLocationOverlay.setData(locData);
		mapView().refresh();
		if (isRequest || isFirstLoc) {
			Log.d("LocationOverlay", "receive location, animate to it");
			mapController().animateTo(
					new GeoPoint((int) (location.latitude * 1e6),
							(int) (location.longitude * 1e6)));
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
		if (isClickOnPop) {
			pop.showPopup(popView, curGP, 32);
		} else {
			pop.hidePop();
		}
		isClickOnPop = false;
		refereshData();
	}

	@Override
	public void onMapLoadFinish() {
	}

	@Override
	public void onMapMoveFinish() {
		if (isClickOnPop) {
			pop.showPopup(popView, curGP, 32);
		} else {
			pop.hidePop();
		}
		isClickOnPop = false;
		refereshData();
	}

	private void refereshData() {
		GeoPoint cp = mapView().getMapCenter();
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
		progress.setVisibility(View.GONE);
		Toast.makeText(getActivity(), resp.message().getErrorMsg(),
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onRequestFinish(MApiRequest req, MApiResponse resp) {
		progress.setVisibility(View.GONE);
		if (resp.result() instanceof GetArtworksByGPSResult) {
			GetArtworksByGPSResult result = (GetArtworksByGPSResult) resp
					.result();
			data = result.result();
			showData();
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
			pop.hidePop();
			Artwork aw = data[index];
			curArtwork = aw;
			popView.setData(aw, myLoc);
			curGP = new GeoPoint((int) (Double.valueOf(aw.Latitude) * 1E6),
					(int) (Double.valueOf(aw.Longitude) * 1E6));
			mapController().animateTo(curGP);
			isClickOnPop = true;
			return true;
		}

		@Override
		public boolean onTap(GeoPoint pt, MapView mMapView) {
			if (pop != null) {
				pop.hidePop();
				mapView().removeView(popView);
				curArtwork = null;
			}
			return false;
		}

	}
}
