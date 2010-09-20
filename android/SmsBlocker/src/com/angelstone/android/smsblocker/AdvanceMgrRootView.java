package com.angelstone.android.smsblocker;

import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.angelstone.android.smsblocker.store.PhoneNumberManager;

public class AdvanceMgrRootView extends Activity implements OnItemLongClickListener,
															OnItemClickListener,
															OnCheckedChangeListener,
															OnClickListener
{
	private static Vector<String> mSceneVector = new Vector<String>();
	private LinearLayout layout;
	private int mListLongClickPos = 0;
	private ListView mListview;
	
	private ProgressDialog mDialog;
	private Menu mMenu;
	
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private final int FC = ViewGroup.LayoutParams.FILL_PARENT;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.advance_mgr_root_view);
        
        String[] sceneNames = GetSceneNames(); 
        mListview = (ListView)this.findViewById(R.id.adv_root_mgr_exist_scene_list); 

        mListview.setItemsCanFocus(false);
        mListview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
           
        refreshList(sceneNames);
        
        registerForContextMenu(mListview);
        mListview.setOnItemLongClickListener(this);
        mListview.setOnItemClickListener(this);
        
        ImageButton addbuttion = (ImageButton)findViewById(R.id.add_new_adv_scene_btn);
        addbuttion.setOnClickListener(this);
        
        mDialog = new ProgressDialog(AdvanceMgrRootView.this);
        mDialog.setMessage(this.getResources().getText(R.string.PleaseWait));
		mDialog.setIndeterminate(true);
		mDialog.setCancelable(false);
        
        /*
        ToggleButton tbtn = (ToggleButton)findViewById(R.id.adv_root_mgr_advance_switch_btn);
        tbtn.setOnCheckedChangeListener(this);
        
        if (!AppMain.db.readSetting("advance_switch"))
        {
        	mListview.setVisibility(ListView.INVISIBLE);
			TextView tv = (TextView)findViewById(R.id.adv_root_mgr_exist_scene);
			tv.setVisibility(TextView.INVISIBLE);
			
			tbtn.setChecked(false);
        }
        else
        {
        	tbtn.setChecked(true);
        }
        */
	}

	@Override
	protected void onDestroy()
	{
		mSceneVector.clear();
		super.onDestroy();
	}
	
	public boolean onPrepareOptionsMenu(Menu menu) 
	{
		menu.clear();
		menu.add(0, 0, 0, R.string.ClearAllScenes).setIcon(this.getResources().getDrawable(R.drawable.ic_menu_delete));
		
		String[] sceneNames = GetSceneNames();
		
		mMenu = menu;
		
		if (sceneNames == null || sceneNames.length == 0)
		{
			menu.getItem(0).setEnabled(false);
		}
 
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
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() 
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    { 			
							Thread thread = new Thread()
                    		{				
								public void run()
								{
									Message msg = new Message();
									Bundle msgdata = new Bundle();
									msgdata.putBoolean("isRunning", true);
									msg.setData(msgdata);
									handler.sendMessage(msg);
									
									String[] sceneNames = GetSceneNames();
									
									if (sceneNames != null)
									{
										for (int i = 0; i < sceneNames.length; i++)
										{
											RemoveScene(i);
										}
									}
									
									Message msg_1 = new Message();
									msgdata = new Bundle();
									msgdata.putBoolean("isRunning", false);
									msg_1.setData(msgdata);
									handler.sendMessage(msg_1);
								}
                    		};                  		
                    		thread.start();						
					}               
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() 
                {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                        /* User clicked Cancel so do some stuff */
                    }
                })
                .create();				
				ad.show();
				
			    break;
			}
			default:
				break;
		}
		
		return true;
		
	}
    
	private Handler handler = new Handler() 
	{
		
		
        @Override
        public void handleMessage(Message msg) 
        {
        	
        	if (msg.getData().getBoolean("isRunning"))
        	{
        		mDialog.show();
        	}
        	else
        	{
        		mDialog.dismiss();
        		mMenu.getItem(0).setEnabled(false);
        		mListview.setAdapter(null);
        		
        		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(FC, FC);
        		ScrollView sv = (ScrollView)findViewById(R.id.adv_root_mgr_user_guide_text);
    	        sv.setLayoutParams(param);
        		
    	        TextView tv = (TextView)findViewById(R.id.adv_root_mgr_exist_scene);
    	        tv.setVisibility(TextView.INVISIBLE);
    	        
        		mListview.invalidateViews();
        	}
        	
        }

	};
    
	private String[] GetSceneNames()
	{
		return PhoneNumberManager.getIntance(this).advGetAllScenes();
		
	}
	
	private void refreshList(String[] names)
	{
		if (names != null)
		{
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					R.layout.simple_list_item_single_choice, names);
	        
	        mListview.setAdapter(adapter);
	        
	        CheckActiveScene();
	        
	        mListview.invalidateViews();
	        
	        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(0, 0);
	        ScrollView sv = (ScrollView)findViewById(R.id.adv_root_mgr_user_guide_text);
	        sv.setLayoutParams(param);
	        
	        TextView tv = (TextView)findViewById(R.id.adv_root_mgr_exist_scene);
	        tv.setVisibility(TextView.VISIBLE);
		}
		else
		{
			mListview.setAdapter(null);
			

			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(FC, FC);
			ScrollView sv = (ScrollView)findViewById(R.id.adv_root_mgr_user_guide_text);
		    sv.setLayoutParams(param);
		    
		    TextView tv = (TextView)findViewById(R.id.adv_root_mgr_exist_scene);
	        tv.setVisibility(TextView.INVISIBLE);
		}
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{  
		
		MenuItem editItem = menu.add(0, 1, 1,  R.string.Edit);
		MenuItem deleteItem = menu.add(0, 2, 2, R.string.Delete);
		MenuItem returnItem = menu.add(0, 3, 3, R.string.Return);
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
				Intent intent = new Intent();
				intent.setClass(this, ExistAdvanceSceneEditView.class);
				
				String name = GetLongClickSceneName(mListLongClickPos);
				
				Bundle bundle = new Bundle();
				bundle.putString("ADVANCE_SCENE_NAME", name);
				intent.putExtras(bundle);
				startActivityForResult(intent, 2);
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
                    	
                		RemoveScene(mListLongClickPos);
        				
        				String[] sceneNames = GetSceneNames();
        				refreshList(sceneNames);
        				
                    	
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data)   
	{   	
		switch (resultCode)
		{
			case 1:
			{	
				mSceneVector.add(data.getExtras().getString("NEW_SCENE_RETURN"));
				String[] sceneNames = GetSceneNames();							
				refreshList(sceneNames);
				break;
			}
			case 2:
			{				
				String[] sceneNames = GetSceneNames();								
				refreshList(sceneNames);
				break;
			}
			default:
				break;
		}
		
	}  
	
	private boolean isExist(String name)
	{
		for (int i = 0; i < mSceneVector.size(); i++)
		{
			if (mSceneVector.elementAt(i).equals(name))
			{
				return true;
			}
		}
		
		return false;
	}

	public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	{
		// TODO Auto-generated method stub
		try
		{
			String name = (String)mListview.getItemAtPosition(position);
			
			PhoneNumberManager.getIntance(this).advSetActiveScene(name);
			
			if (PhoneNumberManager.getIntance(this).readSetting("Firewall_switch") == true &&
				PhoneNumberManager.getIntance(this).readSetting("call_reject_switch") == true &&
				PhoneNumberManager.getIntance(this).readSetting("advance_switch") == true)
			{
				ActivityLog.logInfo(this,getString(R.string.LogCallBlock), getString(R.string.UseScript) + name);
				//silenceRejectCall.unSilenceContacts(this);
				//silenceRejectCall.silenceContacts(this);
			}
		}
		catch (Exception e)
		{
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
	}
	
	public void RemoveScene(int selPosition)
	{		
		try
		{
			PhoneNumberManager.getIntance(this).advDeleteScene((String)mListview.getItemAtPosition(selPosition));
			
			String activeSceneName = PhoneNumberManager.getIntance(this).advGetActiveScene();
			
			if (activeSceneName.equals((String)mListview.getItemAtPosition(selPosition)))
			{
				PhoneNumberManager.getIntance(this).advSetActiveScene(null);
				/*if (AppMain.db.readSetting("Firewall_switch") == true &&
					AppMain.db.readSetting("call_reject_switch") == true &&
					AppMain.db.readSetting("advance_switch") == true)
						silenceRejectCall.unSilenceContacts(this);
				*/
			}
		}
		catch (Exception e)
		{
			Log.d("scfw", e.getClass().toString());
		}
		
	}
	
	private String GetLongClickSceneName(int position)
	{
		try
		{
			return GetSceneNames()[position];
		}
		catch (Exception e)
		{
			Log.d("scfw", e.getClass().toString());
		}
		
		
		return null;
	}
	
	private void CheckActiveScene()
	{
		String activeScene = PhoneNumberManager.getIntance(this).advGetActiveScene();
		
		if (activeScene != null)
		{
			for (int i = 0; i < mListview.getCount(); i++)
			{	
				String itemText = (String)mListview.getItemAtPosition(i);
				
				if (itemText.equals(activeScene))
				{
					mListview.setItemChecked(i, true);
				}

			}
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		/*
		switch (buttonView.getId())
		{
			case R.id.adv_root_mgr_advance_switch_btn:
			{
				if (!isChecked)
				{
					mListview.setVisibility(ListView.INVISIBLE);
					TextView tv = (TextView)findViewById(R.id.adv_root_mgr_exist_scene);
					tv.setVisibility(TextView.INVISIBLE);
					
					AppMain.db.writeSetting("advance_switch", false);
					
					if (CallFwService.db.readSetting("Firewall_switch") == true &&
						CallFwService.db.readSetting("call_reject_switch") == true)
					{
						silenceRejectCall.unSilenceContacts(this);
					}
					
				}
				else
				{
					mListview.setVisibility(ListView.VISIBLE);
					TextView tv = (TextView)findViewById(R.id.adv_root_mgr_exist_scene);
					tv.setVisibility(TextView.VISIBLE);
					
					AppMain.db.writeSetting("advance_switch", true);
					
					if (CallFwService.db.readSetting("Firewall_switch") == true &&
						CallFwService.db.readSetting("call_reject_switch") == true)
					{
						silenceRejectCall.silenceContacts(this);
					}
				}
				Log.d("scfw", "adv_root_mgr_advance_switch_btn:" + String.valueOf(isChecked));
				break;
			}
			default:
				break;
		}
		*/
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.add_new_adv_scene_btn:
			{
				Intent intent = new Intent();
				intent.setClass(this, NewAdvanceSceneView.class);
				startActivityForResult(intent, 1); 
				break;
			}
			default:
				break;
		}
		
	}
}
