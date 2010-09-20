package com.angelstone.android.smsblocker;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.angelstone.android.smsblocker.store.PhoneNumberManager;


public class SmsSceneMgrView extends Activity implements OnClickListener, 
                                                         OnItemLongClickListener,
                                                         OnItemClickListener
{
	private ListView mListview;
	private int mItemLongClickPos = 0;
	private int mItemClickPos = 0;
	private String mRelatedBlName = null;
	
	private Vector<String> mTermVector = new Vector<String>();
	
	public static EditText mBlEditView = null;
	
	private ProgressDialog mDialog;
	private Menu mMenu;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
		try
		{
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.sms_scene_mgr_view_layout);
	       
	        ImageButton addTermBtn = (ImageButton)this.findViewById(R.id.sms_scene_add_term_btn);
	        addTermBtn.setOnClickListener(this);
	        
	        GetRelatedBlName();       
	        
	        mListview = (ListView) findViewById(R.id.sms_scene_term_list);
	        
	        String[] terms = GetFilterTerms();
	   
	        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(terms.length);
			
	        for (int i = 0; i < terms.length; i++)
	        {
	        	Map<String, Object> map = new HashMap<String, Object>();	        	
	        	map.put("term",  terms[i]);
	        	list.add(map);
	        }
	        
	        refreshList(list);
	        mListview.setOnItemClickListener(this);
	        mListview.setOnItemLongClickListener(this);
	        
	        this.registerForContextMenu(mListview);
	        
	        mDialog = new ProgressDialog(SmsSceneMgrView.this);
	        
	        //mDialog.setTitle("Indeterminate");
    		mDialog.setMessage(this.getResources().getString(R.string.PleaseWait));
    		mDialog.setIndeterminate(true);
    		mDialog.setCancelable(false);
	       
	       
		}
		catch (Exception e)
		{
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}	
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.sms_scene_add_term_btn:
			{
				EditText et = (EditText)findViewById(R.id.sms_scene_filter_term_editor);
				
				if (isExist(String.valueOf(et.getText())))
				{
					Toast.makeText(this, 
				            R.string.KeywordExists, Toast.LENGTH_SHORT).show();
					return;
				}
				else if (String.valueOf(et.getText()).equals(""))
				{
					Toast.makeText(this, 
				            R.string.EmptyKeywordNotAllowed, Toast.LENGTH_SHORT).show();
					return;
				}
				
				mTermVector.add(String.valueOf(et.getText()));
				
				SaveNewFilterTerm(String.valueOf(et.getText()));
				
				String[] terms = GetFilterTerms();
				
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(terms.length);
				
		        for (int i = 0; i < terms.length; i++)
		        {
		        	Map<String, Object> map = new HashMap<String, Object>();	        	
		        	map.put("term",  terms[i]);
		        	list.add(map);
		        }
				
		        refreshList(list);
		        
				break;
			}
			default:
				break;
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
			long id)
	{
		mItemLongClickPos = position;
		return false;
	}
	
	
	private void refreshList(List<Map<String, Object>> termList)
	{
		
        
		SimpleAdapter adapter = new SimpleAdapter(this, 
				                                  termList, 
                								  R.layout.image_one_line_item_3,
                								  new String[] {"term"}, 
                								  new int[] {R.id.image_one_line_item_3_text});
                								
        mListview.setAdapter(adapter);
		
	}
	
	private boolean isExist(String term)
	{
		for (int i = 0; i < mTermVector.size(); i++)
		{
			if (mTermVector.elementAt(i).equals(term))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) 
	{
		menu.clear();
		
		MenuItem item = menu.add(0, 0, 0, R.string.ClearAll).setIcon(android.R.drawable.ic_menu_delete );
		
		mMenu = menu;
		
		String[] terms = GetFilterTerms();
		
		if (terms.length == 0)
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
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
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
							
								String[] terms = GetFilterTerms();
		        				
		        				for (int i = 0; i < terms.length; i++)
		        				{
		        					PhoneNumberManager.getIntance(SmsSceneMgrView.this).deleteSmsFilter(terms[i]); 
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
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
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
        		EditText et = (EditText)findViewById(R.id.sms_scene_filter_term_editor);
        		et.setText("");
        		mListview.setAdapter(null);
        		mListview.invalidateViews();
        	}
        	
        }

	};
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{  
		super.onCreateContextMenu(menu, v, menuInfo);    
		menu.add(0, 0, 0,  R.string.Delete);
		menu.add(0, 1, 0, R.string.Return);
		menu.setHeaderTitle(R.string.Menu);
	}
	
	public boolean onContextItemSelected(MenuItem item) 
	{  
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();  
		switch (item.getItemId()) 
		{  
			case 0:   
			{
				AlertDialog ad = new AlertDialog.Builder(this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.alert_dialog_two_buttons_title)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                    	Map<String, Object> listItem = 
        					(Map<String, Object>) mListview.getItemAtPosition(mItemLongClickPos);
        				
        				DelFilterTerm((String)listItem.get("term"));
        				
        				String[] terms = GetFilterTerms();
        				
        				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(terms.length);
        				
        		        for (int i = 0; i < terms.length; i++)
        		        {
        		        	Map<String, Object> map = new HashMap<String, Object>();	        	
        		        	map.put("term", terms[i]);
        		        	list.add(map);
        		        }
        		        
        		        refreshList(list);
        		        
        		        EditText et = (EditText)findViewById(R.id.sms_scene_filter_term_editor);
        				
        		        if (mItemLongClickPos == mItemClickPos)
        		        {
        		        	et.setText("");
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
				return true; 
			}
			case 1:
			default:    
				return super.onContextItemSelected(item);  
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	{
		// TODO Auto-generated method stub
		
		
		mListview.setSelection(position);
		mItemClickPos = position;
		
		EditText et = (EditText)findViewById(R.id.sms_scene_filter_term_editor);
		
		Map<String, Object> listItem = 
			(Map<String, Object>) mListview.getItemAtPosition(mItemClickPos);
		
		et.setText((String)listItem.get("term"));
	}
	
	@Override  
	protected void onActivityResult(int requestCode, int resultCode, Intent data)   
	{   	
		switch (resultCode)
		{
			case 1:
			{	
				mBlEditView.setText(data.getExtras().getString("add_bl_for_sm_result"));
				
		        mRelatedBlName = data.getExtras().getString("add_bl_for_sm_result");
		        
		        SetRelatedBlName(mRelatedBlName);
		        
				break;
			}
			default:
				break;
		}
		
	} 
	
	private void GetRelatedBlName()
	{
		mRelatedBlName = PhoneNumberManager.getIntance(this).getSmsBlacklist();
	}
	
	private void SetRelatedBlName(String blName)
	{
		PhoneNumberManager.getIntance(this).setSmsBlacklist(blName);
	}
	
	private void SaveNewFilterTerm(String term)
	{
		PhoneNumberManager.getIntance(this).addSmsFilter(term);
	}
	
	private String[] GetFilterTerms()
	{
		int cnt = PhoneNumberManager.getIntance(this).getSmsFilterCount();
		
		if (cnt == 0)
		{
			return new String[0];
		}
		else
		{
			String[] terms = new String[cnt];
			PhoneNumberManager.getIntance(this).getSmsFilters(terms);
				
			return terms;
		}
	}
	
	private void DelFilterTerm(String term)
	{
		PhoneNumberManager.getIntance(this).deleteSmsFilter(term);
	}
	
}
