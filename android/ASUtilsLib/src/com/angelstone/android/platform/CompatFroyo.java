package com.angelstone.android.platform;

import android.content.Context;
import android.net.ConnectivityManager;

public class CompatFroyo extends CompatEclair {

	public CompatFroyo(Context ctx) {
		super(ctx);
	}

	@Override
	public boolean enableMobileNetwork(boolean enable) {
        ConnectivityManager cm =
                (ConnectivityManager)mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
        	cm.getClass().getMethod("setMobileDataEnabled",boolean.class).invoke(cm, enable);
        }catch(Throwable t) {
        	
        }
        
        return true;
	}
}
