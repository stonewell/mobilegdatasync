package com.angelstone.android.smsblocker;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.angelstone.android.smsblocker.store.PhoneNumberManager;

public class SystemSettingView extends Activity implements OnItemClickListener,
														   OnClickListener,
														   OnItemSelectedListener
{
	private String[] mFwOnSettingNames = null;
	
	private String[] mFwOffSettingNames = null;;
	
	private String[] mSettingKeys = {"Firewall_switch", 
			                         "call_reject_switch", 
			                         "sms_reject_switch", 
			                         /*"show_location",*/ 
			                         /*"advance_switch",*/
			                         /*"unknown_call_showing"*/};
	private ListView mListview;
	
	private static final String CLOSED_PHONE = "13528795183";
	private static final String EMPTY_PHONE = "13412345678";
	private static final String UNREACHABLE_PHONE = "13999999999";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
		try
		{
	        super.onCreate(savedInstanceState);
	        
	        setContentView(R.layout.system_setting_mgr_view);

	        mFwOnSettingNames = new String[]{this.getResources().getString(R.string.EnableCallBlocking),
				this.getResources().getString(R.string.EnableIncomingCallBlocking),
				this.getResources().getString(R.string.EnableSMSBlocking) /*"Show Caller Location",*/
				/*this.getResources().getString(R.string.EnableAutoReply),*/
				/*this.getResources().getString(R.string.EnableUnknownCallShowing)*/};
			
	        mFwOffSettingNames = new String[] {this.getResources().getString(R.string.EnableCallBlocking)};
	        
	        mListview = (ListView)this.findViewById(R.id.sys_setting_list);
	        
	        boolean ret = PhoneNumberManager.getIntance(this).readSetting("Firewall_switch");
	        
	        if (ret)
	        {
	        	refreshList(mFwOnSettingNames);
	        }
	        else
	        {
	        	refreshList(mFwOffSettingNames);
	        }
	        
	        mListview.setOnItemClickListener(this);
	        
	        /*
	         * In puma, we don't provide the location showing,so we set this setting always false
	         */
	        
	        PhoneNumberManager.getIntance(this).writeSetting("show_location", false);
	        
            /*
	        Button btn = (Button)findViewById(R.id.show_filter_editor);
	        btn.setOnClickListener(this);
            */

	        /*
	         * In puma, we don't provide the hungup tone setting
	         */
	        /*
	        Spinner s = (Spinner)findViewById(R.id.sys_setting_Hungup_tone_spinner);

	        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	                this, R.array.hungupTone, android.R.layout.simple_spinner_item);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        s.setAdapter(adapter);
	        
	        
	        String hungupTonePos = AppMain.db.readSettingString("call_hungup_tone");
	        
	        if (hungupTonePos != null)
	        {
	        	s.setSelection(Integer.valueOf(hungupTonePos));
	        }
	        else
	        {
	        	s.setSelection(Integer.valueOf(0));
	        	AppMain.db.writeSettingString("call_hungup_tone", "0");
	        }
	        s.setOnItemSelectedListener(this);
	        */
		}
		catch (Exception e)
		{
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
        
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	{
		try
		{
			switch (position)
			{
				case 0:
				{
					if (mListview.isItemChecked(position))
					{
						//mListview.setItemChecked(position, true);
						
						//Log.d("scfw", "SystemSettingView:" + String.valueOf(BootStarter.isSetCallListening));
						/*
						if (!bootStarter.isSetCallListening)
						{
							this.startService(new Intent(this, CallFwService.class));
							bootStarter.isSetCallListening = true;
						}
						*/
						PhoneNumberManager.getIntance(this).writeSetting("Firewall_switch", true);
						
						ActivityLog.logInfo(this,getString(R.string.LogCallBlock),getString(R.string.CallBlockingOn));
						
						refreshList(mFwOnSettingNames);
						
						mListview.setEnabled(true);

						if (PhoneNumberManager.getIntance(this).readSetting("call_reject_switch"))
						{		
							/*
							silenceRejectCall.registerBlContact();
							
							Log.d("scfw", "SystemSettingView:registerBlContact");
							
							if (AppMain.db.readSetting("advance_switch") == true)
							{
								silenceRejectCall.silenceContacts(this);
							}
							*/
							/*Thread  thread = new Thread()
							{
								public void run()
								{
									silenceRejectCall.registerBlContact();
									
									Log.d("scfw", "SystemSettingView:registerBlContact");
									
									if (AppMain.db.readSetting("advance_switch") == true)
									{
										silenceRejectCall.silenceContacts(SystemSettingView.this);
									}
								}
							};
							
							thread.start();
							*/
						}		
						
						 /*
				         * In puma, we don't provide the hungup tone setting
				         */

						/*
						//open call forward with the history setting record
						String forwardCallNum = new String();
						int hungupToneType = Integer.valueOf(AppMain.db.readSettingString("call_hungup_tone"));
						
						if (hungupToneType == 0)
						{
							Settings.System.putString(getContentResolver(), 
									"hangup_setting", "off");
							break;
						}
						else if (hungupToneType == 1)
						{
							forwardCallNum = this.EMPTY_PHONE;
						}
						else if (hungupToneType == 2)
						{
							forwardCallNum = this.CLOSED_PHONE;
						}
						else if (hungupToneType == 3)
						{
							forwardCallNum = this.UNREACHABLE_PHONE;
						}
						
						ITelephony tpCallModule = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
						
						try
						{
							StringBuilder strBuilder = new StringBuilder();
							strBuilder.append("tel:");
							strBuilder.append("**67*");
							strBuilder.append(forwardCallNum);
							strBuilder.append("%23");
							tpCallModule.call(strBuilder.toString());
							
							Settings.System.putString(getContentResolver(), 
									"hangup_setting", "on");
						} 
						catch (Exception e)
						{
							Log.d("scfw", this.toString() + ":" + e.getClass().toString());
						}
						*/
						
					}
					else
					{		
						//mListview.setItemChecked(position, false);
						//this.stopService(new Intent(this, CallFwService.class));
						PhoneNumberManager.getIntance(this).writeSetting("Firewall_switch", false);

						ActivityLog.logInfo(this,getString(R.string.LogCallBlock),getString(R.string.CallBlockingOff));
						
						refreshList(mFwOffSettingNames);	
						
						/*if (AppMain.db.readSetting("call_reject_switch"))
						{
							silenceRejectCall.unregisterBlContact();
							
							if (AppMain.db.readSetting("advance_switch") == true)
							{
								silenceRejectCall.unSilenceContacts(SystemSettingView.this);
							}
						}*/
		
						 /*
				         * In puma, we don't provide the hungup tone setting
				         */
				        /*
						
						/*
						//close call forward
						if (!AppMain.db.readSettingString("call_hungup_tone").equals("0"))
						{
							ITelephony tpCallModule = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
							StringBuilder strBuilder = new StringBuilder();
							strBuilder.append("tel:");
							strBuilder.append("%23%2367%23");
							tpCallModule.call(strBuilder.toString());
							
							Settings.System.putString(getContentResolver(), 
									"hangup_setting", "off");
						}
						*/

					}
					break;
					
				}
				case 1:
				{
					if (mListview.isItemChecked(position))
					{
						//mListview.setItemChecked(position, true);
						PhoneNumberManager.getIntance(this).writeSetting("call_reject_switch", true);
						refreshList(mFwOnSettingNames);
	
						/*
						silenceRejectCall.registerBlContact();
						
						if (AppMain.db.readSetting("advance_switch") == true)
						{
							silenceRejectCall.silenceContacts(this);
						}
						*/
						
						/*Thread  thread = new Thread()
						{
							public void run()
							{
								silenceRejectCall.registerBlContact();
								
								if (AppMain.db.readSetting("advance_switch") == true)
								{
									silenceRejectCall.silenceContacts(SystemSettingView.this);
								}
							}
						};
						thread.start();
						*/
					}
					else
					{
						//mListview.setItemChecked(position, false);
						PhoneNumberManager.getIntance(this).writeSetting("call_reject_switch", false);
						PhoneNumberManager.getIntance(this).writeSetting("advance_switch", false);
						refreshList(mFwOnSettingNames);
	
						/*silenceRejectCall.unregisterBlContact();
						
						if (AppMain.db.readSetting("advance_switch") == true)
						{
							silenceRejectCall.unSilenceContacts(this);
						}*/
					}
					break;
				}
				case 2:
				{
					if (mListview.isItemChecked(position))
					{
						//mListview.setItemChecked(position, true);
						PhoneNumberManager.getIntance(this).writeSetting("sms_reject_switch", true);
						refreshList(mFwOnSettingNames);
					}
					else
					{
						//mListview.setItemChecked(position, false);
						PhoneNumberManager.getIntance(this).writeSetting("sms_reject_switch", false);
						refreshList(mFwOnSettingNames);
					}
					break;
				}
				/*
				case 2:
				{
					if (mListview.isItemChecked(position))
					{
						mListview.setItemChecked(position, true);
						AppMain.db.writeSetting("show_location", true);
						refreshList(mFwOnSettingNames);
					}
					else
					{
						mListview.setItemChecked(position, false);
						AppMain.db.writeSetting("show_location", false);
						refreshList(mFwOnSettingNames);
					}
					break;
				}
				
				case 3:
				{
					if (mListview.isItemChecked(position))
					{		
						PhoneNumberManager.getIntance(this).writeSetting("advance_switch", true);
						PhoneNumberManager.getIntance(this).writeSetting("call_reject_switch", true);
						refreshList(mFwOnSettingNames);
						
						
					}
					else
					{
						PhoneNumberManager.getIntance(this).writeSetting("advance_switch", false);
						refreshList(mFwOnSettingNames);
						
						
					}
					break;
				}
				
				case 3:
				{
					if (mListview.isItemChecked(position))
					{		
						PhoneNumberManager.getIntance(this).writeSetting("unknown_call_showing", true);
					}
					else
					{
						PhoneNumberManager.getIntance(this).writeSetting("unknown_call_showing", false);
					}
					break;
				}
				*/
				default:
					break;
			
			}
		}
		catch (Exception e)
		{
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
		
		
	}
	
	public void refreshList(String[] settingNames)
	{
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.simple_list_item_multiple_choice, settingNames);
        
        mListview.setAdapter(adapter);
        
        mListview.setItemsCanFocus(false);
        mListview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
        
        
        for (int i = 0; i < settingNames.length; i++)
        {
        	mListview.setItemChecked(i, PhoneNumberManager.getIntance(this).readSetting(mSettingKeys[i]));
        }
          
  
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
        /*
		Intent intent = new Intent();
		intent.setClass(this, SmsSceneMgrView.class);
		startActivityForResult(intent, 2);
        */
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
	{
		String forwardCallNum = new String();
		
		if (position == Integer.valueOf(PhoneNumberManager.getIntance(this).readSettingString("call_hungup_tone")))
		{
			return;
		}
		
		//If firewall switch is closed, just only change the setting record value ,do not execute the real call forward action
		if (!PhoneNumberManager.getIntance(this).readSetting("Firewall_switch"))
		{
			PhoneNumberManager.getIntance(this).writeSettingString("call_hungup_tone", String.valueOf(position));
			return;
		}
		
		if(position == 0)	
		{	
//			ITelephony tpCallModule = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
//			
//			try
//			{
//				if (!PhoneNumberManager.getIntance(this).readSettingString("call_hungup_tone").equals("0"))
//				{
//					StringBuilder strBuilder = new StringBuilder();
//					strBuilder.append("tel:");
//					strBuilder.append("%23%2367%23");
//					tpCallModule.call(strBuilder.toString());
//					
//					PhoneNumberManager.getIntance(this).writeSettingString("call_hungup_tone", String.valueOf(position));
//					Settings.System.putString(getContentResolver(), 
//							"hangup_setting", "off");
//				}
//			} 
//			catch (Exception e)
//			{
//				Log.d("scfw", this.toString() + ":" + e.getClass().toString());
//			}
	
		}
		else
		{
			if(position == 1)
			{
				PhoneNumberManager.getIntance(this).writeSettingString("call_hungup_tone", String.valueOf(position));
				forwardCallNum = SystemSettingView.EMPTY_PHONE;
				Settings.System.putString(getContentResolver(), 
						"hangup_setting", "on");
			}
			else if (position == 2)
			{
				PhoneNumberManager.getIntance(this).writeSettingString("call_hungup_tone", String.valueOf(position));
				forwardCallNum = SystemSettingView.CLOSED_PHONE;
				Settings.System.putString(getContentResolver(), 
						"hangup_setting", "on");
			}
			else if (position == 3)
			{
				PhoneNumberManager.getIntance(this).writeSettingString("call_hungup_tone", String.valueOf(position));
				forwardCallNum = SystemSettingView.UNREACHABLE_PHONE;
				Settings.System.putString(getContentResolver(), 
						"hangup_setting", "on");
			}
			
//			ITelephony tpCallModule = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
//			
//			try
//			{
//				StringBuilder strBuilder = new StringBuilder();
//				strBuilder.append("tel:");
//				strBuilder.append("**67*");
//				strBuilder.append(forwardCallNum);
//				strBuilder.append("%23");
//				tpCallModule.call(strBuilder.toString());
//			} 
//			catch (Exception e)
//			{
//				Log.d("scfw", this.toString() + ":" + e.getClass().toString());
//			}
		}
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0)
	{
		
	}
}
