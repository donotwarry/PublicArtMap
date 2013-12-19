package com.facsu.publicartmap.explore;

import uk.co.senab.photoview.PhotoView;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.facsu.publicartmap.app.PMActivity;
import com.facsu.publicartmap.widget.HackyViewPager;

public class PhotoPagerActivity extends PMActivity {

	private ViewPager mViewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String[] images = getIntent().getStringArrayExtra("images");

		mViewPager = new HackyViewPager(this);
		setContentView(mViewPager);

		mViewPager.setAdapter(new SamplePagerAdapter(images));
	}

	static class SamplePagerAdapter extends PagerAdapter {

		private String[] images;

		public SamplePagerAdapter(String[] images) {
			this.images = images;
		}

		@Override
		public int getCount() {
			return images.length;
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			PhotoView photoView = new PhotoView(container.getContext());
			photoView.setImage(images[position]);
			photoView.setBackgroundColor(container.getContext().getResources()
					.getColor(android.R.color.black));

			// Now just add PhotoView to ViewPager and return it
			container.addView(photoView, LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);

			return photoView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

	}
}
