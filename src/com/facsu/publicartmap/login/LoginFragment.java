package com.facsu.publicartmap.login;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dennytech.common.service.dataservice.mapi.MApiRequest;
import com.dennytech.common.service.dataservice.mapi.MApiRequestHandler;
import com.dennytech.common.service.dataservice.mapi.MApiResponse;
import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMFragment;
import com.facsu.publicartmap.bean.CreateUserResult;
import com.facsu.publicartmap.common.APIRequest;
import com.facsu.publicartmap.common.Environment;
import com.facsu.publicartmap.me.MeFragment;
import com.facsu.publicartmap.widget.DPBasicItem;

public class LoginFragment extends PMFragment implements MApiRequestHandler {

	private DPBasicItem username;
	private DPBasicItem password;
	private View login;
	private View loginBySinaWeibo;
	private View loginByQQWeibo;
	private View loginByQQ;

	private MApiRequest request;

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

	private void requestUser(String un, String pw, String au, String s) {
		if (request != null) {
			mapiService().abort(request, LoginFragment.this, true);
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("UserName", un);
		map.put("Password", pw);
		map.put("AvatarUrl", au);
		map.put("Signature", s);
		request = APIRequest
				.mapiPostJson(
						"http://web358082.dnsvhost.com/ACservice/ACService.svc/CreateUser",
						CreateUserResult.class, map);
		mapiService().exec(request, LoginFragment.this);
	}

	@Override
	public void onRequestStart(MApiRequest req) {
	}

	@Override
	public void onRequestProgress(MApiRequest req, int count, int total) {
	}

	@Override
	public void onRequestFinish(MApiRequest req, MApiResponse resp) {
		if (resp.result() instanceof CreateUserResult) {
			CreateUserResult result = (CreateUserResult) resp.result();
			preferences().edit().putString("uid", result.CreateUserResult.ID)
					.commit();
			Environment.setUserID(result.CreateUserResult.ID);
		}
	}

	@Override
	public void onRequestFailed(MApiRequest req, MApiResponse resp) {

	}

}
