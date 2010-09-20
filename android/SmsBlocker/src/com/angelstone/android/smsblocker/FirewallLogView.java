package com.angelstone.android.smsblocker;



import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TabHost;

public class FirewallLogView extends TabActivity
{
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private final int FC = ViewGroup.LayoutParams.FILL_PARENT;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        final TabHost tabHost = getTabHost();
        
        try
        {
        	
        	LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(100,100);
	        tabHost.addTab(tabHost.newTabSpec("tab1")
	                .setIndicator("Call log")
	                .setContent(new Intent(this, CallRejectLogView.class)));
	        
	        tabHost.addTab(tabHost.newTabSpec("tab2")
	                .setIndicator("Sms log")
	                .setContent(new Intent(this, SmsRejectLogView.class)));
	        
	        DisplayMetrics dm = new DisplayMetrics(); 
	        dm = this.getResources().getDisplayMetrics(); 
	        int screenWidth = dm.widthPixels; 
	        int screenHeight = dm.heightPixels;
	        
	        tabHost.getTabWidget().getChildAt(0)
            .setLayoutParams(
                            new LinearLayout.LayoutParams(screenWidth / 2, 40));
	        
	        tabHost.getTabWidget().getChildAt(1)
            .setLayoutParams(
                            new LinearLayout.LayoutParams(screenWidth / 2, 40));
                            
        }
        catch (Exception e)
        {
        	Log.d("scfw", e.getClass().toString());
        }
        
        
        
       
    }
}
