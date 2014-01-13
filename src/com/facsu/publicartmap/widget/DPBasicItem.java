package com.facsu.publicartmap.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facsu.publicartmap.R;

public class DPBasicItem extends LinearLayout {
	private Context mContext;

	private LinearLayout itemTitleLay;
	private TextView itemTitle;
	private TextView itemSubtitle;
	private DPEditText itemInput;
	private TextView itemCount;
	private CheckBox itemCheckBox;
	private ImageView itemArrow;

	private String title;
	private String subTitle;
	private String input;
	private String input_hint;
	private int input_type;
	private int input_maxLength;
	private String count;
	private int checked;
	private boolean clickable;

	private int title_textType;
	private int subTitle_textType;
	private int count_textType;
	private int input_textType;

	public static final int TEXT_TYPE_SMALL = 0x01;
	public static final int TEXT_TYPE_YELLOW_COLOR = TEXT_TYPE_SMALL << 1;
	public static final int TEXT_TYPE_GRAY_COLOR = TEXT_TYPE_SMALL << 2;
	public static final int TEXT_TYPE_BLACK_COLOR = TEXT_TYPE_SMALL << 3;
	public static final int TEXT_TYPE_BOLD = TEXT_TYPE_SMALL << 4;

	public DPBasicItem(Context context) {
		this(context, null);
		mContext = context;

		this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
	}

