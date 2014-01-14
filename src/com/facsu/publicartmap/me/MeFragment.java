package com.facsu.publicartmap.me;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.utils.UIHandler;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.weibo.TencentWeibo;

import com.dennytech.common.adapter.BasicAdapter;
import com.dennytech.common.service.dataservice.mapi.MApiRequest;
import com.dennytech.common.service.dataservice.mapi.MApiRequestHandler;
import com.dennytech.common.service.dataservice.mapi.MApiResponse;
import com.dennytech.common.util.Log;
import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMFragment;
import com.facsu.publicartmap.bean.CreateUserResult;
import com.facsu.publicartmap.common.APIRequest;
import com.facsu.publicartmap.common.Environment;
import com.facsu.publicartmap.widget.NetworkThumbView;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

public class MeFragment extends PMFragment implements OnItemClickListener,
		MApiRequestHandler, PlatformActionListener, Callback {

	private static final int MSG_USERID_FOUND = 1;
	private static final int MSG_LOGIN = 2;
	private static final int MSG_AUTH_CANCEL = 3;
	private static final int MSG_AUTH_ERROR = 4;
	private static final int MSG_AUTH_COMPLETE = 5;

	private ListView list;
	private Adapter adapter;

	private MApiRequest request;

	private Platform xlPlatform;
	private Platform tcPlatform;

	private static final String ACTION_REFRESH_USER = "action_referesh_user";
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String name = intent.getStringExtra("username");
			String password = intent.getStringExtra("password");
			String avatarurl = intent.getStringExtra("avatarurl");
			String signature = intent.getStringExtra("signature");
			requestUser(name, password, avatarurl, signature);
		}

	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_me, container, false);
		list = (ListView) v.findViewById(R.id.list);
		adapter = new Adapter();
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);

		IntentFilter filter = new IntentFilter(ACTION_REFRESH_USER);
		getActivity().registerReceiver(receiver, filter);

		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		enableBackButton(false);

		SinaWeibo weibo = new SinaWeibo(getActivity());
		if (weibo.isValid()) {
			String userId = weibo.getDb().getUserId();
			if (userId != null) {
				login(weibo.getName(), userId, weibo.getDb().getUserName(),
						weibo.getDb().getUserIcon(), null);
				this.xlPlatform = weibo;
				adapter.reset();
			}
		}

		TencentWeibo tcweibo = new TencentWeibo(getActivity());
		if (tcweibo.isValid()) {
			String userId = tcweibo.getDb().getUserId();
			if (userId != null) {
				login(tcweibo.getName(), userId, tcweibo.getDb().getUserName(),
						tcweibo.getDb().getUserIcon(), null);
				this.tcPlatform = tcweibo;
				adapter.reset();
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		setTitle(R.string.title_me);
	}

	@Override
	public void onDestroy() {
		if (request != null) {
			mapiService().abort(request, this, true);
		}
		getActivity().unregisterReceiver(receiver);
		super.onDestroy();
	}

	private void requestUser(String un, String pw, String au, String s) {
		if (request != null) {
			mapiService().abort(request, MeFragment.this, true);
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
		mapiService().exec(request, MeFragment.this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Object item = arg0.getItemAtPosition(arg2);
		if (item instanceof Menu) {
			Menu menu = (Menu) item;
			if ("weibo".equals(menu.intent.getAction())) {
				authorize(new SinaWeibo(getActivity()), xlPlatform);
			} else if ("tcweibo".equals(menu.intent.getAction())) {
				authorize(new TencentWeibo(getActivity()), tcPlatform);

			} else if ("update".equals(menu.intent.getAction())) {
				UmengUpdateAgent.setUpdateOnlyWifi(false);
				UmengUpdateAgent.setUpdateAutoPopup(false);
				UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
					@Override
					public void onUpdateReturned(int updateStatus,
							UpdateResponse updateInfo) {
						dismissDialog();
						switch (updateStatus) {
						case UpdateStatus.Yes: // has update
							UmengUpdateAgent.showUpdateDialog(getActivity(),
									updateInfo);
							break;
						case UpdateStatus.No: // has no update
							showDialog(getString(R.string.app_name),
									getString(R.string.update_no), null);
							break;
						case UpdateStatus.NoneWifi: // none wifi
							Toast.makeText(getActivity(),
									getString(R.string.update_no_wifi),
									Toast.LENGTH_SHORT).show();
							break;
						case UpdateStatus.Timeout: // time out
							Toast.makeText(getActivity(),
									getString(R.string.update_timeout),
									Toast.LENGTH_SHORT).show();
							break;
						}
					}
				});
				UmengUpdateAgent.update(getActivity());
				showProgressDialog(getString(R.string.update_checking));

			} else {
				try {
					startActivity(menu.intent);
				} catch (Exception e) {
					Log.e(getTag(), "start activity failed", e);
				}

			}
		}
	}

	private void authorize(Platform plat, Platform save) {
		if (plat == null) {
			return;
		}

		if (plat.isValid()) {
			String userId = plat.getDb().getUserId();
			if (userId != null) {
				UIHandler.sendEmptyMessage(MSG_USERID_FOUND, this);
				login(plat.getName(), userId, plat.getDb().getUserName(), plat
						.getDb().getUserIcon(), null);
				save = plat;
				return;
			}
		}
		plat.setPlatformActionListener(this);
		plat.SSOSetting(true);
		plat.showUser(null);
	}

	private void login(String plat, String userId, String userName,
			String userIcon, HashMap<String, Object> userInfo) {
		// Message msg = new Message();
		// msg.what = MSG_LOGIN;
		// msg.obj = plat;
		// UIHandler.sendMessage(msg, this);

		requestUser(userName, "", userIcon, "暂无");
	}

	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_USERID_FOUND: {
			Toast.makeText(getActivity(), R.string.userid_found,
					Toast.LENGTH_SHORT).show();
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

	class Adapter extends BasicAdapter {

		String[] sections = { getString(R.string.section_login),
				getString(R.string.section_info) };
		Map<String, List<Menu>> menuMap = new HashMap<String, List<Menu>>();

		public Adapter() {
			reset();
		}

		private void reset() {
			menuMap.clear();

			List<Menu> menus = new ArrayList<Menu>();
			Menu weiboMenu = new Menu(R.drawable.ic_me_weibo,
					getString(R.string.menu_weibo_login), new Intent("weibo"));
			if (xlPlatform != null) {
				weiboMenu.iconUrl = xlPlatform.getDb().getUserIcon();
				weiboMenu.title = xlPlatform.getDb().getUserName();
			}
			menus.add(weiboMenu);
			Menu tcweiboMenu = new Menu(R.drawable.ic_me_tcweibo,
					getString(R.string.menu_txweibo_login), new Intent(
							"tcweibo"));
			if (tcPlatform != null) {
				tcweiboMenu.iconUrl = tcPlatform.getDb().getUserIcon();
				tcweiboMenu.title = tcPlatform.getDb().getUserName();
			}
			menus.add(tcweiboMenu);
			menuMap.put(sections[0], menus);

			menus = new ArrayList<Menu>();
			menus.add(new Menu(R.drawable.ic_me_feedback,
					getString(R.string.menu_feedback), new Intent(
							Intent.ACTION_SENDTO, Uri
									.parse("mailto:epublicart@gmail.com"))));
			menus.add(new Menu(R.drawable.ic_me_update,
					getString(R.string.menu_update), new Intent("update")));
			menus.add(new Menu(R.drawable.ic_me_about,
					getString(R.string.menu_about), new Intent(
							Intent.ACTION_VIEW, Uri.parse("pam://login")))); // about
			menuMap.put(sections[1], menus);

			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			int count = 0;
			for (String section : sections) {
				count += (menuMap.get(section).size() + 1);
			}
			return count;
		}

		@Override
		public Object getItem(int position) {
			int total = 0;
			for (int i = 0; i < sections.length; i++) {
				String section = sections[i];
				List<Menu> cmps = menuMap.get(section);
				total += (cmps.size() + 1);
				if (total > position) {
					int pos = total - position;
					if (pos > cmps.size()) {
						return section;
					} else {
						return cmps.get(cmps.size() - pos);
					}
				}
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final Object item = getItem(position);
			if (item instanceof String) {
				TextView view = new TextView(getActivity());
				view.setPadding(10, 10, 10, 10);
				view.setText((String) item);
				return view;
			} else {
				View view = LayoutInflater.from(getActivity()).inflate(
						R.layout.list_item_me_menu, null);
				NetworkThumbView neticon = (NetworkThumbView) view
						.findViewById(R.id.net_icon);
				ImageView icon = (ImageView) view.findViewById(R.id.icon);
				TextView title = (TextView) view.findViewById(R.id.title);
				title.setText(((Menu) item).title);
				View logout = view.findViewById(R.id.logout);
				if (((Menu) item).iconUrl != null) {
					neticon.setVisibility(View.VISIBLE);
					neticon.setImage(((Menu) item).iconUrl);
					icon.setVisibility(View.GONE);
					logout.setVisibility(View.VISIBLE);
					logout.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							if (((Menu) item).intent.getAction()
									.equals("weibo")) {
								xlPlatform.removeAccount();
								xlPlatform = null;
							} else {
								tcPlatform.removeAccount();
								tcPlatform = null;
							}
							reset();
						}
					});
				} else {
					neticon.setVisibility(View.GONE);
					icon.setVisibility(View.VISIBLE);
					icon.setImageResource(((Menu) item).icon);
					logout.setVisibility(View.GONE);
				}
				return view;
			}
		}
	}

	class Menu {
		int icon;
		String iconUrl;
		String title;
		Intent intent;

		public Menu(int icon, String title, Intent intent) {
			this.icon = icon;
			this.title = title;
			this.intent = intent;
		}
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
			adapter.reset();
		}
	}

	@Override
	public void onRequestFailed(MApiRequest req, MApiResponse resp) {

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
			if (userId != null) {
				UIHandler.sendEmptyMessage(MSG_USERID_FOUND, this);
				login(plat.getName(), userId, plat.getDb().getUserName(), plat
						.getDb().getUserIcon(), null);
				this.xlPlatform = plat;
			}
		}
	}

	@Override
	public void onError(Platform arg0, int arg1, Throwable arg2) {
	}

}
