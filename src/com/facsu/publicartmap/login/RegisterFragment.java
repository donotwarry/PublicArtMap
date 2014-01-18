package com.facsu.publicartmap.login;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dennytech.common.service.dataservice.mapi.MApiRequest;
import com.dennytech.common.service.dataservice.mapi.MApiRequestHandler;
import com.dennytech.common.service.dataservice.mapi.MApiResponse;
import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMFragment;
import com.facsu.publicartmap.bean.CreateUserResult;
import com.facsu.publicartmap.bean.User;
import com.facsu.publicartmap.common.APIRequest;
import com.facsu.publicartmap.common.Environment;
import com.facsu.publicartmap.widget.DPBasicItem;

public class RegisterFragment extends PMFragment implements OnClickListener,
		MApiRequestHandler {

	private DPBasicItem username;
	private DPBasicItem password;
	private DPBasicItem passwordRe;
	private View register;

	private MApiRequest request;
	private String un;
	private String pw;
	private String ua;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Uri uri = getActivity().getIntent().getData();
		un = uri.getQueryParameter("username");
		ua = uri.getQueryParameter("useravatar");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_register, null);
		username = (DPBasicItem) view.findViewById(R.id.register_username);
		password = (DPBasicItem) view.findViewById(R.id.register_password);
		passwordRe = (DPBasicItem) view.findViewById(R.id.register_password_re);
		register = view.findViewById(R.id.register);

		username.setInputText(un);

		register.setOnClickListener(this);
		return view;
	}

	@Override
	public void onDestroy() {
		if (request != null) {
			mapiService().abort(request, this, true);
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v == register) {
			if (checkInput()) {
				requestUser(un, pw, ua, null);
			}
		}
	}

	private void requestUser(String un, String pw, String au, String s) {
		showProgressDialog(getString(R.string.msg_logining));
		if (request != null) {
			mapiService().abort(request, RegisterFragment.this, true);
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
		mapiService().exec(request, RegisterFragment.this);
	}

	private boolean checkInput() {
		this.un = username.getInputText().trim();
		if (TextUtils.isEmpty(un)) {
			Toast.makeText(getActivity(),
					getString(R.string.msg_username_not_null),
					Toast.LENGTH_SHORT).show();
			return false;
		}

		this.pw = password.getInputText().trim();
		if (TextUtils.isEmpty(password.getInputText().trim())) {
			Toast.makeText(getActivity(),
					getString(R.string.msg_password_not_null),
					Toast.LENGTH_SHORT).show();
			return false;
		}
		if (!password.getInputText().trim()
				.equals(passwordRe.getInputText().trim())) {
			Toast.makeText(getActivity(),
					getString(R.string.msg_password_not_same),
					Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	@Override
	public void onRequestStart(MApiRequest req) {
	}

	@Override
	public void onRequestProgress(MApiRequest req, int count, int total) {
	}

	@Override
	public void onRequestFinish(MApiRequest req, MApiResponse resp) {
		dismissDialog();
		if (resp.result() instanceof CreateUserResult) {
			CreateUserResult result = (CreateUserResult) resp.result();
			if (result.CreateUserResult.hasError()) {
				showDialog(getString(R.string.app_name),
						result.CreateUserResult.ErrorDesc, null);

			} else {
				User user = new User(Integer.valueOf(result.CreateUserResult.ID),
						un, ua);
				String avatar = Environment.getSinaAvatar(preferences(), un);
				if (avatar != null && user.AvatarUrl == null) {
					user.AvatarUrl = avatar;
				}
				user.save(preferences());
				getActivity().setResult(Activity.RESULT_OK);
				getActivity().finish();
			}
		}
	}

	@Override
	public void onRequestFailed(MApiRequest req, MApiResponse resp) {
		dismissDialog();
		showDialog(getString(R.string.app_name), resp.message().getErrorMsg(), null);
	}

}
