package com.facsu.publicartmap.login;

import android.net.Uri;
import android.os.Bundle;

import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMTabActivity;

public class LoginTabActivity extends PMTabActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState == null) {
			addTab(setIndicatorImage(tabHost.newTabSpec("login"),
					R.drawable.tab_login), LoginFragment.class);
			addTab(setIndicatorImage(tabHost.newTabSpec("register"),
					R.drawable.tab_register), RegisterFragment.class);
		}
		
		Uri uri = getIntent().getData();
		if (uri.getHost().equals("login")) {
			setCurrentTabByTag("login");
		} else {
			setCurrentTabByTag("register");
		}
	}

	@Override
	protected int getContentViewResID() {
		return R.layout.activity_tab_top;
	}
	
}