	public DPBasicItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.DPBasicItem);

		title = a.getString(R.styleable.DPBasicItem_title);
		subTitle = a.getString(R.styleable.DPBasicItem_subTitle);
		input = a.getString(R.styleable.DPBasicItem_input);
		input_hint = a.getString(R.styleable.DPBasicItem_input_hint);
		input_type = a.getInt(R.styleable.DPBasicItem_input_type,
				InputType.TYPE_CLASS_TEXT);
		input_maxLength = a.getInt(R.styleable.DPBasicItem_input_maxLength, 0);
		count = a.getString(R.styleable.DPBasicItem_count);
		checked = a.getInt(R.styleable.DPBasicItem_checked, 0);// 1 true, 2
																// false, 0 gone
		title_textType = a.getInt(R.styleable.DPBasicItem_title_textType, 0);
		subTitle_textType = a.getInt(R.styleable.DPBasicItem_subTitle_textType,
				0);
		count_textType = a.getInt(R.styleable.DPBasicItem_count_textType, 0);
		input_textType = a.getInt(R.styleable.DPBasicItem_input_textType, 0);
		clickable = a.getBoolean(R.styleable.DPBasicItem_clickable, false);
		a.recycle();

		setupView(context);
	}

	private void setupView(Context context) {
		Resources resource = context.getResources();
		ColorStateList csl = (ColorStateList) resource
				.getColorStateList(R.color.text_color_selector);

		// title lay
		itemTitleLay = new LinearLayout(context);
		itemTitleLay.setDuplicateParentStateEnabled(true);

		// title
		itemTitle = new TextView(context);
		itemTitle.setId(R.id.itemTitle);
		itemTitle.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		itemTitle.setText(title);
		itemTitle.setDuplicateParentStateEnabled(true);
		itemTitle.setTextAppearance(context,
				android.R.style.TextAppearance_Medium);
		itemTitle.setTextColor(csl);
		itemTitle.setSingleLine(true);
		itemTitle.setEllipsize(TruncateAt.END);
		itemTitle.setPadding(0, 0, dip2px(10), 0);
		itemTitle.setTypeface(Typeface
				.create(Typeface.DEFAULT, Typeface.NORMAL));
		setTextType(itemTitle, title_textType);
		itemTitleLay.addView(itemTitle);

		// subTitle
		itemSubtitle = new TextView(context);
		itemSubtitle.setId(R.id.itemSubTitle);
		itemSubtitle.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		itemSubtitle.setText(subTitle);
		itemSubtitle.setDuplicateParentStateEnabled(true);
		itemSubtitle.setTextAppearance(context,
				android.R.style.TextAppearance_Medium);
		itemSubtitle.setTextColor(csl);
		itemSubtitle.setSingleLine(true);
		itemSubtitle.setEllipsize(TruncateAt.END);
		setTextType(itemSubtitle, subTitle_textType);
		itemTitleLay.addView(itemSubtitle);
		addView(itemTitleLay);

		// itemInput
		itemInput = new DPEditText(context);
		itemInput.setId(R.id.itemInput);
		LayoutParams inputLayoutParams = new LayoutParams(0,
				LayoutParams.WRAP_CONTENT, 1);
		itemInput.setLayoutParams(inputLayoutParams);
		itemInput.setGravity(Gravity.CENTER_VERTICAL);
		itemInput.setText(input);
		itemInput.setDuplicateParentStateEnabled(true);
		itemInput.setTextAppearance(context,
				android.R.style.TextAppearance_Medium);
		itemInput.setTextColor(csl);
		itemInput.setSingleLine(true);
		itemInput.setEllipsize(TruncateAt.END);
		itemInput.setHint(input_hint);
		itemInput.setInputType(input_type);
		itemInput.setMaxLength(input_maxLength);
		itemInput.setBackgroundDrawable(null);
		itemInput.setPadding(0, 0, 0, 0);
		setTextType(itemInput, input_textType);
		addView(itemInput);

		// itemCount
		itemCount = new TextView(context);
		itemCount.setId(R.id.itemCount);
		itemCount.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		itemCount.setText(count);
		itemCount.setMaxWidth(dip2px(180));
		itemCount.setDuplicateParentStateEnabled(true);
		itemCount.setTextAppearance(context,
				android.R.style.TextAppearance_Medium);
		itemCount.setTextColor((ColorStateList) resource
				.getColorStateList(R.color.text_gray_color_selector));
		itemCount.setPadding(0, 0, 0, 0);
		setTextType(itemCount, count_textType);
		addView(itemCount);

		// itemCheckBox
		itemCheckBox = new CheckBox(context);
		itemCheckBox.setId(R.id.itemCheckBox);
		itemCheckBox.setLayoutParams(new LayoutParams(dip2px(26), dip2px(25)));
		itemCheckBox.setChecked(checked == 1 ? true : false);
		itemCheckBox.setPadding(0, 0, 0, 0);
		addView(itemCheckBox);

		// itemArrow
		itemArrow = new ImageView(context);
		itemArrow.setId(R.id.itemArrow);
		itemArrow.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		itemArrow.setPadding(dip2px(10), 0, 0, 0);
		itemArrow.setDuplicateParentStateEnabled(true);
		itemArrow.setImageResource(R.drawable.arrow);
		addView(itemArrow);

		build();

		setGravity(Gravity.CENTER_VERTICAL);
		setMinimumHeight(dip2px(45));
	}

	/**
	 * set the view's visibility
	 */
	public void build() {
		itemTitle.setVisibility(title == null ? View.GONE : View.VISIBLE);
		itemSubtitle.setVisibility(subTitle == null ? View.GONE : View.VISIBLE);
		itemInput
				.setVisibility(input_hint != null || input != null ? View.VISIBLE
						: View.GONE);
		itemCount.setVisibility(count != null ? View.VISIBLE : View.GONE);
		itemCheckBox.setVisibility(checked == 0 ? View.GONE : View.VISIBLE);
		itemArrow.setVisibility(isClickable() ? View.VISIBLE : View.GONE);

		if (input_hint != null || input != null) {
			itemTitleLay.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0));
		} else {
			itemTitleLay.setLayoutParams(new LayoutParams(0,
					LayoutParams.WRAP_CONTENT, 1));
		}

		if (input_hint != null || subTitle != null)
			title_textType |= DPBasicItem.TEXT_TYPE_GRAY_COLOR;
		setTextType(itemTitle, title_textType);

		setClickable(clickable);
	}

	private int dip2px(float dipValue) {
		float scale = mContext.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	private void setTextType(TextView tv, int textType) {
		Resources resource = (Resources) mContext.getResources();
		if (textType == 0) {
			return;
		}

		if ((textType & 0x01) == TEXT_TYPE_SMALL)
			tv.setTextAppearance(mContext, R.style.content_page_small_text);

		if ((textType & 0x02) == TEXT_TYPE_YELLOW_COLOR) {
			ColorStateList csl = (ColorStateList) resource
					.getColorStateList(R.color.text_yellow_color_selector);
			tv.setTextColor(csl);
		}

		if ((textType & 0x04) == TEXT_TYPE_GRAY_COLOR) {
			ColorStateList csl = (ColorStateList) resource
					.getColorStateList(R.color.text_gray_color_selector);
			tv.setTextColor(csl);
		}

		if ((textType & 0x08) == TEXT_TYPE_BLACK_COLOR) {
			ColorStateList csl = (ColorStateList) resource
					.getColorStateList(R.color.text_color_selector);
			tv.setTextColor(csl);
		}

		if ((textType & 0x10) == TEXT_TYPE_BOLD)
			tv.getPaint().setFakeBoldText(true);
		else
			tv.getPaint().setFakeBoldText(false);
	}

	@Override
	protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
		int inputId = R.id.itemInput - getId();
		DPEditText inputChild = (DPEditText) findViewById(R.id.itemInput);
		if (inputChild == null) {
			super.dispatchSaveInstanceState(container);
			return;
		} else {
			Parcelable state = inputChild.onSaveInstanceState();
			if (state != null) {
				container.put(inputId, state);
			}
		}

		int checkboxId = R.id.itemCheckBox ^ getId();
		CheckBox checkboxChild = (CheckBox) findViewById(R.id.itemCheckBox);
		Parcelable state = checkboxChild.onSaveInstanceState();
		if (state != null) {
			container.put(checkboxId, state);
		}
	}

	@Override
	protected void dispatchRestoreInstanceState(
			SparseArray<Parcelable> container) {
		int inputId = R.id.itemInput - getId();
		DPEditText inputChild = (DPEditText) findViewById(R.id.itemInput);
		if (inputChild == null) {
			super.dispatchRestoreInstanceState(container);
			return;
		} else {
			Parcelable state = container.get(inputId);
			if (state != null) {
				inputChild.onRestoreInstanceState(state);
			}
		}

		int checkboxId = R.id.itemCheckBox ^ getId();
		CheckBox checkboxChild = (CheckBox) findViewById(R.id.itemCheckBox);
		Parcelable state = container.get(checkboxId);
		if (state != null) {
			checkboxChild.onRestoreInstanceState(state);
		}
	}

	@Override
	public boolean isClickable() {
		return clickable;
	}

	@Override
	public void setClickable(boolean clickable) {
		super.setClickable(clickable);
		this.clickable = clickable;
	}

	public String getInputHint() {
		return input_hint;
	}

	public void setHint(String text) {
		this.input_hint = text;
		itemInput.setHint(text);
	}

	public String getInputText() {
		return input;
	}

	public void setInputText(String inputText) {
		this.input = inputText;
		itemInput.setText(inputText);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String mTitle) {
		this.title = mTitle;
		itemTitle.setText(mTitle);
	}

	Spanned titleSpan;

	public void setTitle(Spanned spanText) {
		this.titleSpan = spanText;
		itemTitle.setText(spanText);
	}

	public String getSubTitle() {
		return subTitle;
	}

	public void setSubTitle(String mSubtitle) {
		this.subTitle = mSubtitle;
		itemSubtitle.setText(mSubtitle);
	}

	public String getCount() {
		return count;
	}

	public void setCount(String countText) {
		this.count = countText;
		itemCount.setText(countText);
	}

	public int getInputType() {
		return input_type;
	}

	public void setInputType(int inputType) {
		this.input_type = inputType;
		itemInput.setInputType(inputType);
	}

	public int getTitleTextType() {
		return title_textType;
	}

	public void setTitleTextType(int textType) {
		this.title_textType = textType;
		setTextType(itemTitle, textType);
	}

	public int getSubTitleTextType() {
		return subTitle_textType;
	}

	public void setSubTitleTextType(int textType) {
		this.subTitle_textType = textType;
		setTextType(itemSubtitle, textType);
	}

	public int getCountTextType() {
		return count_textType;
	}

	public void setCountTextType(int textType) {
		this.count_textType = textType;
		setTextType(itemCount, textType);
	}

	public int getInputTextType() {
		return input_textType;
	}

	public void setInputTextType(int textType) {
		this.input_textType = textType;
		setTextType(itemInput, textType);
	}

	public void setInputMaxLength(int maxLength) {
		this.input_maxLength = maxLength;
		itemInput.setMaxLength(maxLength);
	}

	public int getInputMaxLength() {
		return input_maxLength;
	}

	public TextView itemTitle() {
		return itemTitle;
	}
	
	public LinearLayout getItemTitleLay() {
		return itemTitleLay;
	}

	public TextView getItemTitle() {
		return itemTitle;
	}

	public TextView getItemSubtitle() {
		return itemSubtitle;
	}

	public DPEditText getItemInput() {
		return itemInput;
	}

	public TextView getItemCount() {
		return itemCount;
	}

	public CheckBox getItemCheckBox() {
		return itemCheckBox;
	}

	public ImageView getItemArrow() {
		return itemArrow;
	}

}
