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
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.angelstone.android.smsblocker.store.PhoneNumberManager;
import com.angelstone.android.utils.PhoneNumberHelpers;

public class BlNumberSearchView extends Activity implements OnItemLongClickListener
{
	private ListView mListview;
	private List<Map<String, Object>> numlist;
	private String[][] mBlNumbers = null;
	private int mListLongClickPos;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.bl_number_search_view_layout);
	
			mListview = (ListView)findViewById(R.id.bl_number_search_number_list);
	        registerForContextMenu(mListview);
	        
	        numlist = new ArrayList<Map<String, Object>>();
	        
	        mBlNumbers = PhoneNumberManager.getIntance(this).getBlacklistNumbers();
	        
	        if (mBlNumbers != null)
	        {
		        for (int i = 0; i < mBlNumbers.length; i++)
		        {
		        	addNumToView(mBlNumbers[i][0], 
		        			     mBlNumbers[i][1].equals("1"),
		        			     mBlNumbers[i][2].equals("1"), 
		        			     mBlNumbers[i][3],
		        			     String.valueOf(i));
		        }
	        }
	        refreshViewList();
	        
	        registerForContextMenu(mListview);
	        mListview.setOnItemLongClickListener(this);
	        
	        EditText et = (EditText)findViewById(R.id.bl_number_search_editor);
	        et.addTextChangedListener(watcher);  
		}
		catch (Exception e)
		{
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
	}
	
	private TextWatcher watcher = new TextWatcher(){   
		  
        @Override  
        public void afterTextChanged(Editable s) {   
            // TODO Auto-generated method stub   
               
        }   
  
        @Override  
        public void beforeTextChanged(CharSequence s, int start, int count,   
                int after) {   
            // TODO Auto-generated method stub 
               
        }   
  
        @Override  
        public void onTextChanged(CharSequence s, int start, int before,   
                int count) { 
        	
            Log.d("TAG","[TextWatcher][onTextChanged]"+s);  
            
            try
            {
            	searchAndRefreshList(s);
            }
            catch (Exception e)
            {
            	Log.d("scfw", this.toString() + ":" + e.getClass().toString());
            }
               
        }   
           
    };  
    
    public void searchAndRefreshList(CharSequence s)
    {
    	numlist.clear();
    	
    	String source = String.valueOf(s).replace(" ", "");
    	
        for (int i = 0; i < mBlNumbers.length; i++)
        {
        	
        	
        	if (mBlNumbers[i][3].replace(" ", "").toLowerCase().indexOf(source.toLowerCase(), 0) == 0
        		|| mBlNumbers[i][0].replace(" ", "").toLowerCase().indexOf(source.toLowerCase(), 0) == 0)
        	{
        		addNumToView(mBlNumbers[i][0], 
        				     mBlNumbers[i][1].equals("1"),
        				     mBlNumbers[i][2].equals("1"), 
        				     mBlNumbers[i][3],
        				     String.valueOf(i));
        	}
        }	         
        refreshViewList();
    }
    	
	private void addNumToView(String num, boolean blockCall, boolean blockSms, String tag, String positon)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		
		num = PhoneNumberUtils.formatNumber(num);
		
		map.put("number", num);
		map.put("tag", tag);
		if (blockCall)
			map.put("cbimg", R.drawable.rejectcall);
		else
			map.put("cbimg", R.drawable.empty);
		
		if (blockSms)
			map.put("sbimg", R.drawable.sms_reject);
		else
			map.put("sbimg", R.drawable.empty);
		
		map.put("position", positon);
		numlist.add(map);
	}
	
	private void refreshViewList()
	{
		SimpleAdapter adapter = new SimpleAdapter(this, 
				numlist,
				R.layout.black_list_item_layout, 
				new String[] {"number", "tag", "cbimg", "sbimg"},
				new int[] {R.id.black_list_item_text, 
						   R.id.black_list_item_text1,
				           R.id.black_list_item_cb_img, 
				           R.id.black_list_item_sb_img});
		
		mListview.setAdapter(adapter);
		mListview.invalidateViews();
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
		Map<String, String> map = (Map<String, String>)mListview.getItemAtPosition(mListLongClickPos);
		String number = map.get("number");
		String unFormatNumber = PhoneNumberHelpers.removeNonNumbericChar(map.get("number"));
		
		String posStr = map.get("position");
		String replySms = map.get("replySms");
		
		boolean isCallBlock = PhoneNumberManager.getIntance(this).isBlBlockCall(unFormatNumber);
		boolean isSmsBlock = PhoneNumberManager.getIntance(this).isBlBlockSms(unFormatNumber);
		
		String tag = map.get("tag");
		
		switch (item.getItemId()) 
		{  
			case 1:    
			{
				
				
				Intent intent = new Intent(); 
				
			    intent.putExtra("search_number", number);
				intent.putExtra("search_callblock", isCallBlock);
				intent.putExtra("search_smsblock", isSmsBlock);
				intent.putExtra("search_tag", tag);
				//intent.putExtra("search_reply_sms", replySms);
				intent.putExtra("search_position", posStr);
			    setResult(3, intent);
			    
			    EditText et = (EditText)findViewById(R.id.bl_number_search_editor);
			    ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			   
			    finish();
			    
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
                    	Map<String, String> map = (Map<String, String>)mListview.getItemAtPosition(mListLongClickPos);
                		String number = map.get("number");
                    	Intent intent = new Intent(); 
        			    intent.putExtra("search_number", number);
        				
        			    setResult(4, intent);
        			    
        			    EditText et = (EditText)findViewById(R.id.bl_number_search_editor);
        			    ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        			    
        			    finish();
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
		mListLongClickPos = position;
		return false;
	}
	
	


}
