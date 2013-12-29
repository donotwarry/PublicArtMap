package com.facsu.publicartmap.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.dennytech.common.app.CLFragment;
import com.facsu.publicartmap.R;
import com.facsu.publicartmap.widget.TitleBar;

/**
 * base fragment
 * 
 * @author dengjun86
 * 
 */
public class PMFragment extends CLFragment {

	private TitleBar titleBar;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		titleBar = (TitleBar) view.findViewById(R.id.titlebar);
	}

	public void setTitle(String title) {
		if (titleBar != null) {
			titleBar.setTitle(title.toString());
		} else {
			getActivity().setTitle(title);
		}

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

	public void setTitle(int resId) {
		setTitle(getString(resId));
	}

	// ////////////
	// Dialog相关
	// ////////////

	protected Dialog managedDialog;

	/**
	 * 显示progress对话框 </br></br> progress对话框应该只在<b>主线程</b>中被显示
	 * 
	 * @param message
	 */
	final public void showProgressDialog(String message) {
		dismissDialog();
		ProgressDialog dlg = new ProgressDialog(getActivity());
		dlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				onProgressDialogCancel();
			}
		});
		dlg.setMessage(message);

		managedDialog = dlg;
		dlg.show();
	}

	protected void onProgressDialogCancel() {
		// TO OVERRIDE
	}

	final public void showDialog(String title, String message,
			DialogInterface.OnClickListener listener) {
		dismissDialog();
		AlertDialog dlg = createDialog(title, message);
		dlg.setButton(DialogInterface.BUTTON_POSITIVE, "确定", listener);

		managedDialog = dlg;
		dlg.show();
	}

	final public void showDialog(String title, String message,
			String positiveText,
			DialogInterface.OnClickListener positiveListener,
			String negativeText,
			DialogInterface.OnClickListener negativeListener) {
		dismissDialog();
		AlertDialog dlg = createDialog(title, message);
		dlg.setButton(DialogInterface.BUTTON_POSITIVE, positiveText,
				positiveListener);
		dlg.setButton(DialogInterface.BUTTON_NEGATIVE, negativeText,
				negativeListener);

		managedDialog = dlg;
		dlg.show();
	}

	final public AlertDialog createDialog(String title, String message) {
		AlertDialog dlg = new AlertDialog.Builder(getActivity()).create();
		dlg.setTitle(title);
		dlg.setMessage(message);

		managedDialog = dlg;
		return dlg;
	}

	final public void dismissDialog() {
		if (managedDialog != null && managedDialog.isShowing()) {
			managedDialog.dismiss();
		}
		managedDialog = null;
	}
}
