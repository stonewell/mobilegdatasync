package com.angelstone.android.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.text.TextUtils;

import com.angelstone.android.R;

public class LocationUtils {

	public static String locationToString(Location location) {
		if (location == null)
			return "";
		StringBuilder sb = new StringBuilder(1024);
		sb.append(String.valueOf(location.getLongitude())).append(",");
		sb.append(String.valueOf(location.getLatitude()));

		if (location.hasAltitude())
			sb.append(",").append(String.valueOf(location.getAltitude()));
		return sb.toString();
	}

	public static Location locationFromString(String loc) {

		if (!TextUtils.isEmpty(loc)) {
			String[] parts = loc.split(",");

			if (parts.length >= 2) {
				double longitude = Location.convert(parts[0]);
				double latitude = Location.convert(parts[1]);

				Location location = new Location(
						LocationManager.NETWORK_PROVIDER);
				location.setLatitude(latitude);
				location.setLongitude(longitude);

				if (parts.length > 2) {
					location.setAltitude(Location.convert(parts[2]));
				} else {
					location.removeAltitude();
				}

				return location;
			}
		}

		return null;
	}

	public static String locationDefaultDisplay(Context context,
			Location location) {
		StringBuilder sb = new StringBuilder();
		sb.append(location.getLongitude() > 0 ? context
				.getString(R.string.east_longitude) : context
				.getString(R.string.west_longitude));
		sb.append(Location.convert(location.getLongitude(),
				Location.FORMAT_DEGREES));
		sb.append(",");
		sb.append(location.getLatitude() > 0 ? context
				.getString(R.string.north_latitude) : context
				.getString(com.angelstone.android.R.string.south_latitude));
		sb.append(Location.convert(location.getLatitude(),
				Location.FORMAT_DEGREES));

		if (location.hasAltitude()) {
			sb.append(",");
			sb.append(Location.convert(location.getAltitude(),
					Location.FORMAT_DEGREES));
		}

		return sb.toString();
	}
}
