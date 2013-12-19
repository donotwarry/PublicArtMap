package com.facsu.publicartmap.explore;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMActivity;

public class ShareArtworkActivity extends PMActivity {
	
	private TextView addressTv;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_share);
		setTitle(getString(R.string.title_share));
		setRightButton(R.drawable.title_send, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
			}
		});
		
		addressTv = (TextView) findViewById(R.id.share_location);
		addressTv.setText(getIntent().getStringExtra("address") );
	}

}
