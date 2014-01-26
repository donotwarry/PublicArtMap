package com.facsu.publicartmap.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facsu.publicartmap.R;
import com.facsu.publicartmap.bean.Artwork;
import com.facsu.publicartmap.bean.Location;
import com.facsu.publicartmap.utils.MapUtils;
import com.facsu.publicartmap.utils.TextPicker;

public class PopupView extends FrameLayout {

	private TextView title;
	private TextView content;

	public PopupView(Context context) {
		this(context, null);
	}

	public PopupView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		title = (TextView) findViewById(R.id.title);
		content = (TextView) findViewById(R.id.content);
	}

	public void setData(Artwork data, Location myLoc) {
		double awLat = Double.valueOf(data.Latitude);
		double awLng = Double.valueOf(data.Longitude);
		double distance = MapUtils.getDistance(awLat, awLng, myLoc.latitude,
				myLoc.longitude);
		String disStr = "";
		if (distance < 1) {
			disStr = getString(R.string.lab_dis2u) + (int) (distance * 1000)
					+ getString(R.string.lab_m);
		} else if (distance > 9999) {
			disStr = getString(R.string.lab_dis2u) + (int) (distance / 10000)
					+ getString(R.string.lab_10km);
		} else {
			disStr = getString(R.string.lab_dis2u) + (int) (distance)
					+ getString(R.string.lab_km);
		}
		title.setText(disStr);
		content.setText(TextPicker.pick(getContext(), data.ArtworkName));
	}

	private String getString(int resId) {
		return getContext().getString(resId);
	}
}
