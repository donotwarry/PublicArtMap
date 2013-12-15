package com.facsu.publicartmap.app;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;

import com.dennytech.common.app.BDActivity;
import com.facsu.publicartmap.R;
import com.facsu.publicartmap.widget.TitleBar;

/**
 * base activity
 * 
 * @author dengjun86
 * 
 */
public class PMActivity extends BDActivity {

	private TitleBar titleBar;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
	}

	@Override
	public void setContentView(int layoutResID) {
		requestWindowFeature(customTitleType());
		super.setContentView(layoutResID);
		if (customTitleType() == Window.FEATURE_CUSTOM_TITLE) {
			initCustomTitle();
		}
	}

	@Override
	public void setContentView(View view) {
		requestWindowFeature(customTitleType());
		super.setContentView(view);
		if (customTitleType() == Window.FEATURE_CUSTOM_TITLE) {
			initCustomTitle();
		}
	}

	@Override
	public void setContentView(View view, ViewGroup.LayoutParams params) {
		requestWindowFeature(customTitleType());
		super.setContentView(view, params);
		if (customTitleType() == Window.FEATURE_CUSTOM_TITLE) {
			initCustomTitle();
		}
	}

	private void initCustomTitle() {
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		titleBar = (TitleBar) findViewById(R.id.titlebar);
		setBackButtonEnable(true);
	}

	/**
	 * custom title type
	 * 
	 * @return
	 */
	protected int customTitleType() {
		return Window.FEATURE_CUSTOM_TITLE;
	}

	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		titleBar.setTitle(title.toString());
	}

	public void setLeftButton(int resId, OnClickListener listener) {
		titleBar.setLeftButton(resId, listener);
	}

	public void setRightButton(int resId, OnClickListener listener) {
		titleBar.setRightButton(resId, listener);
	}

	public void setBackButtonEnable(boolean enable) {
		titleBar.setBackButtonEnable(enable);
	}

}
