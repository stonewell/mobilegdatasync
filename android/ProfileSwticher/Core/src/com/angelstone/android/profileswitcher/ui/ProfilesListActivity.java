package com.angelstone.android.profileswitcher.ui;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ListView;

import com.angelstone.android.profileswitcher.R;
import com.angelstone.android.profileswitcher.store.Profile;

public class ProfilesListActivity extends ContentListBaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.profiles);

		ListView v = (ListView) findViewById(R.id.profile_list);

		Cursor c = managedQuery(Profile.CONTENT_URI, null, null, null, null);

		v.setAdapter(new ProfileAdapter(this, c));
		registerForContextMenu(v);
	}

	@Override
	protected void editContent(long id) {
		Intent intent = new Intent();
		intent.setClass(this, ProfileEditActivity.class);
		intent.putExtra(ProfileSwitcherConstants.EXTRA_ID, id);
		startActivity(intent);
	}

	@Override
	protected void clearAllContent() {
		AlertDialog ad = new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.clear_all_confirm)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								getContentResolver().delete(
										Profile.CONTENT_URI, null, null);
							}
						}).setNegativeButton(android.R.string.cancel, null)
				.create();
		ad.show();
	}

	@Override
	protected void deleteContent(long id) {
		Uri uri = ContentUris.withAppendedId(Profile.CONTENT_URI, id);

		getContentResolver().delete(uri, null, null);
	}
}
