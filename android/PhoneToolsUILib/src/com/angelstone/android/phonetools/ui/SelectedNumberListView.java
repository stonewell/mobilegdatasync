package com.angelstone.android.phonetools.ui;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.angelstone.android.utils.PhoneNumberHelpers;
import com.angelstone.android.utils.ToastShowWaitHandler;

public class SelectedNumberListView extends Activity implements OnClickListener													
{
	private List<Map<String, String>> mList = new ArrayList<Map<String, String>>();
	
	ArrayList<String> mDeleteNumbers = new ArrayList<String>();
	ArrayList<String> mDeleteNames = new ArrayList<String>();
	
	private ToastShowWaitHandler toastShowWaitHandler = new ToastShowWaitHandler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        
        // See assets/res/any/layout/dialog_activity.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.selected_numbers_list_view_layout);
         
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, 
                android.R.drawable.ic_menu_more);
        
        
        Bundle bundle = this.getIntent().getExtras();
        int count = bundle.getInt("number_count");
        String[] numbers = bundle.getStringArray("selected_numbers");
        String[] names = bundle.getStringArray("selected_names");
        
        String title = this.getResources().getString(R.string.selectedNumbersTitle)
                       + " "
                       + count;
        
        this.setTitle(title);
        
        
        
        for (int i = 0; i < numbers.length; i++)
		{		
			Map<String, String> map = new HashMap<String, String>();
			
			map.put("number", PhoneNumberUtils.formatNumber(numbers[i]));
			map.put("name", names[i]);
			
			mList.add(map);
		}
        
        ListView listView = (ListView)this.findViewById(R.id.selected_number_list);
		
        SimpleAdapter adapter = null;
        
       if (count > 0)
       {
        
        	adapter= new SimpleAdapter(this, 
	                                   mList,
 									   R.layout.selected_numbers_list_item_layout,
							      	   new String[] {"number", "name"},
							      	   new int[] {R.id.selected_number, R.id.selected_name});
        }

		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
        Button button1 = (Button)findViewById(R.id.selected_number_list_view_delete_btn);	
        button1.setOnClickListener(this);
		Button button2 = (Button)findViewById(R.id.selected_number_list_view_return_btn);	
		button2.setOnClickListener(this);
		
		if (count == 0)
		{
			button1.setEnabled(false);
		}
    }

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.selected_number_list_view_delete_btn:
			{ 	
				
				mDeleteNumbers = new ArrayList<String>();
				mDeleteNames = new ArrayList<String>();
            	ListView listView = (ListView)findViewById(R.id.selected_number_list);
            	for (int i = 0; i < listView.getCount(); i++)
            	{
            		if (listView.isItemChecked(i))
            		{
            			Map<String, String> item = (Map<String, String>)mList.get(i);
            			mDeleteNumbers.add(PhoneNumberHelpers.removeNonNumbericChar(item.get("number")));
            			mDeleteNames.add(item.get("name"));
            			
            		}
            	}
            	
            	if (mDeleteNumbers.size() == 0)
            	{
            		if (toastShowWaitHandler.IsAllowShow())
					{
						Toast.makeText(this, R.string.PleaseSelectOneNumberAtLeast, Toast.LENGTH_SHORT).show();	
						
 					}
            		return;
            	}
				
				AlertDialog ad = new AlertDialog.Builder(this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.delete_confirm)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {     
                    	
                    	String[] numbers = (String[])mDeleteNumbers.toArray(new String[mDeleteNumbers.size()]);
                    	String[] names = (String[])mDeleteNames.toArray(new String[mDeleteNames.size()]);
        				
        				Intent intent = new Intent(); 
        			    intent.putExtra("delete_numbers", numbers);
        			    intent.putExtra("delete_names", names);
        			    
        			    setResult(2, intent);
        			    
        			    finish();

                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) 
                    {
                       
                    }
                })
                .create();				
				ad.show();
				break;
			}
			case R.id.selected_number_list_view_return_btn:
			{
				finish();
				break;
			}
			default:
				break;
		}
		
	}

}
