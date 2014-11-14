package com.facsu.publicartmap.explore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baidu.mapapi.map.LocationData;
import com.dennytech.common.service.dataservice.mapi.CacheType;
import com.dennytech.common.service.dataservice.mapi.MApiRequest;
import com.dennytech.common.service.dataservice.mapi.MApiRequestHandler;
import com.dennytech.common.service.dataservice.mapi.MApiResponse;
import com.dennytech.common.service.dataservice.mapi.impl.BasicMApiRequest;
import com.dennytech.common.util.Log;
import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMApplication;
import com.facsu.publicartmap.app.PMFragment;
import com.facsu.publicartmap.bean.Artwork;
import com.facsu.publicartmap.bean.GetArtworksByGPSResult;
import com.facsu.publicartmap.bean.Location;
import com.facsu.publicartmap.bean.User;
import com.facsu.publicartmap.common.LocationListener;
import com.facsu.publicartmap.utils.MapUtils;
import com.facsu.publicartmap.widget.PopupView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ExploreGMapFragment extends PMFragment implements
		LocationListener, OnClickListener, MApiRequestHandler,
		OnMyLocationButtonClickListener, OnMarkerClickListener,
		OnCameraChangeListener, OnInfoWindowClickListener {

	private static final int DEFAULT_ZOOM_LEVEL = 12;
	private static final int REQUEST_CODE_SHARE = 1;

	private GoogleMap map;
	private View rootView;
	private View progress;

	boolean isRequest = false;
	boolean isFirstLoc = true;
	boolean isClickOnPop = false;

	private MApiRequest request;
	private List<Artwork> data = new ArrayList<Artwork>();
	private Location myLoc;
	private List<Artwork> shares = new ArrayList<Artwork>();
	private Marker curMarker;

	private Map<Marker, Artwork> markerMap = new HashMap<Marker, Artwork>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_explore_gmap, null);
			rootView.findViewById(R.id.mylocation).setOnClickListener(this);
		}
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}
		map = ((SupportMapFragment) getActivity().getSupportFragmentManager()
				.findFragmentById(R.id.mapview)).getMap();
		map.setOnMarkerClickListener(this);
		map.setOnCameraChangeListener(this);
		map.setInfoWindowAdapter(new CustomInfoWindowAdapter());
		map.setOnInfoWindowClickListener(this);
		progress = rootView.findViewById(R.id.progressbar);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setLeftButton(R.drawable.title_referesh, this);
		setRightButton(R.drawable.title_add, this);

		myLoc = PMApplication.instance().myLocation();
		if (myLoc != null) {
			onReceiveLocation(myLoc);
		} else {
			PMApplication.instance().addLocationListener(this);
		}

		if (myLoc != null) {
			new Handler() {
				public void handleMessage(android.os.Message msg) {
					refereshData();
				};
			}.sendEmptyMessageDelayed(0, 1200);
		}
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
		if (requestCode == REQUEST_CODE_SHARE
				&& resultCode == Activity.RESULT_OK) {
			Artwork share = data.getParcelableExtra("share");
			shares.add(share);
			showData();
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
				CameraUpdate center = CameraUpdateFactory.newLatLngZoom(
						new LatLng(myLoc.latitude, myLoc.longitude),
						DEFAULT_ZOOM_LEVEL);

				map.moveCamera(center);
				Toast.makeText(getActivity(),
						myLoc.latitude + ", " + myLoc.longitude,
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getActivity(), getString(R.string.msg_locating),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void refereshData() {
		CameraPosition cp = map.getCameraPosition();
		LatLng target = cp.target;
		double clat = target.latitude;
		double clng = target.longitude;
		LatLng left = map.getProjection().getVisibleRegion().nearLeft;
		double tlat = left.latitude;
		double tlng = left.longitude;
		int disKM = (int) (MapUtils.getDistance(clat, clng, tlat, tlng));
		loadData(clat, clng, disKM);
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
			data.clear();
			for (Artwork artwork : result.result()) {
				data.add(artwork);
			}
			showData();
		}
	}

	@Override
	public void onRequestProgress(MApiRequest arg0, int arg1, int arg2) {
	}

	@Override
	public void onRequestStart(MApiRequest arg0) {
	}

	private void showData() {
		map.clear();
		markerMap.clear();

		if (shares.size() > 0) {
			data.addAll(shares);
		}
		for (Artwork artwork : data) {
			BitmapDescriptor bd;
			if ("Official".equals(artwork.Type)) {
				bd = BitmapDescriptorFactory
						.fromResource(R.drawable.icon_marka);
			} else {
				bd = BitmapDescriptorFactory
						.fromResource(R.drawable.icon_markb);
			}
			Marker marker = map.addMarker(new MarkerOptions()
					.position(
							new LatLng(Double.valueOf(artwork.Latitude), Double
									.valueOf(artwork.Longitude)))
					.title(artwork.ArtworkName).icon(bd)
					.snippet(artwork.Artist));
			markerMap.put(marker, artwork);
		}
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
		if (isRequest || isFirstLoc) {
			Log.d("LocationOverlay", "receive location, animate to it");
			CameraUpdate center = CameraUpdateFactory.newLatLngZoom(new LatLng(
					location.latitude, location.longitude), DEFAULT_ZOOM_LEVEL);

			map.moveCamera(center);
			isRequest = false;
		}
		isFirstLoc = false;
	}

	@Override
	public boolean onMyLocationButtonClick() {
		return false;
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		curMarker = marker;
		return false;
	}

	@Override
	public void onCameraChange(CameraPosition cp) {
		do {
			if (curMarker == null) {
				break;
			}

//			LatLng target = cp.target;
//			if (curMarker.getPosition().latitude == target.latitude
//					&& curMarker.getPosition().longitude == target.longitude) {
//				return;
//			}
			curMarker = null;
			return;

		} while (false);

		refereshData();
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		Artwork aw = markerMap.get(marker);
		if (aw.ArtworkID == null) {
			Toast.makeText(getActivity(), getString(R.string.shareing),
					Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("pam://artworkinfo?id=" + aw.ArtworkID));
		if (myLoc != null) {
			intent.putExtra("location", myLoc);
		}
		startActivity(intent);
	}

	class CustomInfoWindowAdapter implements InfoWindowAdapter {

		private final PopupView popView;

		public CustomInfoWindowAdapter() {
			popView = (PopupView) getActivity().getLayoutInflater().inflate(
					R.layout.layout_pop_gmap, null);
		}

		@Override
		public View getInfoContents(Marker marker) {
			return null;
		}

		@Override
		public View getInfoWindow(Marker marker) {
			Artwork artwork = markerMap.get(marker);
			if (artwork != null) {
				popView.setData(artwork, myLoc);
			}
			return popView;
		}

	}

}
