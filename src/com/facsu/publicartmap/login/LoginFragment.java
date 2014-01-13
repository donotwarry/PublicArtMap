package com.facsu.publicartmap.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMFragment;
import com.facsu.publicartmap.widget.DPBasicItem;

public class LoginFragment extends PMFragment {
	
	private DPBasicItem username;
	private DPBasicItem password;
	private View login;
	private View loginBySinaWeibo;
	private View loginByQQWeibo;
	private View loginByQQ;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_login, null);
		username = (DPBasicItem) view.findViewById(R.id.login_username);
		password = (DPBasicItem) view.findViewById(R.id.login_password);
		login = view.findViewById(R.id.login);
		loginBySinaWeibo = view.findViewById(R.id.login_by_sinaweibo);
		loginByQQWeibo = view.findViewById(R.id.login_by_qqweibo);
		loginByQQ = view.findViewById(R.id.login_by_qq);
		return view;
	}

}
