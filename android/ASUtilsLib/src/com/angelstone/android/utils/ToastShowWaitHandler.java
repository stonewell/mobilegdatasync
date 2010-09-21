package com.angelstone.android.utils;

import android.os.Handler;

public class ToastShowWaitHandler
{
	public static final int WAITING_DURATION = 1800;
	
	private boolean mIsShowing = false;
	private int mDuration;
	 private Handler mHandler; 
	
	public ToastShowWaitHandler(int duration)
	{
		mDuration = duration;
		mHandler = new Handler();
	}
	
	public ToastShowWaitHandler()
	{
		mDuration = WAITING_DURATION;
		mHandler = new Handler();
	}
	
	public boolean IsAllowShow()
	{
		if (mIsShowing)
		{
			return false;
		}
		else
		{
			mIsShowing = true;
			mHandler.postDelayed(update, mDuration);
			return true;
		}
	}
	
	private Runnable update = new Runnable() 
	{   
        public void run() 
        {   
        	mIsShowing = false;
        }  
    };   

}
