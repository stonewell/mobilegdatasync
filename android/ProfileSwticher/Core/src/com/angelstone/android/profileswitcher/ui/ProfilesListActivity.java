package com.angelstone.android.profileswitcher.ui;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.angelstone.android.profileswitcher.R;

public class ProfilesListActivity extends ContentListBaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.profiles);
		
		ArrayList<String> results = new ArrayList<String>();
		for(int i=0;i<100;i++)
			results.add(String.valueOf(i));
		
		ListView v = (ListView)findViewById(R.id.profile_list);
		v.setAdapter(new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1, results.toArray(new String[0])));
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
