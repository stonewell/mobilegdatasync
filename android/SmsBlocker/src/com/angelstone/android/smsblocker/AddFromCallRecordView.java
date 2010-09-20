package com.angelstone.android.smsblocker;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.telephony.PhoneNumberUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.angelstone.android.smsblocker.store.PhoneNumberManager;
import com.angelstone.android.utils.PhoneNumberHelpers;
import com.angelstone.android.utils.ToastShowWaitHandler;

public class AddFromCallRecordView extends Activity implements OnItemClickListener,
															   OnClickListener
{
	private Cursor mCursor = null;
	
	private static ToastShowWaitHandler mToastShowWaitHandler = new ToastShowWaitHandler();
	private int[] mCheckState;
	
	private ProgressDialog mDialog;
	
	private int mAllCheckAllowedItemCount = 0;
	private int mCurrentCheckOnItemCount = 0;
	
	private ArrayList<String> mTempNumberList = new ArrayList<String>();
	private ArrayList<String> mTempNameList = new ArrayList<String>();
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.add_from_call_record_view_layout);
			
			mTempNumberList.addAll(AddBlNumberView.mSelectedNumbers);
	        mTempNameList.addAll(AddBlNumberView.mSelectedNames);
			
			mCursor = getContentResolver().query(CallLog.Calls.CONTENT_URI,
												 null, 
												 null, 
												 null, 
					                             CallLog.Calls.DEFAULT_SORT_ORDER);
	        startManagingCursor(mCursor);
	      
	        mCursor.moveToFirst();
	        ListView listView = (ListView)this.findViewById(R.id.call_record_list);
	      
	        CheckBox chkBox = (CheckBox)findViewById(R.id.call_record_check_box);
	        chkBox.setOnClickListener(this);
	        
	        mCheckState = new int[mCursor.getCount()];
	        
	        if (mCursor.getCount() == 0)
	        {
	        	if (mToastShowWaitHandler.IsAllowShow())
	        	{
	        		Toast.makeText(AddFromCallRecordView.this, 
					            R.string.NoRecord, Toast.LENGTH_SHORT).show();
	        	}
	        	listView.setAdapter(null);
	        	chkBox.setEnabled(false);
	        }
	        else
	        {    	
	        	listView.setAdapter((new CallLogAdapter(this, R.layout.call_record_list_row, mCursor)));
	        }
	        
	        listView.setOnItemClickListener(this);
	        
	        Button btn = (Button)findViewById(R.id.call_record_ok_btn);
	        btn.setOnClickListener(this);
	        
	        mDialog = new ProgressDialog(this);
    		mDialog.setMessage(this.getResources().getText(R.string.PleaseWait));
    		mDialog.setIndeterminate(true);
    		mDialog.setCancelable(false);

	        
	        /*
	        btn = (Button)findViewById(R.id.call_record_cancel_btn);
	        btn.setOnClickListener(this);
	        */
	        
		}
		catch (Exception e)
		{
			if (mCursor != null)
			{
				mCursor.close();
			}
			Log.d("scfw", "AddFromCallRecordView:" + e.getClass().toString());
		}
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		mCursor.close();
		Log.d("scfw", this.toString() + ":close cursor");
	}

	public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	{
		// TODO Auto-generated method stub

		//String number = (String)mList.get(position).get("number");
		//String name = (String)mList.get(position).get("name");
		/*
		ListView listView = (ListView)this.findViewById(R.id.call_record_list);
		View view = listView.getAdapter().getView(position, null, null);
		TextView tv = (TextView)view.findViewById(R.id.call_record_phonenumber);
		String number = String.valueOf(tv.getText());
		tv = (TextView)view.findViewById(R.id.call_record_name);
		String name = String.valueOf(tv.getText());
		
		if (number.equals(name))
		{
			//name = AppMain.db.getNameByNumber(number);
			name = PhoneNumberManager.getIntance(this).getNameByNumber(number);
		}
		
		//delete separator of the numbers
		number = number.replace("-", "");

		Intent intent = new Intent();   		       
	    intent.putExtra("call_record_result", number);   
	    intent.putExtra("call_record_return_name", name);
	    setResult(4, intent);     
	    finish(); 
	    */
		
		ListView listView = (ListView)this.findViewById(R.id.call_record_list);
		View view = listView.getChildAt(position - listView.getFirstVisiblePosition());
		ImageView iv = (ImageView)view.findViewById(R.id.call_record_list_item_check_img);
		
		TextView tv = (TextView)view.findViewById(R.id.call_record_phonenumber);
		String number = PhoneNumberHelpers.removeNonNumbericChar(String.valueOf(tv.getText()));
		
		if (mCheckState[position] == PhoneNumberHelpers.CHECK_OFF)
    	{
			iv.setImageResource(R.drawable.btn_check_on);
			mCheckState[position] = PhoneNumberHelpers.CHECK_ON;
			
			mCurrentCheckOnItemCount++;
    	}
    	else if(mCheckState[position] == PhoneNumberHelpers.CHECK_ON)
    	{
    		iv.setImageResource(R.drawable.btn_check_off);
    		mCheckState[position] = PhoneNumberHelpers.CHECK_OFF;
    		
    		mCurrentCheckOnItemCount--;
    		
    		int pos;
    		
    		if ((pos = containsNumber(number)) != -1)
    		{
    			AddBlNumberView.mSelectedNumbers.remove(pos);
    			AddBlNumberView.mSelectedNames.remove(pos);
    		}
    	}
		
		listView.invalidate();
		
		CheckBox chkBox = (CheckBox)findViewById(R.id.call_record_check_box);
		
		if (!chkBox.isEnabled())
		{
			return;
		}
		
		if (mCurrentCheckOnItemCount == mAllCheckAllowedItemCount)
		{
			chkBox.setChecked(true);
		}
		else if (mCurrentCheckOnItemCount >= 0 
				&& mCurrentCheckOnItemCount < mAllCheckAllowedItemCount)
		{
			chkBox.setChecked(false);
		}
	}
	
	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.call_record_ok_btn:
			{		
				
				mCursor.moveToFirst();
				
				for (int i = 0; i < mCheckState.length; i++)
				{
					if (mCheckState[i] == PhoneNumberHelpers.CHECK_ON)
					{
						String number = mCursor.getString(mCursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
						number = PhoneNumberHelpers.removeNonNumbericChar(number);
						String name = mCursor.getString(mCursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME));
						
						if (name == null)
						{
							name = "";
						}
						
						if (containsNumber(number) == -1)
						{
							AddBlNumberView.mSelectedNumbers.add(number);
							AddBlNumberView.mSelectedNames.add(name);
						}
						
					}
					
					mCursor.moveToNext();
				}
				
				
				Intent intent = new Intent();   		       			    
			    setResult(4, intent);  
				finish();
				
				break;
			}
			
			case R.id.call_record_check_box:
			{
				CheckBox chkBox = (CheckBox)findViewById(R.id.call_record_check_box);
				if (chkBox.isChecked())
				{
					Thread thread = new Thread()
		    		{
		    			public void run()
						{
		    				Message msg = new Message();
								Bundle msgdata = new Bundle();
								msgdata.putBoolean("IsRunning", true);
								msg.setData(msgdata);
								handler.sendMessage(msg);
							
								for (int i = 0; i < mCheckState.length; i++)
								{		
									if (mCheckState[i] == -1)
									{
										continue;
									}
									
									ListView listView = (ListView)findViewById(R.id.call_record_list);
									
									View view = listView.getAdapter().getView(i, null, null);
									ImageView iv = (ImageView)view.findViewById(R.id.call_record_list_item_check_img);
									iv.setImageResource(R.drawable.btn_check_on);
									mCheckState[i] = PhoneNumberHelpers.CHECK_ON;
								}
								
								mCurrentCheckOnItemCount = mAllCheckAllowedItemCount;
				        	
							Message msg_1 = new Message();
							msgdata = new Bundle();
							msgdata.putBoolean("isRunning", false);
							msg_1.setData(msgdata);
							handler.sendMessage(msg_1);
						}
		    		};
		    		
		    		thread.start();	
					
					
				}
				else
				{
					Thread thread = new Thread()
		    		{
		    			public void run()
						{
		    				Message msg = new Message();
							Bundle msgdata = new Bundle();
							msgdata.putBoolean("IsRunning", true);
							msg.setData(msgdata);
							handler.sendMessage(msg);
					
							for (int i = 0; i < mCheckState.length; i++)
							{		
								if (mCheckState[i] == -1)
								{
									continue;
								}
								
								ListView listView = (ListView)findViewById(R.id.call_record_list);
								
								View view = listView.getAdapter().getView(i, null, null);
								ImageView iv = (ImageView)view.findViewById(R.id.call_record_list_item_check_img);
								iv.setImageResource(R.drawable.btn_check_off);
								mCheckState[i] = PhoneNumberHelpers.CHECK_OFF;
								
								TextView tv = (TextView)view.findViewById(R.id.call_record_phonenumber);
								String number = PhoneNumberHelpers.removeNonNumbericChar(String.valueOf(tv.getText()));
								
								int pos;
					    		
					    		if ((pos = containsNumber(number)) != -1)
					    		{
					    			AddBlNumberView.mSelectedNumbers.remove(pos);
					    			AddBlNumberView.mSelectedNames.remove(pos);
					    		}
								
								
							}
							
							mCurrentCheckOnItemCount = 0;
							
							Message msg_1 = new Message();
							msgdata = new Bundle();
							msgdata.putBoolean("isRunning", false);
							msg_1.setData(msgdata);
							handler.sendMessage(msg_1);
						}
		    			
					};
					
					thread.start();	
					
					
				}
				
				break;
			}
			
			default:
				break;
		}
		
	}
	
	public class CallLogAdapter extends ResourceCursorAdapter 
	{
		private int mNumberColIndex;
		private int mNameColIndex;
		private int mDateColIndex;
		private int mCallTypeIndex;
		
		
	    public CallLogAdapter(Context context, int layout, Cursor c) 
	    {
	        super(context, layout, c);
	        mNameColIndex = mCursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME);
	        mNumberColIndex = mCursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER);
	        mDateColIndex = mCursor.getColumnIndexOrThrow(CallLog.Calls.DATE);
	        mCallTypeIndex = mCursor.getColumnIndexOrThrow(CallLog.Calls.TYPE);
	        
	        c.moveToFirst();
	        
	        while (!c.isAfterLast())
	        {
	        	String number = PhoneNumberHelpers.removeNonNumbericChar(c.getString(mNumberColIndex));
	        	
	        	if (isInBlacklist(number))
	        	{
	        		mCheckState[c.getPosition()] = PhoneNumberHelpers.CHECK_DISABLE;
	        	}
	        	else if (containsNumber(number) != -1)
	        	{
	        		mCheckState[c.getPosition()] = PhoneNumberHelpers.CHECK_ON;
	        		
	        		mCurrentCheckOnItemCount++;
	        		mAllCheckAllowedItemCount++;
	        	}
	        	else
	        	{
	        		mCheckState[c.getPosition()] = PhoneNumberHelpers.CHECK_OFF;
	        		
	        		mAllCheckAllowedItemCount++;

	        	}
	        	
	        	c.moveToNext();
	        }
	        
	        CheckBox chkBox = (CheckBox)findViewById(R.id.call_record_check_box);
	        
	        if (mAllCheckAllowedItemCount == 0)
	        {
	        	chkBox.setEnabled(false);
	        }
	        else if(mCurrentCheckOnItemCount == mAllCheckAllowedItemCount)
			{
	        	chkBox.setChecked(true);
			}
	        
	        c.moveToFirst();
	    }

	    @Override
	    public void bindView(View view, Context context, Cursor cursor) 
	    {
	        int type = cursor.getInt(mCallTypeIndex);
                  
        	ImageView iv = (ImageView) view.findViewById(R.id.call_record_img);
	    	
	    	if (type == CallLog.Calls.INCOMING_TYPE)
        	{
	    		iv.setImageResource(R.drawable.sym_call_incoming);
        	}
        	else if (type == CallLog.Calls.OUTGOING_TYPE)
        	{
        		iv.setImageResource(R.drawable.sym_call_outgoing);
        	}
        	else if (type == CallLog.Calls.MISSED_TYPE)
        	{
        		iv.setImageResource(R.drawable.sym_call_missed);
        	}
        	
        	
        	String name = cursor.getString(mNameColIndex);      
            TextView tv = (TextView)view.findViewById(R.id.call_record_name);
            tv.setText(name);
            
            String number = PhoneNumberUtils.formatNumber(cursor.getString(mNumberColIndex));
            tv = (TextView)view.findViewById(R.id.call_record_phonenumber);
            tv.setText(number);
            
            long time = cursor.getLong(mDateColIndex);           
            tv = (TextView) view.findViewById(R.id.call_record_date);   
        	tv.setText(DateUtils.formatDateTime(AddFromCallRecordView.this, time, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME
                                                                                                      |DateUtils.FORMAT_ABBREV_ALL));
       
        	iv = (ImageView)view.findViewById(R.id.call_record_list_item_check_img);
        	
        	if (mCheckState[cursor.getPosition()] == PhoneNumberHelpers.CHECK_OFF)
        	{
        		iv.setImageResource(R.drawable.btn_check_off);
        	}
        	else if(mCheckState[cursor.getPosition()] == PhoneNumberHelpers.CHECK_ON)
        	{
        		iv.setImageResource(R.drawable.btn_check_on);
        	}
        	else if (mCheckState[cursor.getPosition()] == PhoneNumberHelpers.CHECK_DISABLE)
        	{
        		iv.setImageResource(R.drawable.btn_check_off_disable);
        	}
            
	    }
		
		private boolean isInBlacklist(String number)
		{
			return PhoneNumberManager.getIntance(AddFromCallRecordView.this).isInBlacklist(number);
		}
	    
	}
	
	private Handler handler = new Handler() 
	{
		
		
        @Override
        public void handleMessage(Message msg) 
        {
        	
        	if (msg.getData().getBoolean("IsRunning"))
        	{
        		mDialog.show();
        	}
        	else
        	{
        		mDialog.dismiss();
        		ListView listView = (ListView)findViewById(R.id.call_record_list);
        		((ResourceCursorAdapter)listView.getAdapter()).notifyDataSetChanged();
        	}
        }

	};
	
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
	
	private int containsNumber(String number)
	{
		for (int i = 0; i < AddBlNumberView.mSelectedNumbers.size(); i++)
		{
			if (PhoneNumberManager.getIntance(this).cmpNumber(number, AddBlNumberView.mSelectedNumbers.get(i)))
			{
				return i;
			}
		}
		
		return -1;
	}
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{
			AddBlNumberView.mSelectedNumbers.clear();
			AddBlNumberView.mSelectedNumbers.addAll(mTempNumberList);
			AddBlNumberView.mSelectedNames.clear();
			AddBlNumberView.mSelectedNames.addAll(mTempNameList);
		}
		
		return super.onKeyDown(keyCode, event);		
	}

}
