package com.facsu.publicartmap.widget;

import android.content.Context;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.widget.EditText;

public class DPEditText extends EditText {

	int maxLength;

	public DPEditText(Context context) {
		this(context,null);
	}

	public DPEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
		if (maxLength > 0)
			setFilters(new InputFilter[] { new InputFilter.LengthFilter(
					maxLength) });
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		super.setText(text, type);
		if (text == null)
			text = "";
		this.setSelection(maxLength > 0 && maxLength < text.length() ? maxLength
				: text.length());
	}
}
