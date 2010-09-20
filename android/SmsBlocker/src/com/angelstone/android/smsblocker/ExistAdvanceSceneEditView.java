package com.angelstone.android.smsblocker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.angelstone.android.smsblocker.store.AdvScene;
import com.angelstone.android.smsblocker.store.PhoneNumberManager;
import com.angelstone.android.utils.ToastShowWaitHandler;

public class ExistAdvanceSceneEditView extends Activity implements OnClickListener,
																   OnCheckedChangeListener
																  
{
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private final int FC = ViewGroup.LayoutParams.FILL_PARENT;
	
	private ListView mListview;
	private int mListLongClickPos;
	private int mChkedSmsPos = -1;
	
	private String mOldSceneName = new String();
	private boolean block_contacts = false;
	private boolean block_unknown = false;
	
	private ToastShowWaitHandler toastShowWaitHandler = new ToastShowWaitHandler();
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
		try
		{
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.advance_scene_edit_view_layout);
	        
	        Bundle bundle = this.getIntent().getExtras();
	        mOldSceneName = bundle.getString("ADVANCE_SCENE_NAME");
	        
	        EditText et = (EditText)findViewById(R.id.advance_scene_edit_name_editor);
	        et.setText(mOldSceneName);
	        
	        mListview = (ListView) findViewById(R.id.advance_scene_reply_sms_list);
	        mListview.setItemsCanFocus(false);
	        mListview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	        
	        AdvScene scene_info = new AdvScene();
	        
	        PhoneNumberManager.getIntance(this).advGetSceneInfo( mOldSceneName, scene_info );
	        
	      //String[] replySms = new String[1];
	        if (savedInstanceState == null)
	        {  
	        	refreshList(scene_info.m_Sms);
	        }
	        else
	        {
	        	String replySms = savedInstanceState.getString("edited_sms_body"); 
	        	refreshList(replySms);
	        }
	              	     	
	        registerForContextMenu(mListview);
	        
	        
	        
	        DisplayMetrics dm = new DisplayMetrics(); 
	        dm = this.getResources().getDisplayMetrics(); 
	        int screenWidth = dm.widthPixels; 
	        int screenHeight = dm.heightPixels; 
	        
	        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(screenWidth / 2 - 10, WC);
	        
	        block_contacts = (scene_info.m_Target & AdvScene.TARGET_GROUP_CONTACTS) != 0;
	        
	        
	        
	        CheckBox cb = (CheckBox)findViewById(R.id.advance_scene_edit_all_contact_check_box);
	        cb.setChecked(block_contacts);
	        cb.setLayoutParams(param);
	        
	        /*
	        ToggleButton tbtn = (ToggleButton)findViewById(R.id.all_contact_check_btn);
	        tbtn.setOnCheckedChangeListener(this);
	        tbtn.setChecked(block_contacts);
	        */
	       
	        block_unknown = (scene_info.m_Target & AdvScene.TARGET_GROUP_UNKNOWN) != 0;
	        
	        cb = (CheckBox)findViewById(R.id.advance_scene_edit_all_unknown_call_check_box);
	        cb.setChecked(block_unknown);
	        cb.setLayoutParams(param);
	        
	        /*
	        tbtn = (ToggleButton)findViewById(R.id.all_unknown_call_check_btn);
	        tbtn.setOnCheckedChangeListener(this);
	        tbtn.setChecked(block_unknown);
	        */
	        
	        ImageButton btn = (ImageButton)findViewById(R.id.advance_scene_edit_modify_reply_sms_btn);
	        btn.setOnClickListener(this);
	        
	        btn = (ImageButton)findViewById(R.id.advance_scene_edit_import_reply_sms_btn);
	        btn.setOnClickListener(this);
	        
	        Button btn1 = (Button)findViewById(R.id.advance_scene_edit_ok_btn);
	        btn1.setOnClickListener(this);
	        btn1.setLayoutParams(param);
	        
	        btn1 = (Button)findViewById(R.id.advance_scene_edit_cancel_btn);
	        btn1.setOnClickListener(this);
	        btn1.setLayoutParams(param);

		}
		catch (Exception e)
		{
			Log.d("scfw", e.getClass().toString());
		}
		
		
	}
	
	@Override 
	protected void onSaveInstanceState(Bundle outState) 
	{ 
		super.onSaveInstanceState(outState);
		
		String editedSms = (String)((Map<String, Object>)mListview.getItemAtPosition(0)).get("sms");
		
		outState.putString("edited_sms_body", editedSms);
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
			case R.id.advance_scene_edit_modify_reply_sms_btn:
			{
				Map<String, Object> item = (Map<String, Object>)mListview.getAdapter().getItem(0);
				
				Intent intent = new Intent();
				intent.setClass(this, ActiveReplySmsEditorView.class);
				Bundle bundle = new Bundle();
				bundle.putString("CURRENT_SMS_CONTENT", (String)item.get("sms"));
				intent.putExtras(bundle);
				startActivityForResult(intent, 1);
				break;
			}
			case R.id.advance_scene_edit_import_reply_sms_btn:
			{
				
				Intent intent = new Intent();
				intent.setClass(this, ReplySmsTemplateListView.class);
				startActivityForResult(intent, 2);
				break;
			}
			case R.id.advance_scene_edit_ok_btn:
			{
				try 
				{
					
					EditText et = (EditText)findViewById(R.id.advance_scene_edit_name_editor);
					Intent intent = new Intent(); 
					
					if (String.valueOf(et.getText()).trim().equals(""))
					{
						if (toastShowWaitHandler.IsAllowShow())
						{
							Toast.makeText(this, 
								R.string.EmptyNumberIsNotAllowed, Toast.LENGTH_SHORT).show();
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
					
					//ToggleButton button1 = (ToggleButton)findViewById(R.id.all_contact_check_btn);
					//ToggleButton button2 = (ToggleButton)findViewById(R.id.all_unknown_call_check_btn);
					CheckBox cb1 = (CheckBox)findViewById(R.id.advance_scene_edit_all_contact_check_box);   
					CheckBox cb2 = (CheckBox)findViewById(R.id.advance_scene_edit_all_unknown_call_check_box);
			    
					
					if (cb1.isChecked() == false && cb2.isChecked() == false)
					{
						if (toastShowWaitHandler.IsAllowShow())
						{
							Toast.makeText(this, R.string.PleaseChooseContactOrUnknown, Toast.LENGTH_SHORT).show();		
						}
						return;
					}						 
					
					String scenename = String.valueOf(et.getText());
					SaveNewScene(scenename);
					
					if (!this.mOldSceneName.equals(scenename))
					{
						String chgString = this.mOldSceneName + " " + getString(R.string.ChangeTo) + " " + scenename;
						
						ActivityLog.logInfo(this,getString(R.string.LogCallBlock), getString(R.string.ModifyScript) + chgString);
					}
					
					/*
					String actScene = AppMain.db.advGetActiveScene();
					if (actScene != null && scenename.equals(actScene) &&
						AppMain.db.readSetting("Firewall_switch") == true &&
						AppMain.db.readSetting("call_reject_switch") == true &&
						AppMain.db.readSetting("advance_switch") == true)
					{
						silenceRejectCall.unSilenceContacts(this);
						silenceRejectCall.silenceContacts(this);
					} */		   
					setResult(2, intent);
					
					((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					
				    finish();  	    
				}
				catch (Exception e)
				{
					Log.d("scfw", e.getClass().toString());
				}
				
				break;
			}
			case R.id.advance_scene_edit_cancel_btn:
			{
				EditText et = (EditText)findViewById(R.id.advance_scene_edit_name_editor);
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
		        refreshList(data.getExtras().getString("ACTIVE_SMS_MODIFY_RETURN"));  
		        break;
			}
			case 2:
			{	
		        refreshList(data.getExtras().getString("SMS_SELECT_RETURN"));
		        break;
			}
			default:
				break;
		}
	}  
	
	private void refreshList(String text)
	{	
		if (text == null)
		{
			text = new String();
		}
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(1);
		
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("sms", text);
		list.add(root);
		
		
		SimpleAdapter adapter = new SimpleAdapter(this, 
						list,
						R.layout.image_one_line_item_4, 
						new String[] {"sms"},
						new int[] {R.id.image_one_line_item_4_text});
		
        mListview.setAdapter(adapter); 
		
		
	}
	
	private boolean isSceneExist(String sceneName)
	{
		if (mOldSceneName.equals(sceneName))
		{
			return false;
		}
		
		
		String[] names = PhoneNumberManager.getIntance(this).advGetAllScenes();;
		
		if (names != null)
		{
			for (int i = 0; i < names.length; i++)
			{
				if (names[i].equals(sceneName))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	private int SaveNewScene(String newSceneName)
	{
		
		Log.d("scfw", "SaveNewScene");
		
		Log.d("scfw", "SceneName: " + newSceneName);
		
		Map<String, Object> item = (Map<String, Object>)mListview.getAdapter().getItem(0);
		
		String sms_str = (String)item.get("sms");
		
		int target_group = 0;
		
		//ToggleButton tbtn = (ToggleButton)findViewById(R.id.all_contact_check_btn);
		CheckBox cb = (CheckBox)findViewById(R.id.advance_scene_edit_all_contact_check_box);
		
        if (cb.isChecked())
        {
        	target_group |= AdvScene.TARGET_GROUP_CONTACTS;
        }
        else
        {
        	target_group &= ~AdvScene.TARGET_GROUP_CONTACTS;
        }
        
        //tbtn = (ToggleButton)findViewById(R.id.all_unknown_call_check_btn);
        cb = (CheckBox)findViewById(R.id.advance_scene_edit_all_unknown_call_check_box);
        
        if (cb.isChecked())
        {        
        	target_group |= AdvScene.TARGET_GROUP_UNKNOWN;
        }
        else
        {   
        	target_group &= ~AdvScene.TARGET_GROUP_UNKNOWN;;
        }

        PhoneNumberManager.getIntance(this).advUpdateScene( mOldSceneName, newSceneName, target_group, 0, sms_str);
        
        if (IsActiveScene(mOldSceneName))
        {
        	PhoneNumberManager.getIntance(this).advSetActiveScene(newSceneName);
        	/*if (block_contacts != ((target_group & AdvScene.TARGET_GROUP_CONTACTS) != 0) ||
        		block_unknown != ((target_group & AdvScene.TARGET_GROUP_UNKNOWN) != 0))
        	{
				silenceRejectCall.unSilenceContacts(this);
				silenceRejectCall.silenceContacts(this);        		
        	}*/
        }
        
        Log.d("scfw", "advUpdateScene");
		return 1;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		// TODO Auto-generated method stub
		/*
		switch (buttonView.getId())
		{
			case R.id.all_contact_check_btn:
			{
				Log.d("scfw", "all_contact_check_btn:" + String.valueOf(isChecked));
				break;
			}
			case R.id.all_unknown_call_check_btn:
			{
				Log.d("scfw", "all_unknown_call_check_btn:" + String.valueOf(isChecked));
				break;
			}
			default:
				break;
		}
		*/
		
	}
	
	private boolean IsActiveScene(String oldSceneName)
	{
		String activeSceneName = PhoneNumberManager.getIntance(this).advGetActiveScene();
		
		if (activeSceneName == null)
		{
			return false;
		}	
		else if (oldSceneName.equals(activeSceneName))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}

