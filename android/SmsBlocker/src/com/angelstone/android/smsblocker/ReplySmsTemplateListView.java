package com.angelstone.android.smsblocker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.angelstone.android.smsblocker.store.PhoneNumberManager;

public class ReplySmsTemplateListView extends Activity implements OnItemClickListener,
																  OnClickListener,
																  OnItemLongClickListener
{
	private ListView mListview;
	private int mListLongClickPos = -1;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.reply_sms_template_list_view_layout);
	        
			String[] smsBodies = GetSmsBodiesFromDb();
			mListview = (ListView) findViewById(R.id.reply_sms_tmpl_list);
			
			refreshList(smsBodies);
			
			mListview.setOnItemClickListener(this);
			mListview.setOnItemLongClickListener(this);
			registerForContextMenu(mListview);
			
	        Button btn = (Button)findViewById(R.id.reply_sms_tmpl_add_new_reply_sms_btn);
	        btn.setOnClickListener(this);
		}
		catch (Exception e)
		{
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	{
		String[] allSmsBodies = GetSmsBodiesFromDb();
		
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("SMS_SELECT_RETURN", allSmsBodies[position]);
		intent.putExtras(bundle);
		setResult(2, intent);
		
		finish();
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.reply_sms_tmpl_add_new_reply_sms_btn:
			{
				Intent intent = new Intent();
				intent.setClass(this, NewReplySmsEditorView.class);
				startActivityForResult(intent, 1);
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
	
	private String[] GetSmsBodiesFromDb()
	{	
		
		return PhoneNumberManager.getIntance(this).advGetAllSms();
	}
	
	private void refreshList(String[] texts)
	{	
		if (texts != null)
		{
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(1);
			
			for (int i = 0; i < texts.length; i++)
			{
				Map<String, Object> root = new HashMap<String, Object>();
				root.put("sms", texts[i]);
				list.add(root);
			}
			
			
			SimpleAdapter adapter = new SimpleAdapter(this, 
										list,
										R.layout.image_one_line_item_5, 
										new String[] {"sms"},
										new int[] {R.id.image_one_line_item_5_text});
			
			mListview.setAdapter(adapter);
		}
		else
		{
			mListview.setAdapter(null);
		}
		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{  
		
		menu.add(0, 1, 1, R.string.Edit);
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
				Intent intent = new Intent();
				intent.setClass(this, ExistReplySmsEditorView.class);
				Bundle bundle = new Bundle();
				bundle.putInt("REPLY_SMS_RECORD_ID", mListLongClickPos);
				bundle.putInt("START_BY_ACTIVITY_ID", 0);
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
                    	
                		RemoveReplySms(mListLongClickPos);
        				
        				String[] smsAbStracts = GetSmsBodiesFromDb();
	         
	        			refreshList(smsAbStracts);
     
                    	
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
	
	private void RemoveReplySms(int location)
	{
		String del_sms = PhoneNumberManager.getIntance(this).advGetAllSms()[location];
		PhoneNumberManager.getIntance(this).advDeleteSms(del_sms);
	}
}
