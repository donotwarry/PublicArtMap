package com.facsu.publicartmap.app;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.widget.ImageView;
import android.widget.TabHost.TabSpec;

import com.facsu.publicartmap.R;

public class PMTabActivity extends PMActivity {

	protected FragmentTabHost tabHost;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getContentViewResID());
		tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		tabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
	}
	
	protected int getContentViewResID() {
		return R.layout.activity_tab_bottom;
	}

	protected void addTab(TabSpec tabSpec, Class<?> fragment) {
		tabHost.addTab(tabSpec, fragment, null);
	}
	
	public void setCurrentTabByTag(String tag) {
		tabHost.setCurrentTabByTag(tag);
	}
	
	public void setCurrentTab(int index) {
		tabHost.setCurrentTab(index);
	}

	public TabSpec setIndicatorImage(TabSpec spec, int resid) {
		ImageView v = new ImageView(this);
		v.setImageResource(resid);
		return spec.setIndicator(v);
	}

}
