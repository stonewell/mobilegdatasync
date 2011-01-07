package com.angelstone.android.profileswitcher.utils;

import java.util.concurrent.ConcurrentHashMap;

import com.angelstone.android.profileswitcher.store.Profile;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;

public class ProfileCache extends ContentObserver {
	private static final class CacheItem {
		public int Pos;
		public long Id;
		public String Label;
	};

	private Cursor mCursor = null;
	private Context mContext = null;
	private ConcurrentHashMap<Long, CacheItem> mCache = new ConcurrentHashMap<Long, CacheItem>();

	public ProfileCache(Context context, Cursor cursor) {
		super(new Handler());

		mCursor = cursor;
		mContext = context;

		mContext.getContentResolver().registerContentObserver(
				Profile.CONTENT_URI, true, this);
	}

	public void clear() {
		mCache.clear();
		mContext.getContentResolver().unregisterContentObserver(this);
	}

	public int getPosition(long id) {
		CacheItem item = getCacheItem(id);

		return item == null ? -1 : item.Pos;
	}

	private CacheItem getCacheItem(long id) {
		CacheItem item = mCache.get(id);

		if (item == null) {
			fillCache();

			item = mCache.get(id);
		}
		return item;
	}

	public String getLabel(long id) {
		CacheItem item = getCacheItem(id);

		return item == null ? null : item.Label;
	}

	private void fillCache() {
		mCursor.requery();
		mCache.clear();
		
		if (mCursor.moveToFirst()) {
			int idxId = mCursor.getColumnIndex(Profile.COLUMN_ID);
			int idxLabel = mCursor.getColumnIndex(Profile.COLUMN_NAME);

			do {
				CacheItem item = new CacheItem();
				item.Id = mCursor.getLong(idxId);
				item.Pos = mCursor.getPosition();
				item.Label = mCursor.getString(idxLabel);
				
				mCache.put(item.Id, item);
			}
			while(mCursor.moveToNext());
		}
	}

	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		mCache.clear();
	}
}
