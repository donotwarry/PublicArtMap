package com.facsu.publicartmap.me;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMFragment;

public class MeFragment extends PMFragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_me, container, false);
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		setTitle(R.string.title_me);
	}

}
