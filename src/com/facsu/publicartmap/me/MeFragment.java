package com.facsu.publicartmap.me;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.dennytech.common.adapter.BasicAdapter;
import com.dennytech.common.service.dataservice.mapi.MApiRequest;
import com.dennytech.common.service.dataservice.mapi.MApiRequestHandler;
import com.dennytech.common.service.dataservice.mapi.MApiResponse;
import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMFragment;
import com.facsu.publicartmap.bean.CreateUserResult;
import com.facsu.publicartmap.common.APIRequest;
import com.facsu.publicartmap.common.Environment;
import com.facsu.publicartmap.widget.NetworkThumbView;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.bean.SocializeUser;
import com.umeng.socialize.common.SocializeConstants;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.UMSsoHandler;
import com.umeng.socialize.controller.listener.SocializeListeners.FetchUserListener;
import com.umeng.socialize.controller.listener.SocializeListeners.SocializeClientListener;

public class MeFragment extends PMFragment implements OnItemClickListener,
		MApiRequestHandler {

	final UMSocialService mController = UMServiceFactory.getUMSocialService(
			"com.umeng.share", RequestType.SOCIAL);
	private SocializeUser mUser;

	private ListView list;
	private Adapter adapter;

	private MApiRequest request;

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
		mController.getUserInfo(getActivity(), new FetchUserListener() {
			@Override
			public void onStart() {
			}

			@Override
			public void onComplete(int status, SocializeUser user) {
				mUser = user;
				if (mUser != null && mUser.mLoginAccount != null) {
					String snsuid = mUser.mLoginAccount.getUsid();
					if (!snsuid.equals(preferences().getString("snsuid", null))
							|| preferences().getString("uid", null) == null) {
						preferences().edit().putString("snsuid", snsuid)
								.commit();

						requestUser(mUser.mLoginAccount.getUserName(), "",
								mUser.mLoginAccount.getAccountIconUrl(), "暂无");
					}
					adapter.notifyDataSetChanged();
				}
			}
		});

		IntentFilter filter = new IntentFilter(ACTION_REFRESH_USER);
		getActivity().registerReceiver(receiver, filter);

		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		enableBackButton(false);
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
			if ("login".equals(menu.intent.getAction())) {
				int flag = SocializeConstants.FLAG_USER_CENTER_LOGIN_VERIFY
						| SocializeConstants.FLAG_USER_CENTER_HIDE_LOGININFO;
				mController.openUserCenter(getActivity(), flag);
				mController.getUserInfo(getActivity(), new FetchUserListener() {
					@Override
					public void onStart() {
					}

					@Override
					public void onComplete(int status, SocializeUser user) {
						mUser = user;
						if (mUser != null) {
							String snsuid = mUser.mLoginAccount.getUsid();
							if (!snsuid.equals(preferences().getString(
									"snsuid", null))
									|| preferences().getString("uid", null) == null) {
								preferences().edit()
										.putString("snsuid", snsuid).commit();

								requestUser(
										mUser.mLoginAccount.getUserName(),
										"",
										mUser.mLoginAccount.getAccountIconUrl(),
										"暂无");
							}
						}
					}
				});
			} else {
				startActivity(menu.intent);
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 根据requestCode获取对应的SsoHandler
		UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(
				requestCode);
		if (ssoHandler != null) {
			ssoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
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
			Menu loginMenu = new Menu(R.drawable.ic_me_login,
					getString(R.string.menu_login), new Intent("login"));
			if (mUser != null) {
				loginMenu.iconUrl = mUser.mLoginAccount.getAccountIconUrl();
				loginMenu.title = mUser.mLoginAccount.getUserName();
			}
			menus.add(loginMenu);
			menuMap.put(sections[0], menus);

			menus = new ArrayList<Menu>();
			menus.add(new Menu(R.drawable.ic_me_feedback,
					getString(R.string.menu_feedback), new Intent()));
			menus.add(new Menu(R.drawable.ic_me_update,
					getString(R.string.menu_update), new Intent()));
			menus.add(new Menu(R.drawable.ic_me_about,
					getString(R.string.menu_about), new Intent()));
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
			Object item = getItem(position);
			if (item instanceof String) {
				TextView view = new TextView(getActivity());
				view.setPadding(10, 10, 10, 10);
				view.setText((String) item);
				return view;
			} else {
				View view = LayoutInflater.from(getActivity()).inflate(
						R.layout.list_item_me_menu, null);
				NetworkThumbView icon = (NetworkThumbView) view
						.findViewById(R.id.icon);
				TextView title = (TextView) view.findViewById(R.id.title);
				title.setText(((Menu) item).title);
				View logout = view.findViewById(R.id.logout);
				if (((Menu) item).iconUrl != null) {
					icon.setImage(((Menu) item).iconUrl);
					logout.setVisibility(View.VISIBLE);
					logout.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							mController.deleteOauth(getActivity(),
									mUser.mDefaultPlatform,
									new SocializeClientListener() {

										@Override
										public void onStart() {
											showProgressDialog(getString(R.string.msg_logout));
										}

										@Override
										public void onComplete(int status,
												SocializeEntity arg1) {
											dismissDialog();
											if (status / 100 == 2) {
												mUser = null;
												preferences().edit()
														.remove("snsuid")
														.remove("uid").commit();
												reset();
											}
										}
									});
						}
					});
				} else {
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
		}
	}

	@Override
	public void onRequestFailed(MApiRequest req, MApiResponse resp) {

	}

}
