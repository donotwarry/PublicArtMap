package com.facsu.publicartmap.explore;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;

import com.dennytech.common.service.dataservice.mapi.CacheType;
import com.dennytech.common.service.dataservice.mapi.MApiRequest;
import com.dennytech.common.service.dataservice.mapi.MApiRequestHandler;
import com.dennytech.common.service.dataservice.mapi.MApiResponse;
import com.dennytech.common.service.dataservice.mapi.impl.BasicMApiRequest;
import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMActivity;
import com.facsu.publicartmap.bean.Artwork;
import com.facsu.publicartmap.bean.ArtworkImage;
import com.facsu.publicartmap.bean.CreateUserResult;
import com.facsu.publicartmap.bean.GetArtworkByIDResult;
import com.facsu.publicartmap.bean.GetImageUrlsResult;
import com.facsu.publicartmap.bean.Location;
import com.facsu.publicartmap.bean.VoteResult;
import com.facsu.publicartmap.common.Environment;
import com.facsu.publicartmap.widget.NetworkPhotoView;

public class ArtworkInfoActivity extends PMActivity implements
		MApiRequestHandler, OnClickListener {

	private ViewPager imgPager;
	private Adapter adapter;
	private View imgLoadingPb;
	private TextView voteNumTv, commentNumTv;
	private TextView introTv;
	private TextView locTv;

	private MApiRequest infoReq;
	private MApiRequest imgReq;
	private MApiRequest voteReq;
	private MApiRequest createUserReq;
	private Location location;
	private String artworkID;
	private GetArtworkByIDResult infoResult;
	private GetImageUrlsResult imagesResult;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_artworkinfo);
		setRightButton(R.drawable.title_share, this);
		imgPager = (ViewPager) findViewById(R.id.artworkinfo_pager);
		imgLoadingPb = findViewById(R.id.artworkinfo_images_loading);
		voteNumTv = (TextView) findViewById(R.id.artworkinfo_vote);
		voteNumTv.setOnClickListener(this);
		commentNumTv = (TextView) findViewById(R.id.artworkinfo_comment_num);
		introTv = (TextView) findViewById(R.id.artworkinfo_intro);
		locTv = (TextView) findViewById(R.id.artworkinfo_location);
		locTv.setOnClickListener(this);
		findViewById(R.id.artworkinfo_comment_add).setOnClickListener(this);

		adapter = new Adapter();
		imgPager.setAdapter(adapter);

		Artwork artwork = getIntent().getParcelableExtra("artwork");
		location = getIntent().getParcelableExtra("location");
		artworkID = artwork == null ? getIntent().getData().getQueryParameter(
				"id") : artwork.ArtworkID;

		if (artwork != null) {
			setArtwork(artwork);
		} else {
			requestInfo();
		}

		requestImages();
	}

	@Override
	protected void onDestroy() {
		if (infoReq != null) {
			mapiService().abort(infoReq, this, true);
		}
		if (imgReq != null) {
			mapiService().abort(imgReq, this, true);
		}
		if (voteReq != null) {
			mapiService().abort(voteReq, this, true);
		}
		if (createUserReq != null) {
			mapiService().abort(createUserReq, this, true);
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.artworkinfo_comment_add) {
			startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse("pam://comment?id=" + artworkID)));

		} else if (v.getId() == R.id.artworkinfo_location) {
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("pam://artworklocation"));
			intent.putExtra("artwork", infoResult.result());
			startActivity(intent);
			
		} else if (v.getId() == R.id.artworkinfo_vote) {
			if (voteReq != null) {
				mapiService().abort(voteReq, this, true);
			}
			voteReq = BasicMApiRequest.mapiGet(
					"http://web358082.dnsvhost.com/ACservice/ACService.svc/Vote/"
							+ Environment.userID() + "/" + artworkID + "/1",
					CacheType.DISABLED, VoteResult.class);
			mapiService().exec(voteReq, this);

		} else if (v.getId() == R.id.title_right_btn) {
			String imgUrl = String
					.format("http://web358082.dnsvhost.com/ACservice/pics/%s.jpg",
							imagesResult.GetImageUrlsResult[imgPager
									.getCurrentItem()].ImageURL);
			showShare(true, null, getString(R.string.msg_share_text), imgUrl);
		}
	}

	private void showShare(boolean silent, String platform, String content,
			String imgUrl) {
		final OnekeyShare oks = new OnekeyShare();
		oks.setNotification(R.drawable.ic_launcher,
				getString(R.string.app_name));
		oks.setAddress("12345678901");
		// oks.setTitle(getString(R.string.evenote_title));
		// oks.setTitleUrl("http://sharesdk.cn");
		oks.setText(content);
		// oks.setImagePath(MainActivity.TEST_IMAGE);
		oks.setImageUrl(imgUrl);
		oks.setUrl("http://www.sharesdk.cn");
		// oks.setFilePath(MainActivity.TEST_IMAGE);
		// oks.setComment(menu.getContext().getString(R.string.share));
		// oks.setSite(getString(R.string.app_name));
		// oks.setSiteUrl("http://sharesdk.cn");
		oks.setVenueName(getString(R.string.app_name));
		// oks.setVenueDescription("This is a beautiful place!");
		oks.setLatitude(23.056081f);
		oks.setLongitude(113.385708f);
		oks.setSilent(silent);
		if (platform != null) {
			oks.setPlatform(platform);
		}

		// 去除注释，可令编辑页面显示为Dialog模式
		// oks.setDialogMode();

		// 去除注释，在自动授权时可以禁用SSO方式
		// oks.disableSSOWhenAuthorize();

		// 去除注释，则快捷分享的操作结果将通过OneKeyShareCallback回调
		// oks.setCallback(new OneKeyShareCallback());
		oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {

			@Override
			public void onShare(Platform platform, ShareParams paramsToShare) {
				if (platform != null && platform.isValid()) {
					Intent i = new Intent("action_referesh_user");
					i.putExtra("username", platform.getDb().getUserName());
					i.putExtra("password", "");
					i.putExtra("avatarurl", platform.getDb().getUserIcon());
					i.putExtra("signature", "暂无");
					sendBroadcast(i);
				}
				
			}
		});

		// 去除注释，演示在九宫格设置自定义的图标
		// Bitmap logo = BitmapFactory.decodeResource(menu.getResources(),
		// R.drawable.ic_launcher);
		// String label = menu.getResources().getString(R.string.app_name);
		// OnClickListener listener = new OnClickListener() {
		// public void onClick(View v) {
		// String text = "Customer Logo -- ShareSDK " +
		// ShareSDK.getSDKVersionName();
		// Toast.makeText(menu.getContext(), text, Toast.LENGTH_SHORT).show();
		// oks.finish();
		// }
		// };
		// oks.setCustomerLogo(logo, label, listener);

		oks.show(this);
	}

	private void requestInfo() {
		if (infoReq != null) {
			mapiService().abort(infoReq, this, true);
		}
		infoReq = BasicMApiRequest.mapiGet(
				"http://web358082.dnsvhost.com/ACservice/ACService.svc/GetArtworkByID/"
						+ artworkID, CacheType.NORMAL,
				GetArtworkByIDResult.class);
		mapiService().exec(infoReq, this);
	}

	private void requestImages() {
		if (imgReq != null) {
			mapiService().abort(imgReq, this, true);
		}
		imgReq = BasicMApiRequest
				.mapiGet(
						"http://web358082.dnsvhost.com/ACservice/ACService.svc/GetImageUrls/"
								+ artworkID, CacheType.NORMAL,
						GetImageUrlsResult.class);
		mapiService().exec(imgReq, this);
	}

	private void setArtwork(Artwork artwork) {
		if (artwork == null) {
			return;
		}
		setTitle(artwork.ArtworkName);
		voteNumTv.setText(String.valueOf(artwork.VoteCount));
		commentNumTv.setText(String.valueOf(artwork.RetweetCount));
		StringBuilder sb = new StringBuilder();
		sb.append(getString(R.string.artwork_name)).append(artwork.ArtworkName)
				.append("\n");
		sb.append(getString(R.string.artwork_author)).append(artwork.Artist)
				.append("\n");
		sb.append(getString(R.string.artwork_time))
				.append(artwork.CreationDate).append("\n");
		sb.append(getString(R.string.artwork_intro))
				.append(artwork.ArtworkDesc).append("\n");
		introTv.setText(sb.toString());
		locTv.setText(artwork.Country + "," + artwork.City + ","
				+ artwork.Address);
	}

	class Adapter extends PagerAdapter {

		List<View> viewList = new ArrayList<View>();

		@Override
		public int getCount() {
			if (imagesResult == null || imagesResult.result() == null) {
				return 0;
			} else {
				return imagesResult.result().length;
			}
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			NetworkPhotoView imageView = new NetworkPhotoView(
					ArtworkInfoActivity.this);
			imageView.setScaleType(ScaleType.CENTER_CROP);
			ArtworkImage item = imagesResult.result()[position];
			if (item != null) {
				imageView.setImage(String.format(
						"http://web358082.dnsvhost.com/ACservice/pics/%s.jpg",
						item.ImageURL));
			}
			container.addView(imageView);

			if (position == viewList.size()) {
				viewList.add(imageView);
			}

			imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					List<String> images = new ArrayList<String>();
					for (ArtworkImage image : imagesResult.result()) {
						images.add(String
								.format("http://web358082.dnsvhost.com/ACservice/pics/%s.jpg",
										image.ImageURL));
					}
					Intent i = new Intent(Intent.ACTION_VIEW, Uri
							.parse("pam://photopager"));
					i.putExtra("images", images.toArray(new String[0]));
					startActivity(i);
				}
			});

			return imageView;
		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(viewList.get(position));
		}

	}

	@Override
	public void onRequestFailed(MApiRequest req, MApiResponse resp) {
		if (req == infoReq) {
		} else if (req == imgReq) {
		} else if (req == voteReq) {
			showDialog(getString(R.string.app_name),
					getString(R.string.msg_vote_failed), null);
		}
	}

	@Override
	public void onRequestFinish(MApiRequest req, MApiResponse resp) {
		if (req == infoReq) {
			if (resp.result() instanceof GetArtworkByIDResult) {
				infoResult = ((GetArtworkByIDResult) resp.result());
				setArtwork(infoResult.result());
			}

		} else if (req == imgReq) {
			if (resp.result() instanceof GetImageUrlsResult) {
				imagesResult = ((GetImageUrlsResult) resp.result());
				adapter.notifyDataSetChanged();
			}
			imgLoadingPb.setVisibility(View.GONE);
		} else if (req == voteReq) {
			if (resp.result() instanceof VoteResult) {
				VoteResult result = (VoteResult) resp.result();
				if (!result.VoteResult.hasError()) {
					int voteNum = Integer.valueOf(voteNumTv.getText()
							.toString()) + 1;
					voteNumTv.setText(String.valueOf(voteNum));
					showDialog(getString(R.string.app_name),
							getString(R.string.msg_vote_success), null);
				} else {
					showDialog(getString(R.string.app_name),
							result.VoteResult.ErrorDesc, null);
				}

			} else {
				showDialog(getString(R.string.app_name),
						getString(R.string.msg_vote_failed), null);
			}
		} else if (req == createUserReq) {
			if (resp.result() instanceof CreateUserResult) {
				CreateUserResult result = (CreateUserResult) resp.result();
				preferences().edit().putString("uid",
						result.CreateUserResult.ID);
				Environment.setUserID(result.CreateUserResult.ID);
			}
		}
	}

	@Override
	public void onRequestProgress(MApiRequest req, int arg1, int arg2) {
	}

	@Override
	public void onRequestStart(MApiRequest req) {
	}

}
