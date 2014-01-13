package com.facsu.publicartmap.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMFragment;
import com.facsu.publicartmap.widget.DPBasicItem;

public class RegisterFragment extends PMFragment {
	
	private DPBasicItem username;
	private DPBasicItem password;
	private DPBasicItem passwordRe;
	private View register;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_register, null);
		username = (DPBasicItem) view.findViewById(R.id.register_username);
		password = (DPBasicItem) view.findViewById(R.id.register_password);
		passwordRe = (DPBasicItem) view.findViewById(R.id.register_password_re);
		register = view.findViewById(R.id.register);
		return view;
	}

}
