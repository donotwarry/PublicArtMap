package com.facsu.publicartmap;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import com.facsu.publicartmap.app.PMActivity;
import com.umeng.analytics.MobclickAgent;

public class SplashActivity extends PMActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		new Handler() {
			public void handleMessage(android.os.Message msg) {
				startActivity(new Intent(Intent.ACTION_VIEW,
						Uri.parse("pam://home")));
				finish();
			};
		}.sendEmptyMessageDelayed(0, 1000);

		MobclickAgent.updateOnlineConfig(this);
	}

	@Override
	protected int customTitleType() {
		return Window.FEATURE_NO_TITLE;
	}

}
