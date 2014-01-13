package com.facsu.publicartmap.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;

import com.dennytech.common.app.CLActivity;
import com.facsu.publicartmap.R;
import com.facsu.publicartmap.widget.TitleBar;

/**
 * base activity
 * 
 * @author dengjun86
 * 
 */
public class PMActivity extends CLActivity {

	private TitleBar titleBar;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
	}

	@Override
	public void setContentView(int layoutResID) {
		requestWindowFeature(customTitleType());
		super.setContentView(layoutResID);
		if (customTitleType() == Window.FEATURE_CUSTOM_TITLE) {
			initCustomTitle();
		}
	}

	@Override
	public void setContentView(View view) {
		requestWindowFeature(customTitleType());
		super.setContentView(view);
		if (customTitleType() == Window.FEATURE_CUSTOM_TITLE) {
			initCustomTitle();
		}
	}

	@Override
	public void setContentView(View view, ViewGroup.LayoutParams params) {
		requestWindowFeature(customTitleType());
		super.setContentView(view, params);
		if (customTitleType() == Window.FEATURE_CUSTOM_TITLE) {
			initCustomTitle();
		}
	}

	private void initCustomTitle() {
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		titleBar = (TitleBar) findViewById(R.id.titlebar);
		enableBackButton(true);
	}

	/**
	 * custom title type
	 * 
	 * @return
	 */
	protected int customTitleType() {
		return Window.FEATURE_CUSTOM_TITLE;
	}

	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		titleBar.setTitle(title == null ? "" : title.toString());
	}

	public void setLeftButton(int resId, OnClickListener listener) {
		titleBar.setLeftButton(resId, listener);
	}

	public void setRightButton(int resId, OnClickListener listener) {
		titleBar.setRightButton(resId, listener);
	}

	public void enableBackButton(boolean enable) {
		titleBar.enableBackButton(enable);
	}

	private SharedPreferences sharePref;

	public SharedPreferences preferences() {
		if (sharePref == null) {
			sharePref = getSharedPreferences(getPackageName(),
					Context.MODE_PRIVATE);
		}
		return sharePref;
	}
	
	//
	// login & register
	//
	
	public static final int REQUEST_CODE_LOGIN = 1001;

	public void gotoLogin() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("pam://login"));
		startActivityForResult(intent, REQUEST_CODE_LOGIN);
	}
	
	public void gotoRegister() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("pam://register"));
		startActivityForResult(intent, REQUEST_CODE_LOGIN);
	}
	
	public void onLoginSuccess() {
		// sub class implement
	}
	
	public void onLoginFailed() {
		// sub class implement
	}
	
	public void onLoginCancel() {
		// sub class implement
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_LOGIN) {
			if (resultCode == RESULT_OK) {
				onLoginSuccess();
			} else if (resultCode == RESULT_CANCELED) {
				onLoginCancel();
			} else {
				onLoginFailed();
			}
			
		}
	}

}
