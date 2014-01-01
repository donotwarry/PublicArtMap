package com.facsu.publicartmap.me;

import android.os.Bundle;

import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMActivity;

public class AboutActivity extends PMActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_about);
		setTitle(R.string.title_me);
	}

}
