package com.angelstone.android.utils;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;

public class PhoneToolsUtil {
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	
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

	public static boolean isBetterLocation(Location newLocation,
			Location oldLocation) {
		if (oldLocation == null) {
			// A new newLocation is always better than no newLocation
			return true;
		}

		// Check whether the new newLocation fix is newer or older
		long timeDelta = newLocation.getTime() - oldLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current newLocation, use
		// the new newLocation
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new newLocation is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new newLocation fix is more or less accurate
		int accuracyDelta = (int) (newLocation.getAccuracy() - oldLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new newLocation are from the same provider
		boolean isFromSameProvider = isSameProvider(newLocation.getProvider(),
				oldLocation.getProvider());

		// Determine newLocation quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private static boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
}
