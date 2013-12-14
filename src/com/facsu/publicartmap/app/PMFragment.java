package com.facsu.publicartmap.app;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.dennytech.common.app.BDFragment;
import com.facsu.publicartmap.R;

/**
 * base fragment
 * 
 * @author dengjun86
 * 
 */
public class PMFragment extends BDFragment {

	private TextView titleTv;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		titleTv = (TextView) view.findViewById(R.id.title);
	}

	public void setTitle(String title) {
		if (titleTv != null) {
			titleTv.setText(title);
		} else {
			getActivity().setTitle(title);
		}
	}

	public void setTitle(int resId) {
		setTitle(getString(resId));
	}
}
