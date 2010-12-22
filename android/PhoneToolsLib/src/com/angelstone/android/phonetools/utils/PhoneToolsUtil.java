package com.angelstone.android.phonetools.utils;

import android.content.Intent;

public class PhoneToolsUtil {
	public static Intent getPhotoPickIntent(int dstWidth, int dstHeight) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		intent.setType("image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", dstWidth);
		intent.putExtra("outputY", dstHeight);
		intent.putExtra("return-data", true);
		return intent;
	}
}
