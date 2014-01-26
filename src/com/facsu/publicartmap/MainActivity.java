package com.facsu.publicartmap;

import android.os.Bundle;
import android.view.Window;
import cn.sharesdk.framework.ShareSDK;

import com.facsu.publicartmap.app.PMTabActivity;
import com.facsu.publicartmap.barcode.BarcodeFragment;
import com.facsu.publicartmap.explore.ExploreFragment;
import com.facsu.publicartmap.me.MeFragment;
import com.umeng.update.UmengUpdateAgent;

public class MainActivity extends PMTabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			addTab(setIndicatorImage(tabHost.newTabSpec("explore"),
					R.drawable.tab_explore), ExploreFragment.class);
			addTab(setIndicatorImage(tabHost.newTabSpec("barcode"),
					R.drawable.tab_barcode), BarcodeFragment.class);
			addTab(setIndicatorImage(tabHost.newTabSpec("me"), R.drawable.tab_me),
					MeFragment.class);
		}
		
		ShareSDK.initSDK(this);
		
		UmengUpdateAgent.update(this);
	}
	
	@Override
	protected void onDestroy() {
		ShareSDK.stopSDK(this);
		super.onDestroy();
	}

	@Override
	protected int customTitleType() {
		return Window.FEATURE_NO_TITLE;
	}

}
