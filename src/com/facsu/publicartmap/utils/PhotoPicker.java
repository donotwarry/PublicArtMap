package com.facsu.publicartmap.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.dennytech.common.util.Log;

public class PhotoPicker {
	private static final String LOG_TAG = PhotoPicker.class.getSimpleName();
	//
	// The launch code when picking a photo and the raw data is returned
	//
	public static final int REQUEST_CODE_PHOTO_PICKED = 3021;
	//
	// The launch code when taking a picture
	//
	public static final int REQUEST_CODE_CAMERA = 3022;

	private Context mContext = null;
	private String strImgPath;
	private Fragment fragment = null;

	public PhotoPicker(Context context) {
		this.mContext = context;
	}

	public PhotoPicker(Fragment frag) {
		this.fragment = frag;
	}

	/** call before the super.onSaveInstanceState */
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("strImgPath", strImgPath);
	}

	/** call after the super.onRestoreInstanceState */
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		strImgPath = savedInstanceState.getString("strImgPath");
	}

	public void doTakePhoto() {
		try {
			if (mContext != null) {
				((Activity) mContext).startActivityForResult(
						getTakePhotoIntent(), REQUEST_CODE_CAMERA);
			} else if (fragment != null) {
				fragment.startActivityForResult(getTakePhotoIntent(),
						REQUEST_CODE_CAMERA);
			}

		} catch (ActivityNotFoundException e) {
			if (mContext != null) {
				Toast.makeText(mContext, "手机上没有照片。", Toast.LENGTH_LONG).show();
			} else if (fragment != null && fragment.getActivity() != null) {
				Toast.makeText(fragment.getActivity(), "手机上没有照片。",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	public void doPickPhotoFromGallery() {
		try {
			Intent intent = getPhotoPickIntent();
			if (mContext != null) {
				((Activity) mContext).startActivityForResult(
						Intent.createChooser(intent, "Select Picture"),
						REQUEST_CODE_PHOTO_PICKED);
			} else if (fragment != null) {
				fragment.startActivityForResult(
						Intent.createChooser(intent, "Select Picture"),
						REQUEST_CODE_PHOTO_PICKED);
			}

		} catch (Exception e) {
			e.printStackTrace();
			if (mContext != null) {
				Toast.makeText(mContext, "手机上没有照片。", Toast.LENGTH_LONG).show();
			} else if (fragment != null && fragment.getActivity() != null) {
				Toast.makeText(fragment.getActivity(), "手机上没有照片。",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	// should called after doTakePhoto()
	public void doCropPhoto() {
		if (strImgPath == null) {
			Log.e(LOG_TAG, "the file path of the croped photo is null");
			return;
		}

		try {
			File tempFile = new File(strImgPath);
			Uri uri = Uri.fromFile(tempFile);
			final Intent intent = getCropImageIntent(uri);
			if (mContext != null) {
				((Activity) mContext).startActivityForResult(intent,
						REQUEST_CODE_PHOTO_PICKED);
			} else if (fragment != null) {
				fragment.startActivityForResult(intent,
						REQUEST_CODE_PHOTO_PICKED);
			}

		} catch (Exception e) {

			if (mContext != null) {
				Toast.makeText(mContext, "手机上没有照片。", Toast.LENGTH_LONG).show();
			} else if (fragment != null && fragment.getActivity() != null) {
				Toast.makeText(fragment.getActivity(), "手机上没有照片。",
						Toast.LENGTH_LONG).show();
			}

		}
	}

	public String strImgPath() {
		return strImgPath;
	}

	public String parseImgPath(Intent data) {
		if (mContext == null) {
			return null;
		}
		String path = null;
		if (data != null) {
			Uri uri = data.getData();
			if (uri != null) {
				Cursor cursor = null;
				String[] proj = { MediaStore.Images.Media.DATA };
				if (mContext != null) {
					cursor = mContext.getContentResolver().query(uri, proj,
							null, null, null);
				} else if (fragment != null && fragment.getActivity() != null) {
					cursor = fragment.getActivity().getContentResolver()
							.query(uri, proj, null, null, null);
				}

				if (cursor == null)
					return null;

				try {
					cursor.moveToFirst();
					path = cursor.getString(cursor
							.getColumnIndex(MediaStore.Images.Media.DATA));
				} catch (Exception e) {
					Toast.makeText(mContext, "请换一个文件夹试试！", Toast.LENGTH_SHORT)
							.show();
					e.printStackTrace();
				} finally {
					if (cursor != null) {
						cursor.close();
					}
				}
			}
		}
		return path;
	}

	protected File getOutFile() {
		String imgPath = Environment.getExternalStorageDirectory().toString()
				+ "/DCIM/Camera/";
		String fileName = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(new Date()) + ".jpg";
		File out = new File(imgPath);
		if (!out.exists()) {
			out.mkdirs();
		}
		out = new File(imgPath, fileName);
		strImgPath = imgPath + fileName;
		return out;
	}

	protected Intent getTakePhotoIntent() {
		Uri uri = Uri.fromFile(getOutFile());
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		return intent;
	}

	protected Intent getPhotoPickIntent() {
		Intent intent = new Intent();
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		if (Build.VERSION.SDK_INT < 19) {
			intent.setAction(Intent.ACTION_GET_CONTENT);
		} else {
			intent.setAction("android.intent.action.OPEN_DOCUMENT");
		}
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", outputX());
		intent.putExtra("outputY", outputY());
		intent.putExtra("return-data", true);
		return intent;
	}

	protected Intent getCropImageIntent(Uri photoUri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(photoUri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", outputX());
		intent.putExtra("outputY", outputY());
		intent.putExtra("return-data", true);
		return intent;
	}

	protected int outputX() {
		return 144;
	}

	protected int outputY() {
		return 144;
	}

}
