package com.angelstone.android.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.widget.TextView;

public class GeoCodeLoader implements Callback {

	private static final String LOADER_THREAD_NAME = "GeoCodeLoader";
	private static final int MESSAGE_REQUEST_LOADING = 1;
	private static final int MESSAGE_GEO_CODE_LOADED = 2;
	private static final int RETRY_COUNT = 5;

	private static class GeoCodeHolder {
		private static final int NEEDED = 0;
		private static final int LOADING = 1;
		private static final int LOADED = 2;

		int state;
		String GeoCodeString;
		int retry_count = 0;
	}

	private final ConcurrentHashMap<Location, GeoCodeHolder> mCache = new ConcurrentHashMap<Location, GeoCodeHolder>();

	private final ConcurrentHashMap<TextView, Location> mPendingRequests = new ConcurrentHashMap<TextView, Location>();

	private final Handler mMainThreadHandler = new Handler(this);

	private LoaderThread mLoaderThread;

	private boolean mLoadingRequested;

	private boolean mPaused;

	private final Context mContext;

	public GeoCodeLoader(Context context) {
		mContext = context;
	}

	public void loadGeoCode(TextView view, Location loc) {
		if (loc == null) {
			// No photo is needed
			view.setText("");
			mPendingRequests.remove(view);
		} else {
			boolean loaded = loadCached(view, loc);
			if (loaded) {
				mPendingRequests.remove(view);
			} else {
				mPendingRequests.put(view, loc);
				if (!mPaused) {
					// Send a request to start loading photos
					requestLoading();
				}
			}
		}
	}

	private boolean loadCached(TextView view, Location loc) {
		GeoCodeHolder holder = mCache.get(loc);
		if (holder == null) {
			holder = new GeoCodeHolder();
			mCache.put(loc, holder);
		} else if (holder.state == GeoCodeHolder.LOADED) {
			if (holder.GeoCodeString != null) {
				view.setText(holder.GeoCodeString);
			}
			return true;
		}

		view.setText(LocationUtils.locationDefaultDisplay(mContext, loc));
		holder.state = GeoCodeHolder.NEEDED;
		return false;
	}

	public void stop() {
		pause();

		if (mLoaderThread != null) {
			try {
				HandlerThreadQuiter.quit(mLoaderThread);
			} catch (VerifyError ex) {
				mLoaderThread.getLooper().quit();
			}
			mLoaderThread = null;
		}

		mPendingRequests.clear();
		mCache.clear();
	}

	public void clear() {
		mPendingRequests.clear();
		mCache.clear();
	}

	public void pause() {
		mPaused = true;
	}

	public void resume() {
		mPaused = false;
		if (!mPendingRequests.isEmpty()) {
			requestLoading();
		}
	}

	private void requestLoading() {
		if (!mLoadingRequested) {
			mLoadingRequested = true;
			mMainThreadHandler.sendEmptyMessage(MESSAGE_REQUEST_LOADING);
		}
	}

	/**
	 * Processes requests on the main thread.
	 */
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MESSAGE_REQUEST_LOADING: {
			mLoadingRequested = false;
			if (!mPaused) {
				if (mLoaderThread == null) {
					mLoaderThread = new LoaderThread();
					mLoaderThread.start();
				}

				mLoaderThread.requestLoading();
			}
			return true;
		}

		case MESSAGE_GEO_CODE_LOADED: {
			if (!mPaused) {
				processLoadedGeoCode();
			}
			return true;
		}
		}
		return false;
	}

	private void processLoadedGeoCode() {
		Iterator<TextView> iterator = mPendingRequests.keySet().iterator();
		while (iterator.hasNext()) {
			TextView view = iterator.next();
			Location loc = mPendingRequests.get(view);
			boolean loaded = loadCached(view, loc);
			if (loaded) {
				iterator.remove();
			}
		}

		if (!mPendingRequests.isEmpty()) {
			requestLoading();
		}
	}

	private void cacheGeoCode(Location loc, String code) {
		if (mPaused) {
			return;
		}

		GeoCodeHolder holder = mCache.get(loc);

		if (holder == null) {
			holder = new GeoCodeHolder();
			holder.state = GeoCodeHolder.NEEDED;
			mCache.put(loc, holder);
		}

		holder.GeoCodeString = code;

		if (holder.retry_count > RETRY_COUNT || code != null) {
			holder.state = GeoCodeHolder.LOADED;
		} else {
			holder.retry_count++;
		}
	}

	private void obtainLocationsToLoad(ArrayList<Location> locations) {
		locations.clear();

		Iterator<Location> iterator = mPendingRequests.values().iterator();
		while (iterator.hasNext()) {
			Location id = iterator.next();
			GeoCodeHolder holder = mCache.get(id);
			if (holder != null && holder.state == GeoCodeHolder.NEEDED) {
				// Assuming atomic behavior
				holder.state = GeoCodeHolder.LOADING;
				locations.add(id);
			}
		}
	}

	private class LoaderThread extends HandlerThread implements Callback {
		private final ArrayList<Location> mLocations = new ArrayList<Location>();
		private Handler mLoaderThreadHandler;
		private Geocoder mGeoCode = new Geocoder(mContext);

		public LoaderThread() {
			super(LOADER_THREAD_NAME);
		}

		public void requestLoading() {
			if (mLoaderThreadHandler == null) {
				mLoaderThreadHandler = new Handler(getLooper(), this);
			}
			mLoaderThreadHandler.sendEmptyMessage(0);
		}

		public boolean handleMessage(Message msg) {
			loadPhotosFromDatabase();
			mMainThreadHandler.sendEmptyMessage(MESSAGE_GEO_CODE_LOADED);
			return true;
		}

		private void loadPhotosFromDatabase() {
			obtainLocationsToLoad(mLocations);

			int count = mLocations.size();
			if (count == 0) {
				return;
			}

			Iterator<Location> it = mLocations.iterator();

			while (it.hasNext()) {
				Location loc = it.next();

				List<Address> results = null;

				try {
					results = mGeoCode.getFromLocation(loc.getLatitude(),
							loc.getLongitude(), 1);
				} catch (Throwable t) {

				}

				if (results != null && results.size() > 0) {
					Address address = results.get(0);
					StringBuilder sb = new StringBuilder();

					for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
						sb.append(address.getAddressLine(i)).append("");
					}

					cacheGeoCode(loc, sb.toString());

					it.remove();
				}
			}

			count = mLocations.size();
			for (int i = 0; i < count; i++) {
				cacheGeoCode(mLocations.get(i), null);
			}
		}
	}

}
