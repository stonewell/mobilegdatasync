package com.angelstone.android.smsblocker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.angelstone.android.utils.ToastShowWaitHandler;

public class EditBlNumberView extends Activity implements OnClickListener,
														  OnCheckedChangeListener
{
	private int mPosition;
	
//	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
//	private final int FC = ViewGroup.LayoutParams.FILL_PARENT;
	
	private ToastShowWaitHandler toastShowWaitHandler = new ToastShowWaitHandler();
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.edit_bl_number_view);
	        
	        Bundle bundle = this.getIntent().getExtras();
	        mPosition = bundle.getInt("POSITION");
	        String number = bundle.getString("EDIT_NUMBER");
	        String name = bundle.getString("EDIT_NAME");
	        //String sms = bundle.getString("reply_sms");
	        
	        EditText numberEt = (EditText)findViewById(R.id.edit_bl_number_editor);
	        EditText nameEt = (EditText)findViewById(R.id.edit_bl_name_editor);
	        //EditText smsEt = (EditText)findViewById(R.id.edit_bl_number_reply_sms_editor);
        	
	        //RadioGroup radioGroup = (RadioGroup) findViewById(R.id.edit_bl_number_reply_option_radioGroup);
	        //radioGroup.setOnCheckedChangeListener(this);
	        
	        numberEt.setText(number);
	        nameEt.setText(name);
	        /*
	        if (sms == null || sms.equals(""))
	        {
	        	radioGroup.check(R.id.edit_bl_number_radio_group_ignore);
	        	smsEt.setEnabled(false);
	        	
	        	smsEt.setInputType(InputType.TYPE_NULL);
	        }
	        else
	        {
	        	radioGroup.check(R.id.edit_bl_number_radio_group_reply_sms);
	        	smsEt.setText(sms);
	        }
        	*/	
        	CheckBox bc = (CheckBox)findViewById(R.id.edit_bl_number_call_block_check_box);
        	bc.setChecked(bundle.getBoolean("block_call"));
        	
        	CheckBox bs = (CheckBox)findViewById(R.id.edit_bl_number_sms_block_check_box);
        	bs.setChecked(bundle.getBoolean("block_sms"));
	        	
	        Button btn = (Button)findViewById(R.id.edit_bl_number_view_done_btn);
	        btn.setOnClickListener(this);
	        
	        //btn = (Button)findViewById(R.id.edit_bl_number_view_cancel_btn);
	        //btn.setOnClickListener(this);
			
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
			case R.id.edit_bl_number_view_done_btn:
			{
				EditText numberEt = (EditText)findViewById(R.id.edit_bl_number_editor);
		        EditText nameEt = (EditText)findViewById(R.id.edit_bl_name_editor);
		        //EditText smsEt = (EditText)findViewById(R.id.edit_bl_number_reply_sms_editor);				
				
				CheckBox bc = (CheckBox)findViewById(R.id.edit_bl_number_call_block_check_box);
	        	CheckBox bs = (CheckBox)findViewById(R.id.edit_bl_number_sms_block_check_box);
	       
				
				if (bc.isChecked() == false && bs.isChecked() == false)
				{
					if (toastShowWaitHandler.IsAllowShow())
					{			
						Toast.makeText(this, R.string.PleaseChooseBlockCallsOrMessages, Toast.LENGTH_SHORT).show();
					}
					break;
				}				
				/*
				RadioGroup radioGroup = (RadioGroup) findViewById(R.id.edit_bl_number_reply_option_radioGroup);
	        	String replySms = null;
	        	
	        	if (radioGroup.getCheckedRadioButtonId() == R.id.edit_bl_number_radio_group_reply_sms)
	        	{
	        		if (String.valueOf(smsEt.getText()).trim().equals(""))
	        		{
	        			if (toastShowWaitHandler.IsAllowShow())
						{			
							Toast.makeText(this, R.string.replySmsCannotBeEmpty, Toast.LENGTH_SHORT).show();
						}
						break;
	        		}
	        		
	        		replySms = String.valueOf(smsEt.getText()).trim();
	        	}
	        	else
	        	{
	        		replySms = "";
	        	}
				*/
				Intent intent = new Intent(); 
			    intent.putExtra("edited_number_return", String.valueOf(numberEt.getText()));
			    intent.putExtra("edited_name_return", String.valueOf(nameEt.getText()));
			    //intent.putExtra("edited_reply_sms_return", replySms);
			    
				if (bc.isChecked())
					intent.putExtra("call_block", true);
				else
					intent.putExtra("call_block", false);
				if (bs.isChecked())
					intent.putExtra("sms_block", true);
				else
					intent.putExtra("sms_block", false);
		
				intent.putExtra("position", mPosition);
				setResult(2, intent);
		
				
				((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(nameEt.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				//((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(smsEt.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				
			    finish(); 
				
				break;
				
				  
			}
			/*
			case R.id.edit_bl_number_view_cancel_btn:
			{
				finish();
				break;
			}
			*/
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
	public void onCheckedChanged(RadioGroup group, int checkedId)
	{
		/*
		EditText smsEt = (EditText)findViewById(R.id.edit_bl_number_reply_sms_editor);
		
		switch (checkedId)
		{
			case R.id.edit_bl_number_radio_group_reply_sms:
			{			
				smsEt.setEnabled(true);
				smsEt.setInputType(InputType.TYPE_CLASS_TEXT);
				break;
			}
			case R.id.edit_bl_number_radio_group_ignore:
			{
				smsEt.setEnabled(false);
				
				smsEt.setInputType(InputType.TYPE_NULL);
				
				((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(smsEt.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				break;
			}
			default:
				break;
		}
		*/
	} 
}
