package com.angelstone.android.callfirewall;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import com.android.internal.telephony.ITelephony;
import com.symantec.cfw.db.EventLog;
import com.symantec.cfw.db.PhoneNumberDisposition;
import com.symantec.cfw.db.PhoneNumberManager;
import com.symantec.mobilesecurity.R;
import com.symantec.mobilesecurity.licensemanager.LicenseManager;
import com.symantec.util.GlobalObjects;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.provider.CallLog;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CallFwService extends Service
{

	private boolean isHook = false;
	private String from = null;
	private static ITelephony tpCallModule = null;
	private static boolean isAfterRing = false;	
	private static boolean isFirstInCallScreen = true;
	//private ArrayList<String> mIncomingNumbers = new ArrayList<String>();
	
	private static String mIncomingNumber;
	
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private final int FC = ViewGroup.LayoutParams.FILL_PARENT;
	
	public static PhoneNumberManager db = null;
	public static CallerLocation cl = null;
	public static SysCompat sys = null;
	
	public static boolean mRejectCall = false;
	
	public static AudioManager audioManager = null;
	
	PhoneNumberDisposition disp = new PhoneNumberDisposition();
	
	public static int mRingingCallCount;
	
	
	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void onDestroy()
	{
		Log.d("fwfwfw", "destroy");	
		super.onDestroy();
	}
	 
	public void onCreate()
	{
        super.onCreate();

        db = PhoneNumberManager.getIntance(this);
        
        cl = new CallerLocation( this );
        sys = SysCompat.register(this);
        
        /*
         * this function can raise the priority of the service
         */
        sys.setServiceForeground(this);

        //silenceRejectCall s = new silenceRejectCall( this );        
        
        String url = "content://sms/"; 
        Uri uri = Uri.parse(url); 
        //getContentResolver().registerContentObserver(uri, true, new SMSObserver(handler, CallFwService.this.getApplicationContext())); 

        getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, new CallLogOberver(handler, CallFwService.this.getApplicationContext())); 
     
  
        if (cl == null)
        {
        	Log.d("scfw", "cl is null");
        }
        
        TelephonyManager mTelephonyMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyMgr.listen(new TeleListener(), PhoneStateListener.LISTEN_CALL_STATE);

        bootStarter.isSetCallListening = true;
        
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
          
        tpCallModule = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));  
        
        GlobalObjects.mCurrentRingtoneMode = audioManager.getRingerMode();
        
        /*
         * Due to remove the setting tab, old user can not enable the call block, if he/she disable the call blocking
         * use the old version app,so after live update ,the old user can not use the call blocking , so we should enable
         * it automatically.
         * 
         */
        PhoneNumberManager.getIntance(this).writeSetting("Firewall_switch", true);
		PhoneNumberManager.getIntance(this).writeSetting("call_reject_switch", true);
		PhoneNumberManager.getIntance(this).writeSetting("sms_reject_switch", true);
		
		mRingingCallCount = 0;
      
		/*
		StringBuilder sb = new StringBuilder();

		sb.append("DEVICE : ").append(Build.DEVICE).append(",,,");
		sb.append("DISPLAY : ").append(Build.DISPLAY).append(",,,");
		sb.append("HOST : ").append(Build.HOST).append(",,,");
		sb.append("MANUFACTURER : ").append(Build.MANUFACTURER).append(",,,");
		sb.append("MODEL : ").append(Build.MODEL).append(",,,");
		sb.append("PRODUCT : ").append(Build.PRODUCT).append(",,,");
		sb.append("BOARD : ").append(Build.BOARD).append(",,,");
		sb.append("BRAND : ").append(Build.BRAND).append(",,,");
		sb.append("CPU_ABI : ").append(Build.CPU_ABI).append(",,,");
		sb.append("FINGERPRINT : ").append(Build.FINGERPRINT).append(",,,");
		sb.append("ID : ").append(Build.ID).append(",,,");
		sb.append("MANUFACTURER : ").append(Build.MANUFACTURER).append(",,,");
		sb.append("TAGS : ").append(Build.TAGS).append(",,,");
		sb.append("TYPE : ").append(Build.TYPE).append(",,,");
		sb.append("USER : ").append(Build.USER).append(",,,");
			
		Log.d("scfw1", sb.toString());
		*/
	}
	
	class TeleListener extends PhoneStateListener 
	{
		
	    public void onCallStateChanged(int state, String incomingNumber) 
	    {   
	        super.onCallStateChanged(state, incomingNumber);
	       
	        Log.d("scfw1", "CallStateChanged: " + state + " : " + incomingNumber);
	        try
			{
				Log.d("scfw1", "ActivePhoneType: " + tpCallModule.getActivePhoneType());
				
			} 
	        catch (RemoteException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        
	        
	        if (!CallFwService.db.readSetting("Firewall_switch") 
	        	|| !LicenseManager.isLicenseValid(CallFwService.this))
	        {
	        	Log.d("scfw", "Firewall switch off or License expired");
	        	return;
	        }
	        
	        switch (state) 
	        {
	            case TelephonyManager.CALL_STATE_IDLE:
	            {
	            	
	            	mRingingCallCount = 0;
	            	
	            	audioManager.setRingerMode(GlobalObjects.mCurrentRingtoneMode);
	            	
	            	String out = "CALL_STATE_IDLE:" + incomingNumber;
	            	Log.d("scfw", out);
	            
	            	Log.d("scfw1", out);
	            	
	            	try
	            	{
	            		if (isAfterRing)
	            		{
	            			
	            			if ((!db.isContact(mIncomingNumber)) 
		            			    && (!db.isDontAsk(mIncomingNumber))
		            			    && (!db.isBlacklisted(mIncomingNumber))
		            			    && db.readSetting("unknown_call_showing"))
		            			{
	            					Log.d("scfw1", "show hungup option");
	            				
				            		Intent intent = new Intent(CallFwService.this,HungupOptionActivity.class); 
				            		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
				            				        | Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				        	        Bundle bundle = new Bundle();
				    				bundle.putString("INCOME_NUMBER", mIncomingNumber);
				    				intent.putExtras(bundle);
				        	        startActivity(intent);
		            			}
		            			
	            			}

	            			isAfterRing = false;
	        
	        	      
	            	}
	            	catch (Exception e)
	            	{
	            		Log.d("scfw", e.getClass().toString());
	            	}
	            	
	            	
	            	
	            	isHook = true;
	            		           
	               break;
	            }
	            case TelephonyManager.CALL_STATE_OFFHOOK:
	            {
	            	String out = "CALL_STATE_OFFHOOK:" + incomingNumber;
	            	
	            	isHook = true;
	            	
	            	Log.d("scfw", out);
	            	
	            	Log.d("scfw1", out);
	               break;
	            }
	            case TelephonyManager.CALL_STATE_RINGING: 
				{
					String out = "CALL_STATE_RINGING:" + incomingNumber;
					
					mRingingCallCount++;
					
					Log.d("scfw1", out);
					Log.d("scfw1", "Ringing Call Count : " + mRingingCallCount);
					
					try
					{
						//audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
						if (sys.SetRingerSilence(audioManager, true))
						{		
							if (GlobalObjects.mCurrentRingtoneMode != AudioManager.RINGER_MODE_SILENT)
			            	{
			            		GlobalObjects.mIsRingToneModeChgByFwService = true;
			            	}
						}
				 		 
						mIncomingNumber = incomingNumber;
													
						Log.d("scfw", "queryLocation");	
						
						Log.d("scfw1", "ActivePhoneType : " + tpCallModule.getActivePhoneType());

						if (CallFwService.db.readSetting("call_reject_switch"))
						{							
							disp = CallFwService.db.queryAction(incomingNumber);
							
							if (disp.m_CallAction == PhoneNumberDisposition.CALL_REJECT)
							{										
								Log.d("scfw", "enter");
								
								Log.d("scfw1", "enter");
								
								mRejectCall = true;

				            	//audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
								
								if (mRingingCallCount >= 2 
								    && Build.MODEL.contains("PC36100"))
								{
									Log.d("scfw1", "Already has a incoming call, don't block this call");					
									return;	
								}
								
								Log.d("scfw1", Build.MODEL);
								
								if (sys.SetRingerSilence(audioManager, false))
								{
								
									if (GlobalObjects.mCurrentRingtoneMode != AudioManager.RINGER_MODE_SILENT)
					            	{
					            		GlobalObjects.mIsRingToneModeChgByFwService = true;
					            	}
								}
								
								
 
								if (isFirstInCallScreen)
								{
									long at = SystemClock.uptimeMillis();
									Cursor cur = CallFwService.this.getApplicationContext().getContentResolver().query(
											CallLog.Calls.CONTENT_URI,
											null, null, null, "date DESC");//CallLog.Calls.DEFAULT_SORT_ORDER);
									if (cur != null)
									{
										cur.moveToFirst();	
										if (cur.getCount() > 0)
										{
											int idx_date = cur.getColumnIndex(CallLog.Calls.DATE );
											long time = Long.parseLong( cur.getString( idx_date ) );
											
											Date now = new Date();
											if (now.getTime() - at >= time)
												Thread.sleep(3000);
											Log.d("scfw", now.getTime() + ", " + Long.toString(at) + ", " + Long.toString(time));
										}
										cur.close();
									}
									isFirstInCallScreen = false;
								}
								boolean isEndCall = tpCallModule.endCall();
								Log.d("scfw1", "endCall: " + incomingNumber);
								
							
								
								if (isEndCall)
								{
									Log.d("scfw1", "endCall: true");
								}
								else
								{
									Log.d("scfw1", "endCall: false");
								}
				
								Log.d("scfw", "endCall");	

								if (disp.m_ReplySms != null && !disp.m_ReplySms.equals(""))
								{
									Thread t = new Thread()
									{
										public void run() 
										{
											try 
											{
												Thread.sleep(3000);
											} 
											catch (InterruptedException e) 
											{
												Log.d("scfw", this.toString() + ":" + e.getMessage());
											}
											if (mIncomingNumber != null)
											{
												sys.sendMsg(mIncomingNumber, disp.m_ReplySms);	
												
												Log.d("scfw1", "sendMsg");
											}
										}
									}; 
									t.start();
									
									
									
									//WriteToCallRejectLogForAdvCall(incomingNumber, disp.m_ReplySms);
									
							       	
								}
								
								//write call reject log
								WriteToCallRejectLog(incomingNumber, disp.m_ReplySms);		
								
								
								if (CallRejectLogView.getIntance() != null)
						       	{
						       		CallRejectLogView.getIntance().refreshList_2();
						       	}
						       	
								
								break;
							}
						}
						audioManager.setRingerMode(GlobalObjects.mCurrentRingtoneMode);						
						isAfterRing = true;
						
						from = new String();
						
						if (CallFwService.cl == null)
						{
							Log.d("scfw", "CallFwService.cl");
							
							Log.d("scfw1", "CallFwService.cl");
						}
												
						if ((from = CallFwService.cl.queryLocation(incomingNumber)) == null)
							from = CallFwService.this.getApplicationContext().getResources().getString(R.string.Unknown);
						
						if (!from.equals(CallerLocation.UNINITIALIZED))
						{
							isHook = false;								
							
							Thread t = new Thread()
							{
								public void run() 
								{ 
									while (!isHook)
									{
										Message msg = new Message();
										Bundle msgdata = new Bundle();
										msgdata.putString("city", from);
										msg.setData(msgdata);
										handler.sendMessage(msg);
		
										try 
										{
											Thread.sleep(5000);
										} 
										catch (InterruptedException e) 
										{
											// TODO Auto-generated catch block
											Log.d("scfw", this.toString() + ":" + e.getClass().toString());
										}
									}									
								} 

							}; 
							t.start(); 							
						}
								    		
			    		Log.d("scfw", "call complete");		
			    		
			    		Log.d("scfw1", "call complete");	
			    		break;
					} 
					catch (Exception e)
					{
						Log.d("scfw", this.toString() + ":" + e.getClass().toString());
					}
				}
	            default:
	               break;
	        }
	     }
	    
	    private void WriteToCallRejectLog(String incomeNumber, String replySms)
		{
	    	EventLog evt = new EventLog(PhoneNumberUtils.formatNumber(incomeNumber), EventLog.LOG_TYPE_CALL );
	    	
	    	String tag = CallFwService.db.getTagByNumber(incomeNumber);
	    	evt.setTagOrName(tag);
	    	evt.setReplySmsTxt(replySms);
	    	evt.setBlockType(EventLog.CALL_LOG_BLOCK_TYPE_BL);
	    	
	    	CallFwService.db.writeLog(evt);		
		}
	    
	    private void WriteToCallRejectLogForAdvCall(String incomeNumber, String replySms)
	    {
	    	EventLog evt = new EventLog(incomeNumber, EventLog.LOG_TYPE_CALL );
	    	String name = CallFwService.db.getNameByNumber(incomeNumber);
	    	//String name = "aaa";
	    	String sceneName = CallFwService.db.advGetActiveScene();
	    	
	    	evt.setTagOrName(name);
	    	evt.setSceneOrKeyword(sceneName);
	    	evt.setBlockType(EventLog.CALL_LOG_BLOCK_TYPE_ADV);
	    	evt.setReplySmsTxt(replySms);
	    	
	    	CallFwService.db.writeLog(evt);	
	    }
	
	}
	
	private Handler handler = new Handler() 
	{
        @Override
        public void handleMessage(Message msg) 
        {
			Log.d("scfw", "Display Toast");
			
			if (!CallFwService.db.readSetting("show_location"))
			{
				return;
			}
			
			ShowLocationMsg(msg.getData().getString("city"), CallFwService.this.getApplicationContext());
        }
	};
	
	private void ShowLocationMsg(String location, Context ctx)
	{	
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setBackgroundColor(Color.DKGRAY);
        LinearLayout.LayoutParams param = 
                                  new LinearLayout.LayoutParams(FC, FC);
        layout.setLayoutParams(param);
     
        TextView text = new TextView(ctx.getApplicationContext());
        param = new LinearLayout.LayoutParams(320, 40);
        text.setText(location);
        text.setTextColor(Color.WHITE);
        text.setTextSize(22);
        text.setGravity(Gravity.CENTER);
     
        layout.addView(text, param);
        Toast toast = new Toast(ctx);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
	}
	
	private boolean isAdvanceOn()
	{
		return CallFwService.db.readSetting("advance_switch");
	}
	
	public static String getIncomeCallNumber()
	{
		return mIncomingNumber;
	}
	/*
	public static void SetSettingVibrateType(int type)
	{
		mSettingVibrateType = type;
	}*/
}
