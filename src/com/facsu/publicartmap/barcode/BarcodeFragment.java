package com.facsu.publicartmap.barcode;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.facsu.publicartmap.R;
import com.facsu.publicartmap.widget.TitleBar;
import com.google.zxing.Result;
import com.welcu.android.zxingfragmentlib.BarCodeScannerFragment;

public class BarcodeFragment extends BarCodeScannerFragment {
	
	private TitleBar title;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setmCallBack(new IResultCallback() {
			@Override
			public void result(Result lastResult) {
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

}
