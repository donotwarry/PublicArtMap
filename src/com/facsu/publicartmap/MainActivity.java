package com.facsu.publicartmap;

import android.os.Bundle;
import android.view.Window;

import com.facsu.publicartmap.app.PMTabActivity;
import com.facsu.publicartmap.explore.ExploreFragment;

public class MainActivity extends PMTabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addTab(setIndicatorImage(tabHost.newTabSpec("explore"),
				R.drawable.tab_explore), ExploreFragment.class);
		addTab(setIndicatorImage(tabHost.newTabSpec("barcode"),
				R.drawable.tab_barcode), ExploreFragment.class);
		addTab(setIndicatorImage(tabHost.newTabSpec("me"), R.drawable.tab_me),
				ExploreFragment.class);
	}

	@Override
	protected int customTitleType() {
		return Window.FEATURE_NO_TITLE;
	}

}
