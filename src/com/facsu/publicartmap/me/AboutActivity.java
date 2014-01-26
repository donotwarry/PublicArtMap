package com.facsu.publicartmap.me;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMActivity;

public class AboutActivity extends PMActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_about);
		setTitle(R.string.title_me);

		TextView version = (TextView) findViewById(R.id.version);
		version.setText("版本：" + getVersionName());
	}

	public String getVersionName() {
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			return pi.versionName;
		} catch (NameNotFoundException e) {
			return "1.0";
		}
	}

}
