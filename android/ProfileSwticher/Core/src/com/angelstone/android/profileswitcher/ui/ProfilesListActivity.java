package com.angelstone.android.profileswitcher.ui;

import android.content.Intent;
import android.os.Bundle;

import com.angelstone.android.profileswitcher.R;

public class ProfilesListActivity extends ContentListBaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.profiles);
	}

	@Override
	protected void editContent(int id) {
		 Intent intent = new Intent();
		 intent.setClass(this, ProfileEditActivity.class);
		 intent.putExtra("id", id);
		 startActivity(intent);
	}

	@Override
	protected void clearAllContent() {
		// TODO Auto-generated method stub
		
	}
}
