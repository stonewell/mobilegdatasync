
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.angelstone.android.smsblocker.store.AdvScene;
import com.angelstone.android.smsblocker.store.PhoneNumberManager;
import com.angelstone.android.utils.ToastShowWaitHandler;


public class NewAdvanceSceneView extends Activity implements OnClickListener,
															 OnCheckedChangeListener,
															 OnItemLongClickListener,
															 OnItemClickListener
{
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private final int FC = ViewGroup.LayoutParams.FILL_PARENT;
	
	private ListView mListview;
	private int mListLongClickPos;
	private int mChkedSmsPos = 0;
	
	private ToastShowWaitHandler toastShowWaitHandler = new ToastShowWaitHandler();
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
		try
		{
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.new_advance_scene_edit_view_layout);
	        
	        mListview = (ListView) findViewById(R.id.new_advance_scene_reply_sms_list);
	        mListview.setItemsCanFocus(false);
	        mListview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	        
	        String[] smsBodies = GetSmsBodiesFromDb();
	        
	        refreshList(smsBodies);
	        
	        registerForContextMenu(mListview);
	        mListview.setOnItemLongClickListener(this);
	        mListview.setOnItemClickListener(this);
	        
	        DisplayMetrics dm = new DisplayMetrics(); 
	        dm = this.getResources().getDisplayMetrics(); 
	        int screenWidth = dm.widthPixels; 
	        int screenHeight = dm.heightPixels;        
	        
	        /*
	        ToggleButton tbtn = (ToggleButton)findViewById(R.id.new_scene_all_contact_check_btn);
	        tbtn.setOnCheckedChangeListener(this);
	        
	        tbtn = (ToggleButton)findViewById(R.id.new_scene_all_unknown_call_check_btn);
	        tbtn.setOnCheckedChangeListener(this);
	        */
	        
	        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(screenWidth / 2 - 10, WC);
	        CheckBox cb = (CheckBox)findViewById(R.id.new_scene_all_contact_check_box); 
	        cb.setLayoutParams(param);
	        
	        cb = (CheckBox)findViewById(R.id.new_scene_all_unknown_call_check_box); 
	        cb.setLayoutParams(param);
	        
	        ImageButton btn = (ImageButton)findViewById(R.id.new_advance_scene_add_new_reply_sms_btn);
	        btn.setOnClickListener(this);
	  
	        Button btn1 = (Button)findViewById(R.id.new_advance_scene_ok_btn);
	        btn1.setOnClickListener(this);
	        btn1.setLayoutParams(param);
	        
	        btn1 = (Button)findViewById(R.id.new_advance_scene_cancel_btn);
	        btn1.setOnClickListener(this);
	        btn1.setLayoutParams(param);
	        
	        
		}
		catch (Exception e)
		{
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
		
		
	}
	
	@Override
    protected void onDestroy()
	{
		super.onDestroy();
	}

	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.new_advance_scene_add_new_reply_sms_btn:
			{
				Intent intent = new Intent();
				intent.setClass(this, NewReplySmsEditorView.class);
				startActivityForResult(intent, 1);
				break;
			}
			case R.id.new_advance_scene_ok_btn:
			{
				try 
				{
					EditText et = (EditText)findViewById(R.id.new_advance_scene_edit_name_editor);
	
					if (String.valueOf(et.getText()).trim().equals(""))
					{
						if (toastShowWaitHandler.IsAllowShow())
						{
							Toast.makeText(this, 
								R.string.EmptyNameNotAllowed, Toast.LENGTH_SHORT).show();
						}
						return;
					}
					if (isSceneExist(String.valueOf(et.getText()).trim()))
					{
						if (toastShowWaitHandler.IsAllowShow())
						{
						
							Toast.makeText(this, 
								R.string.SceneAlreadyExists, Toast.LENGTH_SHORT).show();
						}
						return;
					}
					/*
					ToggleButton button1 = (ToggleButton)findViewById(R.id.new_scene_all_contact_check_btn);
					ToggleButton button2 = (ToggleButton)findViewById(R.id.new_scene_all_unknown_call_check_btn);
					*/
					
					CheckBox cb1 = (CheckBox)findViewById(R.id.new_scene_all_contact_check_box); 	        
			        CheckBox cb2 = (CheckBox)findViewById(R.id.new_scene_all_unknown_call_check_box); 
	
					
					if (cb1.isChecked() == false && cb2.isChecked() == false)
					{
						if (toastShowWaitHandler.IsAllowShow())
						{
							Toast.makeText(this, R.string.PleaseChooseContactOrUnknown, Toast.LENGTH_SHORT).show();	
						}
						return;
					}									 
					
					SaveNewScene(String.valueOf(et.getText()));
					Intent intent = new Intent(); 
					intent.putExtra("NEW_SCENE_RETURN", String.valueOf(et.getText()));
					setResult(1, intent);
					
					((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				    
    				finish();  
				    
				}
				catch (Exception e)
				{
					Log.d("scfw", this.toString() + ":" + e.getClass().toString());
				}
				
				break;
			}
			case R.id.new_advance_scene_cancel_btn:
			{
				EditText et = (EditText)findViewById(R.id.new_advance_scene_edit_name_editor);
				((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				
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
		switch (resultCode)
		{
			case 1:
			{
				
				String[] smsAbStract = GetSmsBodiesFromDb();
				
		        refreshList(smsAbStract);
		        break;
			}
			case 2:
			{			
				String[] smsAbStract = GetSmsBodiesFromDb();
		        refreshList(smsAbStract);
		        break;
			}
			default:
				break;
		}
	}  
	
	private void refreshList(String[] texts)
	{	
		if (texts != null)
		{
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(FC, texts.length * 60 + 60);
			
			Log.d("scfw", "refreshList" + Integer.toString(texts.length));
			
	        mListview.setLayoutParams(param);
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					R.layout.simple_list_item_single_choice_1, texts);
			
	        mListview.setAdapter(adapter);
	
	        CheckActiveSms();
		}
		else
		{
			mListview.setAdapter(null);
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{    
		//menu.add(0, 0, 0, "����").setIcon(this.getResources().getDrawable(R.drawable.ic_menu_save));;
		//return true;
		return false;
	}
	
	private boolean isSceneExist(String sceneName)
	{
		String[] list = PhoneNumberManager.getIntance(this).advGetAllScenes();
		
		if (list != null)
		{
			for (int i = 0; i < list.length; i++)
			{
				if (list[i].equals(sceneName))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	private int SaveNewScene(String sceneName)
	{
		int ret = -1;
		
		String sms_str = null;
		
		if (!mListview.isItemChecked(0))
		{
		
		String[] allSmsBodies = PhoneNumberManager.getIntance(this).advGetAllSms();
		
			if (allSmsBodies != null)
			{
				for (int i = 1; i <= allSmsBodies.length; i++)
				{

					if (mListview.isItemChecked(i))
					{
						sms_str = allSmsBodies[i - 1];						
					}
				}
			}
		}
		
		int target_group = 0;
		
		//ToggleButton tbtn = (ToggleButton)findViewById(R.id.new_scene_all_contact_check_btn);
		CheckBox cb = (CheckBox)findViewById(R.id.new_scene_all_contact_check_box);
		
        if (cb.isChecked())
        {
        	target_group |= AdvScene.TARGET_GROUP_CONTACTS;
        }
        else
        {
        	target_group &= ~AdvScene.TARGET_GROUP_CONTACTS;
        }
        
        //tbtn = (ToggleButton)findViewById(R.id.new_scene_all_unknown_call_check_btn);
        cb = (CheckBox)findViewById(R.id.new_scene_all_unknown_call_check_box);
        
        if (cb.isChecked())
        {        
        	target_group |= AdvScene.TARGET_GROUP_UNKNOWN;
        }
        else
        {   
        	target_group &= ~AdvScene.TARGET_GROUP_UNKNOWN;
        }
        PhoneNumberManager.getIntance(this).advAddNewScene(sceneName, target_group, 0, sms_str);		
		return ret;
	}
	
	
	private String[] GetSmsBodiesFromDb()
	{	
		String[] smses = null;
		
		if (PhoneNumberManager.getIntance(this).advGetAllSms() != null)
		{
			smses = new String[PhoneNumberManager.getIntance(this).advGetAllSms().length + 1];
		
			smses[0] = this.getResources().getString(R.string.DontReply);
		
			for (int i = 0; i < PhoneNumberManager.getIntance(this).advGetAllSms().length; i++)
			{
				smses[i + 1] = PhoneNumberManager.getIntance(this).advGetAllSms()[i];
			}
		}
		else
		{
			smses = new String[1];
			smses[0] = this.getResources().getString(R.string.DontReply);
		}
		
		return smses;
	}
	
	private void CheckActiveSms()
	{	
		if (mChkedSmsPos >= 0 && mListview.getCount() > 0)
		{
			mListview.setItemChecked(mChkedSmsPos, true);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		/*
		switch (buttonView.getId())
		{
			case R.id.new_scene_all_contact_check_btn:
			{
				Log.d("scfw", "all_contact_check_btn:" + String.valueOf(isChecked));
				break;
			}
			case R.id.new_scene_all_unknown_call_check_btn:
			{
				Log.d("scfw", "all_unknown_call_check_btn:" + String.valueOf(isChecked));
				break;
			}
			default:
				break;
		}
		*/
		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{  
		
		menu.add(0, 1, 1,  R.string.Edit);
		menu.add(0, 2, 2, R.string.Delete);
		menu.add(0, 3, 3, R.string.Return);
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
				if (mListLongClickPos == 0)
            	{
					Toast.makeText(NewAdvanceSceneView.this, 
							R.string.CantEditSMS, Toast.LENGTH_SHORT).show();
            	}
				else
				{
					Intent intent = new Intent();
					intent.setClass(this, ExistReplySmsEditorView.class);
					Bundle bundle = new Bundle();
					bundle.putInt("REPLY_SMS_RECORD_ID", mListLongClickPos - 1);
					bundle.putInt("START_BY_ACTIVITY_ID", 0);
					intent.putExtras(bundle);
					startActivityForResult(intent, 2);
				}
            	
				return true; 
			}
			case 2:
			{	
				
				AlertDialog ad = new AlertDialog.Builder(this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.alert_dialog_two_buttons_title)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	if (mListLongClickPos != 0)
                    	{
                    		/*
                    		 * due to a 0 item can not delete and it isn't in
                    		 * the database ,so we must reduce 1 to get right 
                    		 * position of the reply sms 
                    		 */
                    		RemoveReplySms(mListLongClickPos - 1);
        				
                    		String[] smsAbStracts = GetSmsBodiesFromDb();
	         
                    		refreshList(smsAbStracts);
                    	}
                    	else
                    	{
                    		Toast.makeText(NewAdvanceSceneView.this, 
                    				R.string.CantDeleteSMS, Toast.LENGTH_SHORT).show();
                    	}
     
                    	
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked Cancel so do some stuff */
                    }
                })
                .create();
				
				ad.show();
				
			}
			case 3:
			default:    
				return super.onContextItemSelected(item);  
		}
	}
	
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	{
		// TODO Auto-generated method stub
		mListLongClickPos = position;
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	{
		mChkedSmsPos = position;
	}
	
	private void RemoveReplySms(int location)
	{
		String del_sms = PhoneNumberManager.getIntance(this).advGetAllSms()[location];
		PhoneNumberManager.getIntance(this).advDeleteSms(del_sms);
		
		if (mChkedSmsPos == mListLongClickPos)
		{
			mChkedSmsPos = 0;
		}
		else if (mChkedSmsPos > mListLongClickPos)
		{
			mChkedSmsPos--;
		}
	}
	
	
}
