package com.facsu.publicartmap.explore;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.dennytech.common.service.dataservice.mapi.CacheType;
import com.dennytech.common.service.dataservice.mapi.MApiRequest;
import com.dennytech.common.service.dataservice.mapi.MApiRequestHandler;
import com.dennytech.common.service.dataservice.mapi.MApiResponse;
import com.dennytech.common.service.dataservice.mapi.impl.BasicMApiRequest;
import com.dennytech.common.util.Log;
import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMGMapFragment;
import com.facsu.publicartmap.bean.Artwork;
import com.facsu.publicartmap.bean.GetArtworksByGPSResult;
import com.facsu.publicartmap.bean.Location;
import com.facsu.publicartmap.bean.User;
import com.facsu.publicartmap.utils.MapUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class ExploreGMapFragment extends PMGMapFragment implements
		OnClickListener, MApiRequestHandler {

	private View rootView;
	private View progress;

	boolean isRequest = false;
	boolean isFirstLoc = true;
	boolean isClickOnPop = false;

	private MApiRequest request;
	private Artwork[] data;
	private Artwork curArtwork;
	private GeoPoint curGP;
	private Location myLoc;

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
		progress = rootView.findViewById(R.id.progressbar);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setLeftButton(R.drawable.title_referesh, this);
		setRightButton(R.drawable.title_add, this);

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
				startActivity(i);
			}

		} else if (v.getId() == R.id.mylocation) {
			if (myLoc != null) {
				CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(
						myLoc.latitude, myLoc.longitude));

				getMap().moveCamera(center);
				Toast.makeText(getActivity(),
						myLoc.latitude + ", " + myLoc.longitude,
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getActivity(), getString(R.string.msg_locating),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	// TODO 求半径内
	private void refereshData() {
		CameraPosition cp = getMap().getCameraPosition();
		LatLng target = cp.target;
		double clat = target.latitude;
		double clng = target.longitude;
		double tlat = target.latitude + 0.1;
		double tlng = target.longitude + 0.1;
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
	public void onRequestFailed(MApiRequest arg0, MApiResponse arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRequestFinish(MApiRequest arg0, MApiResponse arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRequestProgress(MApiRequest arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRequestStart(MApiRequest arg0) {
		// TODO Auto-generated method stub

	}

}
