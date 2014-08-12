package com.facsu.publicartmap.login;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.utils.UIHandler;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.tencent.weibo.TencentWeibo;

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

public class LoginFragment extends PMFragment implements MApiRequestHandler,
		OnClickListener, PlatformActionListener, Callback {

	private static final int MSG_USERID_FOUND = 1;
	private static final int MSG_LOGIN = 2;
	private static final int MSG_AUTH_CANCEL = 3;
	private static final int MSG_AUTH_ERROR = 4;
	private static final int MSG_AUTH_COMPLETE = 5;

	private DPBasicItem username;
	private DPBasicItem password;
	private View login;
	private View loginBySinaWeibo;
	private View loginByQQWeibo;
	private View loginByQQ;

	private MApiRequest request;
	private String un;
	private String pw;
	private String ua;

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

		login.setOnClickListener(this);
		loginBySinaWeibo.setOnClickListener(this);
		loginByQQWeibo.setOnClickListener(this);
		loginByQQ.setOnClickListener(this);

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
		if (v == login) {
			if (checkInput()) {
				showProgressDialog(getString(R.string.msg_logining));
				requestUser(un, pw, ua, null, null);
			}
		} else if (v == loginBySinaWeibo) {
			authorize(new SinaWeibo(getActivity()));

		} else if (v == loginByQQWeibo) {
			authorize(new TencentWeibo(getActivity()));

		} else if (v == loginByQQ) {
			authorize(new QZone(getActivity()));
		}
	}

	private void authorize(Platform plat) {
		if (plat == null) {
			return;
		}

		if (plat.isValid()) {
			String userId = plat.getDb().getUserId();
			if (!TextUtils.isEmpty(userId)) {
				UIHandler.sendEmptyMessage(MSG_USERID_FOUND, this);
				// gotoRegister("username=" + plat.getDb().getUserName()
				// + "&useravatar=" + plat.getDb().getUserIcon());
				un = plat.getDb().getUserName();
				ua = plat.getDb().getUserIcon();
				requestUser(un, null, ua, plat.getDb().getPlatformNname() + "|"
						+ plat.getDb().getUserId(), null);
				if (plat instanceof SinaWeibo) {
					Environment.saveSinaAvatar(preferences(), plat.getDb()
							.getUserName(), plat.getDb().getUserIcon());

				} else if (plat instanceof TencentWeibo) {
					Environment.saveQQWeiboAvatar(preferences(), plat.getDb()
							.getUserName(), plat.getDb().getUserIcon());
				} else if (plat instanceof QZone) {
					Environment.saveQQAvatar(preferences(), plat.getDb()
							.getUserName(), plat.getDb().getUserIcon());
				}
				return;
			}
		}
		plat.setPlatformActionListener(this);
		plat.SSOSetting(true);
		plat.showUser(null);
		showProgressDialog(getString(R.string.msg_logining));
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_USERID_FOUND: {
			// Toast.makeText(getActivity(), R.string.userid_found,
			// Toast.LENGTH_SHORT).show();
		}
			break;
		case MSG_LOGIN: {
			String text = getString(R.string.logining, msg.obj);
			Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();

			Builder builder = new Builder(getActivity());
			builder.setTitle(R.string.if_register_needed);
			builder.setMessage(R.string.after_auth);
			builder.setPositiveButton(R.string.ok, null);
			builder.create().show();
		}
			break;
		case MSG_AUTH_CANCEL: {
			Toast.makeText(getActivity(), R.string.auth_cancel,
					Toast.LENGTH_SHORT).show();
		}
			break;
		case MSG_AUTH_ERROR: {
			Toast.makeText(getActivity(), R.string.auth_error,
					Toast.LENGTH_SHORT).show();
		}
			break;
		case MSG_AUTH_COMPLETE: {
			Toast.makeText(getActivity(), R.string.auth_complete,
					Toast.LENGTH_SHORT).show();
		}
			break;
		}
		return false;
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
		return true;
	}

	private void requestUser(String un, String pw, String au, String token,
			String s) {
		if (request != null) {
			mapiService().abort(request, LoginFragment.this, true);
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("UserName", un);
		map.put("Password", pw);
		map.put("AvatarUrl", au);
		map.put("ThirdToken", token);
		map.put("Signature", s);
		request = APIRequest.mapiPostJson(
				"http://web358082.dnsvhost.com/ACservice/ACService.svc/Login",
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
		dismissDialog();
		if (resp.result() instanceof CreateUserResult) {
			CreateUserResult result = (CreateUserResult) resp.result();
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

	@Override
	public void onRequestFailed(MApiRequest req, MApiResponse resp) {
		dismissDialog();
		showDialog(getString(R.string.app_name), resp.message().getErrorMsg(),
				null);
	}

	@Override
	public void onCancel(Platform arg0, int arg1) {

	}

	@Override
	public void onComplete(Platform plat, int arg1, HashMap<String, Object> arg2) {
		if (plat == null) {
			return;
		}

		if (plat.isValid()) {
			String userId = plat.getDb().getUserId();
			if (!TextUtils.isEmpty(userId)) {
				UIHandler.sendEmptyMessage(MSG_USERID_FOUND, this);
				// gotoRegister("username=" + plat.getDb().getUserName()
				// + "&useravatar=" + plat.getDb().getUserIcon());
				un = plat.getDb().getUserName();
				ua = plat.getDb().getUserIcon();
				requestUser(un, null, ua, plat.getDb().getPlatformNname() + "|"
						+ plat.getDb().getUserId(), null);
				if (plat instanceof SinaWeibo) {
					Environment.saveSinaAvatar(preferences(), plat.getDb()
							.getUserName(), plat.getDb().getUserIcon());

				} else if (plat instanceof TencentWeibo) {
					Environment.saveQQWeiboAvatar(preferences(), plat.getDb()
							.getUserName(), plat.getDb().getUserIcon());
				} else if (plat instanceof QZone) {
					Environment.saveQQAvatar(preferences(), plat.getDb()
							.getUserName(), plat.getDb().getUserIcon());
				}
			}
		}
	}

	@Override
	public void onError(Platform arg0, int arg1, Throwable arg2) {

	}

	@Override
	public void onLoginSuccess() {
		getActivity().setResult(Activity.RESULT_OK);
		getActivity().finish();
	}

	@Override
	public void onLoginCancel() {
		getActivity().setResult(Activity.RESULT_CANCELED);
		getActivity().finish();
	}

}
