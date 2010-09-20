package com.angelstone.android.smsblocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.angelstone.android.smsblocker.store.EventLog;
import com.angelstone.android.smsblocker.store.PhoneNumberManager;
import com.angelstone.android.utils.PhoneNumberHelpers;


public class CallRejectLogView extends Activity implements OnItemLongClickListener,                                                        
                                                           OnClickListener
{
	//private Menu mMenu;
	//public static final int CALL_BL_INTC_VIEW = 0;
	//public static final int CALL_ADV_INTC_VIEW = 1;
	//public static final int SMS_BL_INTC_VIEW = 1;
	//public static final int SMS_FILTER_INTC_VIEW = 3;
	
	//private int whichView;
	
	private int mPosition = 0;
	private ListView lv = null;
    
    private static CallRejectLogView _instance = null;
  
    private Cursor mLogCursor;
    
    public  static final int CLEAR_CALL_LOG = 1;
    
    private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private final int FC = ViewGroup.LayoutParams.FILL_PARENT;

	public void onCreate(Bundle savedInstanceState) 
    {        
        try
		{			
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.reject_log_view_layout);
	        _instance = this;
	        
	        mLogCursor = PhoneNumberManager.getIntance(this).getCallRejectLogCursor(EventLog.CALL_LOG_BLOCK_TYPE_BL);
	        
	        /*
	        TextView tv = (TextView)findViewById(R.id.reject_log_view_name);
	        tv.setText(mViewNames[mViewIndex]);
	         */       
	        lv = (ListView)this.findViewById(R.id.reject_log_list);
	        
	        registerForContextMenu(lv);
		    lv.setOnItemLongClickListener(this);
		    /*
		    Button btn = (Button)findViewById(R.id.reject_log_view_prev_btn);
		    btn.setOnClickListener(this);
		    btn.setVisibility(Button.INVISIBLE);
		    
		    btn = (Button)findViewById(R.id.reject_log_view_next_btn);
		    btn.setOnClickListener(this);
	        */
		    refreshList();
		      
		    /*
		    mDialog = new ProgressDialog(CallRejectLogView.this);
	        
    		mDialog.setMessage(this.getResources().getText(R.string.PleaseWait));
    		mDialog.setIndeterminate(true);
    		mDialog.setCancelable(false);
    		*/
		    
		}
		catch (Exception e)
		{
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
    }
	
	public static CallRejectLogView getIntance()
	{
		return _instance;
	}
	
	protected void onDestroy()
	{
		if (mLogCursor != null)
		{
			mLogCursor.close();
		}
		super.onDestroy();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) 
	{
		mPosition = position;
		return false;
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{  	
		View view = lv.getAdapter().getView(mPosition, null, null);
		TextView tv = (TextView)view.findViewById(R.id.call_reject_record_item_text_2);
		//Map<String, String> map = (Map<String, String>)lv.getItemAtPosition(mPosition);
		//String number = map.get("number");
		String number = PhoneNumberHelpers.removeNonNumbericChar(String.valueOf(tv.getText()));
		
		if (!PhoneNumberManager.getIntance(this).isBlacklisted(number))
		{
			MenuItem addItem = menu.add(0, 1, 1,  R.string.AddToBlacklist);
		}
		
		menu.add(0, 2, 2, R.string.CallBack);
		menu.add(0, 3, 3, R.string.EditNumberAndCallBack);
		menu.add(0, 4, 4, R.string.SendTextMessage);
		menu.add(0, 5, 5, R.string.Delete);
		menu.add(0, 6, 6, R.string.Return);
		menu.setHeaderTitle(R.string.Menu);
		super.onCreateContextMenu(menu, v, menuInfo);		
	}
	
	public boolean onContextItemSelected(MenuItem item) 
	{  
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();  
		switch (item.getItemId()) 
		{  
			case 1:    
			{
				//Map<String, String> map = (Map<String, String>)lv.getItemAtPosition(mPosition);
				//String number = map.get("number");
				View view = lv.getAdapter().getView(mPosition, null, null);
				TextView tv = (TextView)view.findViewById(R.id.call_reject_record_item_text_2);
				String number = String.valueOf(tv.getText());
				
				tv = (TextView)view.findViewById(R.id.call_reject_record_item_text_1);
				String tag_or_name = String.valueOf(tv.getText());
				
			
				Intent intent = new Intent();
				intent.setClass(this, EditBlNumberView.class);				
				
				Bundle bundle = new Bundle();
				
				//The key "POSITION" is useless here, set to 0 is OK, if not set ,the error will occur
				bundle.putInt("POSITION", 0);
				bundle.putString("EDIT_NUMBER", number);
				bundle.putString("EDIT_NAME", tag_or_name);
				bundle.putBoolean("block_call", true);
				bundle.putBoolean("block_sms", true);
				//bundle.putString("reply_sms", "");
				intent.putExtras(bundle);
				startActivityForResult(intent, 2);
				
				
				break;
			}
			case 2:
			{
				View view = lv.getAdapter().getView(mPosition, null, null);
				TextView tv = (TextView)view.findViewById(R.id.call_reject_record_item_text_2);
				String number = String.valueOf(tv.getText());
				
				Uri uri = Uri.parse("tel:" + number);
				Intent it = new Intent(Intent.ACTION_CALL, uri);
				startActivity(it);

				break;
			}
			case 3:
			{
				View view = lv.getAdapter().getView(mPosition, null, null);
				TextView tv = (TextView)view.findViewById(R.id.call_reject_record_item_text_2);
				String number = String.valueOf(tv.getText());
				
				Uri uri = Uri.parse("tel:" + number);
				Intent it = new Intent(Intent.ACTION_DIAL, uri);
				startActivity(it);
				
				break;
			}
			case 4:
			{
				View view = lv.getAdapter().getView(mPosition, null, null);
				TextView tv = (TextView)view.findViewById(R.id.call_reject_record_item_text_2);
				String number = String.valueOf(tv.getText());
				
				Uri smsToUri = Uri.parse("smsto:" + number); 
				Intent sendIntent = new Intent(Intent.ACTION_VIEW, smsToUri); 
				//sendIntent.putExtra("sms_body", "Hello dear world");
				sendIntent.putExtra("address", number); 
				sendIntent.setType("vnd.android-dir/mms-sms"); 
				startActivity(sendIntent); 
				
				break;
			}
			case 5:
			{
				AlertDialog ad = new AlertDialog.Builder(this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.alert_dialog_two_buttons_title)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {                 
                    	PhoneNumberManager.getIntance(CallRejectLogView.this).deleteLog(mLogCursor, mPosition);
                    	
                    	if (mLogCursor != null)
                    	{
                    		mLogCursor.close();
                    	}
                    	
                    	mLogCursor = PhoneNumberManager.getIntance(CallRejectLogView.this).getCallRejectLogCursor(EventLog.CALL_LOG_BLOCK_TYPE_BL);
	                    refreshList();
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* User clicked Cancel so do some stuff */
                    }
                })
                .create();				
				ad.show();
    				
				break;
			}
			case 6:
			default:
				break;	
		}
		return super.onContextItemSelected(item);  
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) 
	{
		
		menu.clear();
		menu.add(0, 0, 0, R.string.ClearAllRecords).setIcon(this.getResources().getDrawable(R.drawable.ic_menu_delete));
		
		if (mLogCursor != null)
		{
			if (mLogCursor.getCount() == 0)
			{
				menu.getItem(0).setEnabled(false);
			}
		}
		else
		{
			menu.getItem(0).setEnabled(false);
		}
				
		//mMenu = menu;
 
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{    
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) 
	{    
		switch (item.getItemId()) 
		{    
			case 0:        
			{        
		        AlertDialog ad = new AlertDialog.Builder(this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.alert_dialog_two_buttons_title_3)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {                
                    	/*
                    	Thread thread = new Thread()
                		{
                			public void run()
							{
                				Message msg = new Message();
								Bundle msgdata = new Bundle();
								msgdata.putBoolean("isRunning", true);
								msg.setData(msgdata);
								handler.sendMessage(msg);
							
								int type = mViewIndex % 2;
								
								for (int i = 0; i < 100000000; i++)
								{
									int a = 0;
								}
		                    	
								PhoneNumberManager.getIntance(CallRejectLogView.this).deleteLogs(mLogTypes[mViewIndex], mBlockTypes[mViewIndex]);
        				        
        				        Message msg_1 = new Message();
								msgdata = new Bundle();
								msgdata.putBoolean("isRunning", false);
								msg_1.setData(msgdata);
								handler.sendMessage(msg_1);
							}
                		};
                		
                		thread.start();
                    	*/

        				Intent intent = new Intent();
        				intent.setClass(CallRejectLogView.this, ClearWaitingProgressBar.class);
        				intent.putExtra("log_type", EventLog.LOG_TYPE_CALL);
        				intent.putExtra("block_type", EventLog.CALL_LOG_BLOCK_TYPE_BL);
        				intent.putExtra("clear_type", CLEAR_CALL_LOG);
        				startActivityForResult(intent, 2);	
                    	       	
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked Cancel so do some stuff */
                    }
                })
                .create();
				
				ad.show();
	
			    return true;
			}
			default:
				return true;
		}
	}
	
	private Handler handler = new Handler() 
	{
        @Override
        public void handleMessage(Message msg) 
        {
        	/*
        	if (msg.getData().getBoolean("isRunning"))
        	{
        		mDialog.show();
        	}
        	else
        	{
        		mDialog.dismiss();
        		mMenu.getItem(0).setEnabled(false);
        	    
        		if (mLogCursor != null)
        		{
        			mLogCursor.close();
        		}
        		
        		mLogCursor = PhoneNumberManager.getIntance(CallRejectLogView.this).getCallRejectLogCursor(mBlockTypes[mViewIndex]);
    			refreshList();
        		
        	}
        	*/
        }

	};
	
	public void refreshList()
	{
		if (mLogCursor != null)
		{
			if (mLogCursor.getCount() != 0)
			{
				lv.setAdapter((new CallRejectLogAdapter(this, R.layout.call_reject_record_item, mLogCursor)));
				
				LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(0, 0);
				TextView tv = (TextView)findViewById(R.id.empty_record_text);
				tv.setLayoutParams(param);
				
			}
			else
			{
				lv.setAdapter(null);
				
				LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(FC, WC);
				param.weight = 1;
				TextView tv = (TextView)findViewById(R.id.empty_record_text);
				tv.setLayoutParams(param);
			}
		}
		else
		{
			Log.d("scfw", this.toString() + ":" + "Cursor is null");
			lv.setAdapter(null);
			
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(FC, FC);
			TextView tv = (TextView)findViewById(R.id.empty_record_text);
			tv.setLayoutParams(param);
		}
	}
	
	public void refreshList_2()
	{
		try 
		{
			if (mLogCursor != null)
			{
				mLogCursor.close();
			}
			
			mLogCursor = PhoneNumberManager.getIntance(this).getCallRejectLogCursor(EventLog.CALL_LOG_BLOCK_TYPE_BL);
			refreshList();

		}
		catch (Exception e)
		{
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
	}
	
	@Override  
	protected void onActivityResult(int requestCode, int resultCode, Intent data)   
	{   
		try
		{
			switch (resultCode)
			{
				case 2:		//Add to black list result
				{	
					String edit_num = data.getExtras().getString("edited_number_return");
					boolean blockCall = data.getExtras().getBoolean("call_block");
					boolean blockSms = data.getExtras().getBoolean("sms_block");
					String name = data.getExtras().getString("edited_name_return");
					//String replySms = data.getExtras().getString("edited_reply_sms_return");

					edit_num = PhoneNumberHelpers.removeNonNumbericChar(edit_num);
					
	       	        PhoneNumberManager.getIntance(this).blacklistAddNumber(edit_num, blockCall, blockSms, name);
					
					//refreshList_2();
	       	        
					Toast.makeText(this, 
				            R.string.AddedToBlacklist, Toast.LENGTH_SHORT).show();
					
					
	       	        if (BlMgrRootView.getInstance() != null)
					{			
						BlMgrRootView.getInstance().refreshViewList_3(edit_num, blockCall, blockSms, name);
					}
					
	       	        PhoneNumberManager.getIntance(this).removeFromContact(edit_num);
	       	        
					break;
				}
				case 3:
				{
					//mMenu.getItem(0).setEnabled(false);
	        	    
	        		if (mLogCursor != null)
	        		{
	        			mLogCursor.close();
	        		}
	        		
	        		mLogCursor = PhoneNumberManager.getIntance(CallRejectLogView.this).getCallRejectLogCursor(EventLog.CALL_LOG_BLOCK_TYPE_BL);
	    			refreshList();
				}
				default:
					break;
			}
		}
		catch (Exception e)
		{
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
		
	}

	@Override
	public void onClick(View v)
	{
		/*
		switch (v.getId())
		{
			case R.id.reject_log_view_prev_btn:
			{	
				mViewIndex--;
				
				Button btn_n = (Button)findViewById(R.id.reject_log_view_next_btn);
			    //btn_n.setEnabled(true);
				btn_n.setVisibility(Button.VISIBLE);
				
				if (mViewIndex == 0)
				{
					Button btn_p = (Button)findViewById(R.id.reject_log_view_prev_btn);
				    //btn_p.setEnabled(false);
					
					btn_p.setVisibility(Button.INVISIBLE);
				}
				
				break;
			}
			case R.id.reject_log_view_next_btn:
			{
				
				mViewIndex++;
				
				Button btn_p = (Button)findViewById(R.id.reject_log_view_prev_btn);
			    //btn_p.setEnabled(true);
				btn_p.setVisibility(Button.VISIBLE);
				
				if (mViewIndex == 1)
				{
					Button btn_n = (Button)findViewById(R.id.reject_log_view_next_btn);
				    //btn_n.setEnabled(false);
					btn_n.setVisibility(Button.INVISIBLE);
				}
				break;
			}
			default:
				break;
				
		}
		
		//mViewIndex = mViewIndex % 4;
		
		int type = mViewIndex % 2;
		
		TextView tv = (TextView)findViewById(R.id.reject_log_view_name);
        tv.setText(mViewNames[mViewIndex]);
        
        if (mLogCursor != null)
        {
        	mLogCursor.close();
        }
        
        mLogCursor = PhoneNumberManager.getIntance(this).getCallRejectLogCursor(mBlockTypes[mViewIndex]);
        
        refreshList();
		*/
	} 
	
	public class CallRejectLogAdapter extends ResourceCursorAdapter 
	{

	    private int mTimeColumnId;
	    private int mNumberColumnId;
	    private int mNameColumnId;
	    private int mSmsBodyColumnId;

	    public CallRejectLogAdapter(Context context, int layout, Cursor c) {
	        super(context, layout, c);
	        mTimeColumnId = c.getColumnIndexOrThrow("time");
	        mNumberColumnId = c.getColumnIndexOrThrow("number");
	        mNameColumnId = c.getColumnIndexOrThrow("contact_name");
	        mSmsBodyColumnId = c.getColumnIndexOrThrow("sms_text");
	    }

	    @Override
	    public void bindView(View view, Context context, Cursor cursor) 
	    {        
	        
        	ImageView iv = (ImageView) view.findViewById(R.id.call_reject_record_item_img);
 
        	iv.setImageResource(R.drawable.rejectcall);
        	
        	String name = cursor.getString(mNameColumnId);      
            TextView tv = (TextView)view.findViewById(R.id.call_reject_record_item_text_1);
            tv.setText(name);
            
            String number = cursor.getString(mNumberColumnId);
            tv = (TextView)view.findViewById(R.id.call_reject_record_item_text_2);
            tv.setText(number);
            
            long time = cursor.getLong(mTimeColumnId);
            
            //java.text.DateFormat formatter = android.text.format.DateFormat.getDateFormat(CallRejectLogView.this);
        	//String dateStr = formatter.format(time);
        	
        	//formatter = android.text.format.DateFormat.getTimeFormat(CallRejectLogView.this);
        	//String timeStr = formatter.format(time);
            
            tv = (TextView) view.findViewById(R.id.call_reject_record_item_text_3); 
            //String timeStr = DateUtils.formatDateTime(CallRejectLogView.this, time, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME
            		                                                // |DateUtils.FORMAT_ABBREV_ALL);   
        	tv.setText(DateUtils.formatDateTime(CallRejectLogView.this, time, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME
                                                                                                        |DateUtils.FORMAT_ABBREV_ALL));
	    }   
	       
	    
	}
	
	
}
