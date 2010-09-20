package com.angelstone.android.smsblocker;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.ProgressBar;

import com.angelstone.android.smsblocker.store.PhoneNumberManager;

public class ClearWaitingProgressBar extends Activity
{
	public static ProgressBar mProgressBar = null;
	public int mClearType;
	private String[] mClearDatas;
	private int mLogType;
	private int mBlockType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		
		setContentView(R.layout.waiting_progress_bar);
		
		Bundle bundle = this.getIntent().getExtras();
		
        mClearType = bundle.getInt("clear_type");
        
        if (mClearType == BlMgrRootView.CLEAR_BLACK_LIST_NUMBER)
        {
        	mClearDatas = bundle.getStringArray("array");
        }
        else if (mClearType == CallRejectLogView.CLEAR_CALL_LOG)
        {
        	mLogType = bundle.getInt("log_type");
        	mBlockType = bundle.getInt("block_type");
        }
		
		mProgressBar = (ProgressBar) findViewById(R.id.waiting_progress_bar_view);
		//mProgressBar.setMax(BlMgrRootView.numlist.size());
		
		
		Thread thread = new Thread()
		{
			public void run()
			{
				Message msg = new Message();
				Bundle msgdata = new Bundle();
				
				if (mClearType == BlMgrRootView.CLEAR_BLACK_LIST_NUMBER)
				{
					for (int i = 0; i < mClearDatas.length; i++)
			        {
						PhoneNumberManager.getIntance(ClearWaitingProgressBar.this).blacklistDeleteNumber(mClearDatas[i]);
						//silenceRejectCall.removePhone((String)numlist.get(pos).get("number"));
						//mProgressBar.setProgress(i);
						
			        }	
				
		        

	        	//ActivityLog.logInfo(_instance,getString(R.string.LogCallBlock),getString(R.string.BlacklistCleared));
	        	
					Message msg_1 = new Message();
					msgdata = new Bundle();
					msgdata.putInt("clearType", BlMgrRootView.CLEAR_BLACK_LIST_NUMBER);
					msg_1.setData(msgdata);
					handler.sendMessage(msg_1);
				}
				else if (mClearType == CallRejectLogView.CLEAR_CALL_LOG)
				{
					PhoneNumberManager.getIntance(ClearWaitingProgressBar.this).deleteLogs(mLogType, mBlockType);
					
					Message msg_1 = new Message();
					msgdata = new Bundle();
					msgdata.putInt("clearType", CallRejectLogView.CLEAR_CALL_LOG);
					msg_1.setData(msgdata);
					handler.sendMessage(msg_1);
				}
			}
		};
		
		thread.start();	
		
	}
	
	private Handler handler = new Handler() 
	{
        @Override
        public void handleMessage(Message msg) 
        {
        	int type = msg.getData().getInt("clearType");

        	switch (type)
        	{
        		case BlMgrRootView.CLEAR_BLACK_LIST_NUMBER:
        		{
        			Intent intent = new Intent();   		       	    
    			    setResult(5, intent);
        			finish();
        			break;
        		}
        		default:
        		{
        			Intent intent = new Intent();   		       	    
    			    setResult(3, intent);		
        			finish();
        			break;
        		}
        	}
        }

	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{
			return false;
		}
		else
		{
			return super.onKeyDown(keyCode, event);
		}
		
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
	 * This method is due to disable the activity destroyed when the screen orientation is changed
	 */
	@Override 
    public void onConfigurationChanged(Configuration newConfig) 
	{  
        try 
        {  
            super.onConfigurationChanged(newConfig);  
            
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) 
            {  
                // do nothing
            } 
            else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) 
            {  
                // do nothing 
            }  
        } 
        catch (Exception ex) 
        {  
        }  

    } 
	
}
