package com.facsu.publicartmap.explore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.GridLayout.LayoutParams;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dennytech.common.service.dataservice.mapi.MApiRequest;
import com.dennytech.common.service.dataservice.mapi.MApiRequestHandler;
import com.dennytech.common.service.dataservice.mapi.MApiResponse;
import com.dennytech.common.util.Log;
import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMActivity;
import com.facsu.publicartmap.app.PMApplication;
import com.facsu.publicartmap.bean.Artwork;
import com.facsu.publicartmap.bean.CreateArtworkResult;
import com.facsu.publicartmap.bean.Location;
import com.facsu.publicartmap.bean.UploadImageResult;
import com.facsu.publicartmap.bean.User;
import com.facsu.publicartmap.common.APIRequest;
import com.facsu.publicartmap.utils.OKSUtils;
import com.facsu.publicartmap.utils.PhotoPicker;

public class ShareArtworkActivity extends PMActivity implements
		OnClickListener, MApiRequestHandler {

	private static final String TAG = ShareArtworkActivity.class
			.getSimpleName();

	private TextView addressTv;
	private GridLayout shareGrid;
	private ImageView sharePhoto;
	private EditText input;

	private Location location;
	private List<Bitmap> showBitmaps = new ArrayList<Bitmap>();
	private String strImgPath;

	private MApiRequest createReq;
	private MApiRequest uploadImgReq;

	private String artworkID;
	private Artwork share;

	private PhotoPicker photoPicker = new PhotoPicker(this) {
		protected Intent getPhotoPickIntent() {
			Intent intent = new Intent();
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("image/*");
			if (Build.VERSION.SDK_INT < 19) {
				intent.setAction(Intent.ACTION_GET_CONTENT);
			} else {
				intent.setAction("android.intent.action.OPEN_DOCUMENT");
			}
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
		if (location == null) {
			location = PMApplication.instance().myLocation();
		}

		addressTv = (TextView) findViewById(R.id.share_location);
		addressTv.setText(location.address);
		shareGrid = (GridLayout) findViewById(R.id.share_photo_grid);
		sharePhoto = (ImageView) findViewById(R.id.share_add_photo);
		sharePhoto.setOnClickListener(this);
		input = (EditText) findViewById(R.id.input);
		input.setText(getString(R.string.msg_share_text));
		View agreenment = findViewById(R.id.agreenment);
		agreenment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(getString(R.string.app_name),
						getString(R.string.agreement),
						getString(R.string.text_agreen), null,
						getString(R.string.text_reject),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						}, new DialogInterface.OnCancelListener() {

							@Override
							public void onCancel(DialogInterface dialog) {
								finish();
							}
						});
			}
		});
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
		if (showBitmaps.size() == 0) {
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

		User user = User.read(preferences());

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ArtworkName", getString(R.string.my_share));
		map.put("ArtworkDesc", input.getText().toString().trim());
		map.put("SubmitterID", user.UID);
		map.put("Country", "");
		map.put("Artist", user.UserName);
		map.put("City", location.city);
		map.put("Address", location.address);
		map.put("Longitude", String.valueOf(location.longitude));
		map.put("Latitude", String.valueOf(location.latitude));
		map.put("Status", "UA");
		map.put("Type", "Normal");
		map.put("ArtistCountry", location.address);
		map.put("StartYear", "");
		map.put("EndYear", "");
		map.put("StartMonth", "");
		map.put("EndMonth", "");
		share = new Artwork(user.UserName, "我的上传",
				String.valueOf(location.latitude),
				String.valueOf(location.longitude));
		createReq = APIRequest
				.mapiPostJson(
						"http://web358082.dnsvhost.com/ACservice/ACService.svc/CreateArtwork",
						CreateArtworkResult.class, map);
		mapiService().exec(createReq, this);

		showProgressDialog(getString(R.string.msg_submiting));
	}

	private void uploadImage() {
		if (showBitmaps.size() == 0) {
			return;
		}

		if (uploadImgReq != null) {
			mapiService().abort(uploadImgReq, this, true);
		}

		User user = User.read(preferences());

		Map<String, String> map = new HashMap<String, String>();
		Bitmap waitBm = showBitmaps.remove(0);
		map.put("ImageData", bitmapToBase64(waitBm));
		map.put("ImageDesc", "");
		map.put("ImageSource", "");
		uploadImgReq = APIRequest.mapiPostJson(
				"http://web358082.dnsvhost.com/ACservice/ACService.svc/UploadImage/"
						+ artworkID + "/" + user.UID, UploadImageResult.class,
				map);
		mapiService().exec(uploadImgReq, this);
	}

	public String bitmapToBase64(Bitmap bitmap) {
		String result = null;
		ByteArrayOutputStream baos = null;
		try {
			if (bitmap != null) {
				baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				baos.flush();
				baos.close();
				byte[] bitmapBytes = baos.toByteArray();
				result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (baos != null) {
					baos.flush();
					baos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
	public void onClick(View v) {
		if (v == sharePhoto) {
			if (!SDCardExists()) {
				Toast.makeText(this, getString(R.string.msg_have_no_sdcard),
						Toast.LENGTH_SHORT).show();
				return;
			}

			String[] items = { getString(R.string.artwork_take_photo),
					getString(R.string.artwork_pick_photo) };
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, items);

			builder.setSingleChoiceItems(adapter, -1,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							switch (which) {
							case 0:
								// take a new photo
								photoPicker.doTakePhoto();
								break;

							case 1:
								// pick a photo from gallery
								photoPicker.doPickPhotoFromGallery();
								break;
							}
						}
					});
			builder.create().show();
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
					// sharePhoto.setImageBitmap(parseThumbnail(strImgPath));
					addThumb(parseThumbnail(strImgPath));
				}
				break;
			case PhotoPicker.REQUEST_CODE_PHOTO_PICKED:
				if (Build.VERSION.SDK_INT < 19) {
					strImgPath = photoPicker.parseImgPath(data);
					if (!TextUtils.isEmpty(strImgPath)) {
						// sharePhoto.setImageBitmap(parseThumbnail(strImgPath));
						addThumb(parseThumbnail(strImgPath));
					}
				} else {
					if (data != null) {
						// sharePhoto
						// .setImageBitmap(parseThumbnail(data.getData()));
						addThumb(parseThumbnail(data.getData()));
					}
				}
				break;
			}
		}
	}

	private void addThumb(Bitmap bitmap) {
		ImageView thumb = new ImageView(this);
		LayoutParams lp = new LayoutParams();
		lp.setMargins(OKSUtils.dip2px(this, 8), 0, 0, 0);
		lp.width = OKSUtils.dip2px(this, 70);
		lp.height = OKSUtils.dip2px(this, 70);
		thumb.setLayoutParams(lp);
		thumb.setImageBitmap(bitmap);
		int num = shareGrid.getChildCount();
		shareGrid.addView(thumb, num - 1);
	}

	private Bitmap parseThumbnail(String file) {
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

		Bitmap showBitmap = null;

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

		showBitmaps.add(showBitmap);

		return showBitmap;
	}

	private Bitmap parseThumbnail(Uri data) {
		int sampling = 1;
		try {
			InputStream ins = getContentResolver().openInputStream(data);
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

		Bitmap showBitmap = null;

		for (; sampling <= 8 && showBitmap == null; sampling *= 2) {
			try {
				InputStream ins = getContentResolver().openInputStream(data);
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

		showBitmaps.add(showBitmap);

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
					artworkID = result.CreateArtworkResult.ID;
					uploadImage();
					Log.i(TAG, "create artwork success, start upload image...");

				} else {
					dismissDialog();
					showDialog(getString(R.string.app_name),
							result.CreateArtworkResult.ErrorDesc, null);
				}
			}

		} else if (req == uploadImgReq) {
			if (showBitmaps.size() == 0) {
				dismissDialog();
				if (resp.result() instanceof UploadImageResult) {
					UploadImageResult result = (UploadImageResult) resp
							.result();
					if (!result.UploadImageResult.hasError()) {
						showDialog(getString(R.string.app_name),
								getString(R.string.msg_upload_success), new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										Intent data = new Intent();
										data.putExtra("share", share);
										setResult(RESULT_OK, data);
										finish();
									}
								});
						return;
					} else {
						showDialog(getString(R.string.app_name),
								result.UploadImageResult.ErrorDesc, null);
					}
				} else {
					Toast.makeText(this, getString(R.string.msg_failed),
							Toast.LENGTH_SHORT).show();
				}
			} else {
				uploadImage();
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
