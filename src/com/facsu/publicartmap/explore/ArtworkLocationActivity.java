package com.facsu.publicartmap.explore;

import android.os.Bundle;

import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMMapActivity;
import com.facsu.publicartmap.bean.Artwork;
import com.facsu.publicartmap.utils.TextPicker;

public class ArtworkLocationActivity extends PMMapActivity {

	private Artwork artwork;

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		mapView().getController().setZoom(14);
		mapView().getController().enableClick(false);
		mapView().setBuiltInZoomControls(true);

		artwork = getIntent().getParcelableExtra("artwork");
		setTitle(TextPicker.pick(this, artwork.ArtworkName));
		
		ItemizedOverlay overlay = new ItemizedOverlay(getResources()
				.getDrawable(R.drawable.icon_marka), mapView());
		GeoPoint gp = new GeoPoint(
				(int) (Double.valueOf(artwork.Latitude) * 1E6),
				(int) (Double.valueOf(artwork.Longitude) * 1E6));
		OverlayItem item = new OverlayItem(gp, "", "");
		overlay.addItem(item);
		mapView().getOverlays().add(overlay);
		mapController().animateTo(gp);
	}

}
