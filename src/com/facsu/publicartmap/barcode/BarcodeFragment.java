package com.facsu.publicartmap.barcode;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.dennytech.common.service.dataservice.mapi.CacheType;
import com.dennytech.common.service.dataservice.mapi.MApiRequest;
import com.dennytech.common.service.dataservice.mapi.MApiRequestHandler;
import com.dennytech.common.service.dataservice.mapi.MApiResponse;
import com.dennytech.common.service.dataservice.mapi.MApiService;
import com.dennytech.common.service.dataservice.mapi.impl.BasicMApiRequest;
import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMApplication;
import com.facsu.publicartmap.bean.GetArtworksBySNResult;
import com.facsu.publicartmap.widget.TitleBar;
import com.google.zxing.Result;
import com.welcu.android.zxingfragmentlib.BarCodeScannerFragment;

public class BarcodeFragment extends BarCodeScannerFragment implements
		MApiRequestHandler {

	private TitleBar title;

	private MApiService mapi;
	private MApiRequest request;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mapi = PMApplication.instance().mapiService();

		this.setmCallBack(new IResultCallback() {
			@Override
			public void result(Result lastResult) {
				stopScan();
				requestArtworks(lastResult.toString());
				Toast.makeText(getActivity(), "Scan: " + lastResult.toString(),
						Toast.LENGTH_SHORT).show();
			}
		});

	}

	@Override
	protected int contentResId() {
		return R.layout.fragment_barcode;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		title = (TitleBar) view.findViewById(R.id.titlebar);
		title.setTitle(getString(R.string.title_barcode));
		title.setLeftButton(0, null);
	}

	private void requestArtworks(String sn) {
		if (request != null) {
			mapi.abort(request, this, true);
		}
		request = BasicMApiRequest.mapiGet(
				"http://web358082.dnsvhost.com/acservice/acservice.svc/GetArtworksBySN/"
						+ sn, CacheType.NORMAL, GetArtworksBySNResult.class);
		mapi.exec(request, this);

		showProgressDialog(getString(R.string.msg_loading));
	}

	@Override
	public void onRequestFailed(MApiRequest arg0, MApiResponse arg1) {
		dismissDialog();
		showDialog(getString(R.string.app_name),
				getString(R.string.msg_not_found), new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						startScan();
					}
				});
	}

	@Override
	public void onRequestFinish(MApiRequest arg0, MApiResponse resp) {
		dismissDialog();
		if (resp.result() instanceof GetArtworksBySNResult) {
			GetArtworksBySNResult result = (GetArtworksBySNResult) resp.result();
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("pam://artworkinfo"));
			if (result.result() != null && result.result().length > 0) {
				intent.putExtra("artwork", result.result()[0]);
			}
			startActivity(intent);

		} else {
			showDialog(getString(R.string.app_name),
					getString(R.string.msg_not_found), new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							startScan();
						}
					});
		}
	}

	@Override
	public void onRequestProgress(MApiRequest arg0, int arg1, int arg2) {
	}

	@Override
	public void onRequestStart(MApiRequest arg0) {
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
