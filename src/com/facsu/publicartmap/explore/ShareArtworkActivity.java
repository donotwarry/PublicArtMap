package com.facsu.publicartmap.explore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dennytech.common.service.dataservice.mapi.CacheType;
import com.dennytech.common.service.dataservice.mapi.MApiRequest;
import com.dennytech.common.service.dataservice.mapi.MApiRequestHandler;
import com.dennytech.common.service.dataservice.mapi.MApiResponse;
import com.dennytech.common.util.Log;
import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMActivity;
import com.facsu.publicartmap.bean.CreateArtworkResult;
import com.facsu.publicartmap.bean.Location;
import com.facsu.publicartmap.bean.UploadImageResult;
import com.facsu.publicartmap.common.APIRequest;
import com.facsu.publicartmap.common.Environment;
import com.facsu.publicartmap.utils.PhotoPicker;

public class ShareArtworkActivity extends PMActivity implements
		OnClickListener, MApiRequestHandler {

	private static final String TAG = ShareArtworkActivity.class
			.getSimpleName();

	private TextView addressTv;
	private ImageView sharePhoto;
	private EditText input;

	private Location location;
	private Bitmap showBitmap;
	private String strImgPath;

	private MApiRequest createReq;
	private MApiRequest uploadImgReq;

	private PhotoPicker photoPicker = new PhotoPicker(this) {
		protected Intent getPhotoPickIntent() {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			return intent;
		};
	};

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_share);
		setTitle(getString(R.string.title_share));
		setRightButton(R.drawable.title_send, new OnClickListener() {

			@Override
			public void onClick(View v) {
				createArtwork();
			}
		});

		location = getIntent().getParcelableExtra("location");

		addressTv = (TextView) findViewById(R.id.share_location);
		addressTv.setText(location.address);
		sharePhoto = (ImageView) findViewById(R.id.share_add_photo);
		sharePhoto.setOnClickListener(this);
		input = (EditText) findViewById(R.id.input);
		input.setText(getString(R.string.msg_share_text));
	}

	@Override
	protected void onDestroy() {
		if (createReq != null) {
			mapiService().abort(createReq, this, true);
		}
		if (uploadImgReq != null) {
			mapiService().abort(uploadImgReq, this, true);
		}
		super.onDestroy();
	}

	private void createArtwork() {
		if (strImgPath == null) {
			showDialog(getString(R.string.app_name),
					getString(R.string.msg_take_photo_first), null);
			return;
		}

		if (createReq != null) {
			mapiService().abort(createReq, this, true);
		}
		if (uploadImgReq != null) {
			mapiService().abort(uploadImgReq, this, true);
		}

		Map<String, String> map = new HashMap<String, String>();
		map.put("ArtworkName", "我的上传");
		map.put("ArtworkDesc", input.getText().toString().trim());
		map.put("SubmitterID", Environment.userID());
		map.put("Country", "");
		map.put("Artist", Environment.userName());
		map.put("City", location.city);
		map.put("Address", location.address);
		map.put("Longitude", String.valueOf(location.longitude));
		map.put("Latitude", String.valueOf(location.latitude));
		map.put("Status", "UA");
		map.put("ArtistCountry", location.address);
		map.put("StartYear", "");
		map.put("EndYear", "");
		map.put("StartMonth", "");
		map.put("EndMonth", "");
		createReq = APIRequest
				.mapiPost(
						"http://web358082.dnsvhost.com/ACservice/ACService.svc/CreateArtwork",
						CreateArtworkResult.class, map);
		mapiService().exec(createReq, this);

		showProgressDialog(getString(R.string.msg_submiting));
	}

	private void uploadImage(String aid) {
		if (strImgPath == null) {
			return;
		}

		if (uploadImgReq != null) {
			mapiService().abort(uploadImgReq, this, true);
		}

		File uploadImg = new File(strImgPath);
		try {
			FileInputStream is = new FileInputStream(uploadImg);
			uploadImgReq = new APIRequest(
					"http://web358082.dnsvhost.com/ACservice/ACService.svc/UploadImage/"
							+ aid + "/" + Environment.userID(),
					APIRequest.POST, is, CacheType.DISABLED,
					UploadImageResult.class, null);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onClick(View v) {
		if (v == sharePhoto) {
			if (!SDCardExists()) {
				Toast.makeText(this, getString(R.string.msg_have_no_sdcard),
						Toast.LENGTH_SHORT).show();
				return;
			}

			photoPicker.doTakePhoto();

			// String[] items = { getString(R.string.artwork_take_photo),
			// getString(R.string.artwork_pick_photo) };
			// AlertDialog.Builder builder = new AlertDialog.Builder(this);
			// ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
			// android.R.layout.simple_list_item_1, items);
			//
			// builder.setSingleChoiceItems(adapter, -1,
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// dialog.dismiss();
			// switch (which) {
			// case 0:
			// // take a new photo
			// photoPicker.doTakePhoto();
			// break;
			//
			// case 1:
			// // pick a photo from gallery
			// photoPicker.doPickPhotoFromGallery();
			// break;
			// }
			// }
			// });
			// builder.create().show();
		}
	}

	private boolean SDCardExists() {
		return (android.os.Environment.getExternalStorageState()
				.equals(android.os.Environment.MEDIA_MOUNTED));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case PhotoPicker.REQUEST_CODE_CAMERA:
				strImgPath = photoPicker.strImgPath();
				if (!TextUtils.isEmpty(strImgPath)) {
					File f = new File(strImgPath);
					if (!f.exists()) {
						strImgPath = photoPicker.parseImgPath(data);
					}

				} else {
					strImgPath = photoPicker.parseImgPath(data);
				}
				if (!TextUtils.isEmpty(strImgPath)) {
					sharePhoto.setImageBitmap(parseThumbnail(strImgPath));
				}
				break;
			case PhotoPicker.REQUEST_CODE_PHOTO_PICKED:
				strImgPath = photoPicker.parseImgPath(data);
				if (!TextUtils.isEmpty(strImgPath)) {
					sharePhoto.setImageBitmap(parseThumbnail(strImgPath));
				}
				break;
			}
		}

	}

	private Bitmap parseThumbnail(String file) {
		System.gc();
		int sampling = 1;
		try {
			FileInputStream ins = new FileInputStream(file);
			Options opt = new Options();
			opt.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(ins, null, opt);
			ins.close();

			int size = opt.outWidth > opt.outHeight ? opt.outWidth
					: opt.outHeight;
			if (size < 1400)
				sampling = 1;
			else if (size < 2800)
				sampling = 2;
			else
				sampling = 4;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		showBitmap = null;

		for (; sampling <= 8 && showBitmap == null; sampling *= 2) {
			try {
				FileInputStream ins = new FileInputStream(file);
				Options opt = new Options();
				opt.inSampleSize = sampling;
				showBitmap = BitmapFactory.decodeStream(ins, null, opt);
				ins.close();
			} catch (OutOfMemoryError oom) {
				System.gc();
				Toast.makeText(this, getString(R.string.msg_oom),
						Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		return showBitmap;
	}

	@Override
	public void onRequestStart(MApiRequest req) {
	}

	@Override
	public void onRequestProgress(MApiRequest req, int count, int total) {
	}

	@Override
	public void onRequestFinish(MApiRequest req, MApiResponse resp) {
		if (req == createReq) {
			if (resp.result() instanceof CreateArtworkResult) {
				CreateArtworkResult result = (CreateArtworkResult) resp
						.result();
				if (!result.CreateArtworkResult.hasError()) {
					uploadImage(result.CreateArtworkResult.ID);
					Log.i(TAG, "create artwork success, start upload image...");

				} else {
					dismissDialog();
					showDialog(getString(R.string.app_name),
							result.CreateArtworkResult.ErrorDesc, null);
				}
			}

		} else if (req == uploadImgReq) {
			dismissDialog();
			if (resp.result() instanceof UploadImageResult) {
				UploadImageResult result = (UploadImageResult) resp.result();
				if (!result.UploadImageResult.hasError()) {
					showDialog(getString(R.string.app_name),
							getString(R.string.msg_upload_success), null);
					return;
				} else {
					showDialog(getString(R.string.app_name),
							result.UploadImageResult.ErrorDesc, null);
				}
			} else {
				Toast.makeText(this, getString(R.string.msg_failed),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onRequestFailed(MApiRequest req, MApiResponse resp) {
		dismissDialog();
		showDialog(getString(R.string.app_name), resp.message().getErrorMsg(),
				null);
	}

}
