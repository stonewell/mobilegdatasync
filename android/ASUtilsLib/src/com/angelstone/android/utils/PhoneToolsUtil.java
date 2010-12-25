package com.angelstone.android.utils;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

public class PhoneToolsUtil {
	public static Intent getPhotoPickIntent(Uri uri, int dstWidth, int dstHeight) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		intent.setType("image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", dstWidth);
		intent.putExtra("aspectY", dstHeight);
		intent.putExtra("outputX", dstWidth);
		intent.putExtra("outputY", dstHeight);
		intent.putExtra("return-data", false);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		intent.putExtra("return-data", false);
		intent.putExtra("noFaceDetection", true);
		return intent;
	}
}
