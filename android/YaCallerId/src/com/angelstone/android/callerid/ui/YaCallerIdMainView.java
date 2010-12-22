package com.angelstone.android.callerid.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.angelstone.android.callerid.R;
import com.angelstone.android.phonetools.utils.PhoneToolsUtil;

public class YaCallerIdMainView extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        startActivityForResult(PhoneToolsUtil.getPhotoPickIntent(200, 300), 0);
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
}