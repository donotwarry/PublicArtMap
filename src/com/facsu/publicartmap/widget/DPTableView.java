package com.facsu.publicartmap.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.dennytech.common.util.BDUtils;
import com.facsu.publicartmap.R;

public class DPTableView extends LinearLayout {

	private Context mContext;
	private int mIndexController = 0;
	private ItemClickListener mClickListener;

	public DPTableView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		build();
	}

	/**
	 * change the default style to table style
	 */
	public void build() {
		List<View> visiableItems = visiableItemList();
		if (visiableItems == null) {
			this.setVisibility(GONE);
			return;
		} else {
			this.setVisibility(VISIBLE);
		}

		mIndexController = 0;
		int dip10 = BDUtils.dip2px(mContext, 10);

		if (visiableItems.size() > 1) {
			for (int i = 0; i < visiableItems.size(); i++) {
				View tempItemView = visiableItems.get(i);

				if (mIndexController == 0) {
					tempItemView
							.setBackgroundResource(R.drawable.background_view_rounded_top);
				} else if (mIndexController == visiableItems.size() - 1) {
					tempItemView
							.setBackgroundResource(R.drawable.background_view_rounded_bottom);
				} else {
					tempItemView
							.setBackgroundResource(R.drawable.background_view_rounded_middle);
				}
				if (tempItemView instanceof DPBasicItem)
					tempItemView.setPadding(dip10, dip10, dip10, dip10);

				if (tempItemView.isClickable()) {
					tempItemView.setTag(mIndexController);
					tempItemView.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View view) {
							if (mClickListener != null)
								mClickListener.onItemClick(view,
										(Integer) view.getTag());
						}

					});
				}
				mIndexController++;
			}
		} else if (visiableItems.size() == 1) {
			View examView = visiableItems.get(0);
			examView.setBackgroundResource(R.drawable.background_view_rounded_single);
			if (examView instanceof DPBasicItem)
				examView.setPadding(dip10, dip10, dip10, dip10);

			if (examView.isClickable()) {
				examView.setTag(mIndexController);
				examView.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						if (mClickListener != null)
							mClickListener.onItemClick(view,
									(Integer) view.getTag());
					}

				});
			}
		}
	}

	private List<View> visiableItemList() {
		if (getChildCount() == 0) {
			return null;
		}

		List<View> tableItems = new ArrayList<View>();

		for (int i = 0; i < getChildCount(); i++) {
			View tempItemView = getChildAt(i);

			if (tempItemView.getVisibility() != View.GONE) {
				tableItems.add(tempItemView);
			}
		}

		return tableItems;
	}

	@Override
	public void addView(View child) {
		if (child instanceof DPBasicItem)
			((DPBasicItem) child).build();

		super.addView(child);
	}

	public interface ItemClickListener {
		void onItemClick(View itemView, int index);
	}

	public void setOnItemClickListener(ItemClickListener listener) {
		this.mClickListener = listener;
	}

	public void removeClickListener() {
		this.mClickListener = null;
	}

}
