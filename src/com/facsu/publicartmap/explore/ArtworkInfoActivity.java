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
import com.facsu.publicartmap.bean.VoteResult;
import com.facsu.publicartmap.common.Environment;
import com.facsu.publicartmap.widget.NetworkPhotoView;
import com.umeng.socialize.bean.SocializeUser;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.UMSsoHandler;
import com.umeng.socialize.controller.listener.SocializeListeners.FetchUserListener;
import com.umeng.socialize.media.UMImage;

public class ArtworkInfoActivity extends PMActivity implements
		MApiRequestHandler, OnClickListener {

	final UMSocialService mController = UMServiceFactory.getUMSocialService(
			"com.umeng.share", RequestType.SOCIAL);

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
			// TODO

		} else if (v.getId() == R.id.artworkinfo_location) {
			// TODO
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
			// 设置分享内容
			mController.setShareContent(getString(R.string.msg_share_text));
			// 设置分享图片, 参数2为图片的url地址
			if (imagesResult != null) {
				mController.setShareMedia(new UMImage(this, String.format(
						"http://web358082.dnsvhost.com/ACservice/pics/%s.jpg",
						imagesResult.GetImageUrlsResult[imgPager
								.getCurrentItem()].ImageURL)));
				mController.openShare(this, false);
				mController.getUserInfo(this, new FetchUserListener() {
					@Override
					public void onStart() {
					}

					@Override
					public void onComplete(int status, SocializeUser user) {
						if (user != null) {
							Intent i = new Intent("action_referesh_user");
							sendBroadcast(i);
						}
					}
				});
			}

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/** 使用SSO授权必须添加如下代码 */
		UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(
				requestCode);
		if (ssoHandler != null) {
			ssoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
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
				preferences().edit().putString("uid", result.CreateUserResult.ID);
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
