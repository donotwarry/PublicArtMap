package com.facsu.publicartmap.app;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.facsu.publicartmap.R;

/**
 * base activity
 * 
 * @author dengjun86
 * 
 */
public class PMActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
	}

	@Override
	public void setContentView(int layoutResID) {
		requestWindowFeature(customTitleType());
		super.setContentView(layoutResID);
		if (customTitleType() == Window.FEATURE_CUSTOM_TITLE) {
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
					R.layout.custom_title);
		}
	}

	@Override
	public void setContentView(View view) {
		requestWindowFeature(customTitleType());
		super.setContentView(view);
		if (customTitleType() == Window.FEATURE_CUSTOM_TITLE) {
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
					R.layout.custom_title);
		}
	}

	@Override
	public void setContentView(View view, ViewGroup.LayoutParams params) {
		requestWindowFeature(customTitleType());
		super.setContentView(view, params);
		if (customTitleType() == Window.FEATURE_CUSTOM_TITLE) {
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
					R.layout.custom_title);
		}
	}

	/**
	 * custom title type
	 * 
	 * @return
	 */
	protected int customTitleType() {
		return Window.FEATURE_CUSTOM_TITLE;
	}

}
