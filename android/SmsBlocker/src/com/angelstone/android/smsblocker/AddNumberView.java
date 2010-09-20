package com.angelstone.android.smsblocker;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.angelstone.android.smsblocker.store.PhoneNumberManager;
import com.angelstone.android.utils.ToastShowWaitHandler;

public class AddNumberView extends Activity implements OnClickListener
{
	private boolean newnum;
	private int position;
	
	private EditText et1;
	private EditText et2;
	
	private String mOldTag = null;
	
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private final int FC = ViewGroup.LayoutParams.FILL_PARENT;
	
	private ToastShowWaitHandler toastShowWaitHandler = new ToastShowWaitHandler();
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.add_number_view);
			
			DisplayMetrics dm = new DisplayMetrics(); 
	        dm = this.getResources().getDisplayMetrics(); 
	        int screenWidth = dm.widthPixels; 
	        int screenHeight = dm.heightPixels;
			
			Button button1 = (Button)findViewById(R.id.add_from_contact);			
			Button button2 = (Button)findViewById(R.id.add_from_call_record);			
			Button button3 = (Button)findViewById(R.id.add_from_sms_record);

        	et1 = (EditText)findViewById(R.id.phoneNumberEditor);
        	et2 = (EditText)findViewById(R.id.tagEditor);
        	
	        Bundle bundle = this.getIntent().getExtras();

	    	String mSelectedNumber = null;
	    	String mTag = null;
	        if (bundle != null && 
        		(mSelectedNumber = bundle.getString("EDIT_NUMBER")) != null &&
        		mSelectedNumber.length() > 0)
	        {
	        	newnum = false;
	        	position = bundle.getInt("POSITION");
	        	
	        	et1.setText(mSelectedNumber);
	        	et1.setEnabled(false);
	        	et1.setFocusable(false);

	        	mOldTag = bundle.getString("EDIT_TAG");
	        	et2.setText(mOldTag);
	        	et2.setEnabled(true);
	        	
	        	//registerForContextMenu(et2);
	        	/*
				ToggleButton bc = (ToggleButton)findViewById(R.id.call_block_check_btn);
	        	bc.setChecked(bundle.getBoolean("block_call"));
				ToggleButton bs = (ToggleButton)findViewById(R.id.sms_block_check_btn);
	        	bs.setChecked(bundle.getBoolean("block_sms"));
	        	*/
	        	LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(screenWidth / 2 - 10, WC);
	        	
	        	CheckBox bc = (CheckBox)findViewById(R.id.call_block_check_box);
	        	bc.setChecked(bundle.getBoolean("block_call"));
	        	bc.setLayoutParams(param1);
	        	
	        	CheckBox bs = (CheckBox)findViewById(R.id.sms_block_check_box);
	        	bs.setChecked(bundle.getBoolean("block_sms"));
	        	bs.setLayoutParams(param1);
	        	
	        	button1.setEnabled(false);
	        	button2.setEnabled(false);
	        	button3.setEnabled(false);
	        } else {	        	
	        	newnum = true;
	        	
	        	/*
	        	ToggleButton bc = (ToggleButton)findViewById(R.id.call_block_check_btn);
	        	bc.setChecked(true);
				ToggleButton bs = (ToggleButton)findViewById(R.id.sms_block_check_btn);
	        	bs.setChecked(true);
	        	*/
	        	LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(screenWidth / 2 - 10, WC);
	        	
	        	CheckBox bc = (CheckBox)findViewById(R.id.call_block_check_box);
	        	bc.setChecked(true);
	        	bc.setLayoutParams(param2);
	        	
	        	CheckBox bs = (CheckBox)findViewById(R.id.sms_block_check_box);
	        	bs.setChecked(true);
	        	bs.setLayoutParams(param2);
	        	
				button1.setOnClickListener(this);
				button2.setOnClickListener(this);
				button3.setOnClickListener(this);
	        }
			/*
			button1 = (Button)findViewById(R.id.save_btn);
			button1.setOnClickListener(this);
			*/
			
			LinearLayout.LayoutParams param3 = new LinearLayout.LayoutParams(screenWidth / 2, WC);
	        Button btn = (Button)findViewById(R.id.add_number_view_ok_btn);
	        btn.setOnClickListener(this);
	        btn.setLayoutParams(param3);
	        
	        btn = (Button)findViewById(R.id.add_number_view_cancel_btn);
	        btn.setOnClickListener(this);
	        btn.setLayoutParams(param3);
	       
	        param3 = new LinearLayout.LayoutParams(screenWidth * 3 / 4 - 15, WC);
	        et2.setLayoutParams(param3);
	        
	        param3 = new LinearLayout.LayoutParams(screenWidth * 1 / 4 - 10, WC);
	        btn = (Button)findViewById(R.id.tag_select_btn);
	        btn.setOnClickListener(this);
	        btn.setLayoutParams(param3);
	        
	        
			
		}
		catch (Exception e)
		{
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
	}
	
	
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.add_from_contact:
			{
				Intent intent = new Intent();
				intent.setClass(AddNumberView.this, AddFromContactView.class);
				startActivityForResult(intent, 2);
				break;
			}
			case R.id.add_from_call_record:
			{
				Intent intent = new Intent();
				intent.setClass(AddNumberView.this, AddFromCallRecordView.class);
				startActivityForResult(intent, 3);
				break;
			}
			case R.id.add_from_sms_record:
			{
				Intent intent = new Intent();
				intent.setClass(AddNumberView.this, AddFromSmsRecordView.class);
				startActivityForResult(intent, 4);
				break;
			}
			case R.id.tag_select_btn:
			{
				new AlertDialog.Builder(this)
                .setTitle(R.string.PleaseSelectTag)
                .setItems(R.array.select_dialog_items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        /* User clicked so do some stuff */
                        String[] items = getResources().getStringArray(R.array.select_dialog_items);
                                                
                        EditText et1 = (EditText)findViewById(R.id.tagEditor);
                        et1.setText(items[which]);
                    }
                })
                .create()
                .show();
				break;
			}
			case R.id.add_number_view_ok_btn:
			{
				
				EditText et = (EditText)findViewById(R.id.phoneNumberEditor);
				EditText et1 = (EditText)findViewById(R.id.tagEditor);				
				
				String number = String.valueOf(et.getText()).trim();
				if (number.equals(""))
				{
					if (toastShowWaitHandler.IsAllowShow())
					{
			
						Toast.makeText(this, R.string.EmptyNumberIsNotAllowed, Toast.LENGTH_SHORT).show();
					}
			
					break;
				}
				
				/*
				ToggleButton button1 = (ToggleButton)findViewById(R.id.call_block_check_btn);
				ToggleButton button2 = (ToggleButton)findViewById(R.id.sms_block_check_btn);
				*/
				CheckBox bc = (CheckBox)findViewById(R.id.call_block_check_box);
	        	CheckBox bs = (CheckBox)findViewById(R.id.sms_block_check_box);
	       
				
				if (bc.isChecked() == false && bs.isChecked() == false)
				{
					if (toastShowWaitHandler.IsAllowShow())
					{			
						Toast.makeText(this, R.string.PleaseChooseBlockCallsOrMessages, Toast.LENGTH_SHORT).show();
					}
					break;
				}				
				
				if (newnum)
				{	
	
					//String[][] blnums = AppMain.db.getBlacklistNumbers();
					String[][] blnums = PhoneNumberManager.getIntance(this).getBlacklistNumbers();
					
					if (blnums != null)
					{
						for(int i = 0; i < blnums.length; i++) {
							if (PhoneNumberManager.getIntance(this).cmpNumber(number, blnums[i][0]))
							{
								if (toastShowWaitHandler.IsAllowShow())
								{
									Toast.makeText(this, R.string.TheNumberAlreadyExists, Toast.LENGTH_SHORT).show();		
								}
								return;
							}
						}
					}
				}
				/*
				if (PhoneNumberManager.getIntance(this).isContact(number))
				{
					new AlertDialog.Builder(this)
	                .setIcon(R.drawable.alert_dialog_icon)
	                .setTitle(R.string.alert_dialog_two_buttons_title_2)
	                .setMessage(R.string.alert_dialog_two_buttons_msg)
	                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which) 
	                    {
	                    	
	                    	EditText et = (EditText)findViewById(R.id.phoneNumberEditor);
	        				EditText et1 = (EditText)findViewById(R.id.tagEditor);
	        				
	        				CheckBox bc = (CheckBox)findViewById(R.id.call_block_check_box);
	        	        	CheckBox bs = (CheckBox)findViewById(R.id.sms_block_check_box);
	        	        	
	                    	Intent intent = new Intent(); 
	        			    intent.putExtra("add_number_result", String.valueOf(et.getText()));
	        			    intent.putExtra("tag_result", String.valueOf(et1.getText()));
	        			    
	        			    if (mOldTag != null)
	        			    {
	        				    if (mOldTag.equals(String.valueOf(et1.getText())))
	        				    {
	        				    	intent.putExtra("tag_changed", false);
	        				    }
	        				    else
	        				    {
	        				    	intent.putExtra("tag_changed", true);
	        				    }
	        			    }
	        			    
	        				if (bc.isChecked())
	        					intent.putExtra("call_block", true);
	        				else
	        					intent.putExtra("call_block", false);
	        				if (bs.isChecked())
	        					intent.putExtra("sms_block", true);
	        				else
	        					intent.putExtra("sms_block", false);
	        				if (newnum)
	        					setResult(1, intent);
	        				else {
	        					intent.putExtra("position", position);
	        					setResult(2, intent);
	        				}
	        				
	        				((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	        				((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et1.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	        				
	        			    finish(); 
	                        
	                    }
	                })
	                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
	                	public void onClick(DialogInterface dialog, int whichButton) {
                        
	                	}
	                })
	                .create()
	                .show();
				}
				else
				{
				*/
				Intent intent = new Intent(); 
			    intent.putExtra("add_number_result", String.valueOf(et.getText()));
			    intent.putExtra("tag_result", String.valueOf(et1.getText()));
			    
			    if (mOldTag != null)
			    {
				    if (mOldTag.equals(String.valueOf(et1.getText())))
				    {
				    	intent.putExtra("tag_changed", false);
				    }
				    else
				    {
				    	intent.putExtra("tag_changed", true);
				    }
			    }
			    
				if (bc.isChecked())
					intent.putExtra("call_block", true);
				else
					intent.putExtra("call_block", false);
				if (bs.isChecked())
					intent.putExtra("sms_block", true);
				else
					intent.putExtra("sms_block", false);
				if (newnum)
					setResult(1, intent);
				else {
					intent.putExtra("position", position);
					setResult(2, intent);
				}
				
				((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et1.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				
			    finish(); 
				
				break;
				
				  
			}
			case R.id.add_number_view_cancel_btn:
			{
				finish();
				break;
			}
			default:
				break;
		}
		
	}
	
	@Override  
	protected void onActivityResult(int requestCode, int resultCode, Intent data)   
	{   
		try
		{
			switch (resultCode)
			{
				case 3:
				{
					EditText et = (EditText)findViewById(R.id.phoneNumberEditor);
					//clear editor before new filled
					et.setText("");
					et.setText(data.getExtras().getString("contact_result"));
					
					et = (EditText)findViewById(R.id.tagEditor);
					//clear editor before new filled
					et.setText("");
					et.setText(data.getExtras().getString("contact_return_name"));
					
					break;
				}
				case 4:
				{
					EditText et = (EditText)findViewById(R.id.phoneNumberEditor);
					//clear editor before new filled
					et.setText("");
					et.setText(data.getExtras().getString("call_record_result"));
					
					et = (EditText)findViewById(R.id.tagEditor);
					//clear editor before new filled
					et.setText("");
					et.setText(data.getExtras().getString("call_record_return_name"));
				
					break;
				}
				case 5:
				{
					EditText et = (EditText)findViewById(R.id.phoneNumberEditor);
					//clear editor before new filled
					et.setText("");
					et.setText(data.getExtras().getString("sms_record_result"));
					
					et = (EditText)findViewById(R.id.tagEditor);
					//clear editor before new filled
					et.setText("");
					String name1 = data.getExtras().getString("sms_record_result_name");
					if (name1 != null && name1.length() > 2)
					{
						int len = name1.length();
						String name = name1.substring(1, len - 1);

						et.setText(name);
					}
					break;
				}
				case 6:
				{
					EditText et = (EditText)findViewById(R.id.phoneNumberEditor);
					//clear editor before new filled
					et.setText("");
					et.setText(data.getExtras().getString("add_num_from_bl_result"));
				}
				case 0:
				{
					break;
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
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{  		
		MenuItem editItem = menu.add(0, 1, 1,  "tag1");
		MenuItem deleteItem = menu.add(0, 2, 2, "tag2");
		menu.setHeaderTitle(R.string.Menu);
		super.onCreateContextMenu(menu, v, menuInfo);		
	}
	
	public boolean onContextItemSelected(MenuItem item) 
	{  
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();  
		switch (item.getItemId()) 
		{  
			case 1:
				et2.setText("tag1");
			case 2:
				et2.setText("tag2");
			default:    
				return super.onContextItemSelected(item);  
		}
	}
}
