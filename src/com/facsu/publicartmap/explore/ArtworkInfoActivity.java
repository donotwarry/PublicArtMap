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
import com.facsu.publicartmap.bean.GetArtworkByIDResult;
import com.facsu.publicartmap.bean.GetImageUrlsResult;
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
	private String artworkID;
	private GetArtworkByIDResult infoResult;
	private GetImageUrlsResult imagesResult;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_artworkinfo);
		imgPager = (ViewPager) findViewById(R.id.artworkinfo_pager);
		imgLoadingPb = findViewById(R.id.artworkinfo_images_loading);
		voteNumTv = (TextView) findViewById(R.id.artworkinfo_vote);
		commentNumTv = (TextView) findViewById(R.id.artworkinfo_comment_num);
		introTv = (TextView) findViewById(R.id.artworkinfo_intro);
		locTv = (TextView) findViewById(R.id.artworkinfo_location);
		locTv.setOnClickListener(this);
		findViewById(R.id.artworkinfo_comment_add).setOnClickListener(this);

		adapter = new Adapter();
		imgPager.setAdapter(adapter);

		Artwork artwork= getIntent().getParcelableExtra("artwork");
		artworkID = artwork == null ? getIntent().getData().getQueryParameter("id") : artwork.ArtworkID;
		
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
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.artworkinfo_comment_add) {
			// TODO

		} else if (v.getId() == R.id.artworkinfo_location) {
			// TODO
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
		}
	}

	@Override
	public void onRequestProgress(MApiRequest req, int arg1, int arg2) {
	}

	@Override
	public void onRequestStart(MApiRequest req) {
	}

}
