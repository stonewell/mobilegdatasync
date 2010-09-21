package com.angelstone.android.smsblocker.ui;

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
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.angelstone.android.smsblocker.R;
import com.angelstone.android.smsblocker.store.PhoneNumberManager;
import com.angelstone.android.utils.ActivityLog;
import com.angelstone.android.utils.PhoneNumberHelpers;

public class BlackListManageRootView extends Activity implements OnClickListener,
		OnItemLongClickListener, OnItemClickListener {
	public static List<Map<String, Object>> numlist;
	private int mListLongClickPos = 0;
	private ListView mListview;
	private Button mAddbuttion;

	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private final int FC = ViewGroup.LayoutParams.FILL_PARENT;
	public static final int CLEAR_BLACK_LIST_NUMBER = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.bl_mgr_root_view);

			CheckBox chkBox = (CheckBox) findViewById(R.id.sms_allow_contacts_check_box);
			chkBox.setOnClickListener(this);
			chkBox.setChecked(PhoneNumberManager.getIntance(this).readSetting(
					PhoneNumberManager.OPTION_ALLOW_CONTACTS));

			mAddbuttion = (Button) findViewById(R.id.add_number_btn);
			mAddbuttion.setOnClickListener(this);

			mListview = (ListView) this.findViewById(R.id.exist_mgr_bl_list);
			registerForContextMenu(mListview);
			mListview.setOnItemLongClickListener(this);
			mListview.setOnItemClickListener(this);

			numlist = new ArrayList<Map<String, Object>>();

			String[][] blNumbers = PhoneNumberManager.getIntance(this)
					.getBlacklistNumbers();
			if (blNumbers != null) {
				for (int i = 0; i < blNumbers.length; i++) {
					addNumToView(blNumbers[i][0], blNumbers[i][1].equals("1"),
							blNumbers[i][2].equals("1"), blNumbers[i][3]);
				}
			}
			refreshViewList();
		} catch (Exception e) {
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}

	}

	public void addNumToView(String num, boolean blockCall, boolean blockSms,
			String name) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();

			num = PhoneNumberUtils.formatNumber(num);

			map.put("number", num);
			map.put("name", name);

			if (blockCall)
				map.put("cbimg", R.drawable.rejectcall);
			else
				map.put("cbimg", R.drawable.empty);

			if (blockSms)
				map.put("sbimg", R.drawable.sms_reject);
			else
				map.put("sbimg", R.drawable.empty);

			numlist.add(map);
		} catch (Exception e) {
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
	}

	private void refreshViewList() {
		SimpleAdapter adapter = new SimpleAdapter(this, BlackListManageRootView.numlist,
				R.layout.black_list_item_layout, new String[] { "number",
						"name", "cbimg", "sbimg" }, new int[] {
						R.id.black_list_item_text, R.id.black_list_item_text1,
						R.id.black_list_item_cb_img,
						R.id.black_list_item_sb_img });

		if (numlist.size() == 0) {
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(FC,
					FC);
			ScrollView sv = (ScrollView) findViewById(R.id.bl_root_mgr_user_guide_text);
			sv.setLayoutParams(param);

			// TextView tv = (TextView)findViewById(R.id.blName);
			// tv.setVisibility(TextView.INVISIBLE);

			LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(0,
					0);
			TextView tv = (TextView) findViewById(R.id.add_bl_number_edit_guide_text);
			tv.setLayoutParams(param1);

		} else {
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(0,
					0);
			ScrollView sv = (ScrollView) findViewById(R.id.bl_root_mgr_user_guide_text);
			sv.setLayoutParams(param);

			// TextView tv = (TextView)findViewById(R.id.blName);
			// tv.setVisibility(TextView.VISIBLE);

			LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(
					WC, WC);
			param1.gravity = Gravity.CENTER_HORIZONTAL;
			TextView tv = (TextView) findViewById(R.id.add_bl_number_edit_guide_text);
			tv.setLayoutParams(param1);
		}

		mListview.setAdapter(adapter);

		mListview.invalidateViews();

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (numlist.size() == 0) {
			String[][] blNumbers = PhoneNumberManager.getIntance(this)
					.getBlacklistNumbers();
			if (blNumbers != null) {
				for (int i = 0; i < blNumbers.length; i++) {
					addNumToView(blNumbers[i][0], blNumbers[i][1].equals("1"),
							blNumbers[i][2].equals("1"), blNumbers[i][3]);
				}
			}
			refreshViewList();
		}

		CheckBox chkBox = (CheckBox) findViewById(R.id.sms_allow_contacts_check_box);
		chkBox.setChecked(PhoneNumberManager.getIntance(this).readSetting(
				PhoneNumberManager.OPTION_ALLOW_CONTACTS));
	}

	@Override
	protected void onDestroy() {
		numlist.clear();

		super.onDestroy();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add_number_btn: {
			Intent intent = new Intent();
			intent.setClass(this, AddBlackListNumberView.class);
			startActivityForResult(intent, 1);
			break;
		}

		case R.id.sms_allow_contacts_check_box: {
			CheckBox chkBox = (CheckBox) findViewById(R.id.sms_allow_contacts_check_box);

			PhoneNumberManager.getIntance(this).writeSetting(
					PhoneNumberManager.OPTION_ALLOW_CONTACTS,
					chkBox.isChecked());
			break;
		}
		default:
			break;
		}
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		menu.add(0, 1, 1, R.string.Edit);
		menu.add(0, 2, 2, R.string.Delete);
		menu.setHeaderTitle(R.string.Menu);

		super.onCreateContextMenu(menu, v, menuInfo);

	}

	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1: {
			@SuppressWarnings("unchecked")
			Map<String, String> map = (Map<String, String>) mListview
					.getItemAtPosition(mListLongClickPos);
			String number = map.get("number");
			String unFormatNumber = PhoneNumberHelpers
					.removeNonNumbericChar(map.get("number"));
			String name = map.get("name");
			// String replySms = map.get("relpySms");

			if (number != null) {
				boolean blockCall = PhoneNumberManager.getIntance(this)
						.isBlBlockCall(unFormatNumber);
				boolean blockSms = PhoneNumberManager.getIntance(this)
						.isBlBlockSms(unFormatNumber);

				Intent intent = new Intent();
				intent.setClass(BlackListManageRootView.this, EditBlackListNumberView.class);

				Bundle bundle = new Bundle();
				bundle.putInt("POSITION", mListLongClickPos);
				bundle.putString("EDIT_NUMBER", number);
				bundle.putString("EDIT_NAME", name);
				bundle.putBoolean("block_call", blockCall);
				bundle.putBoolean("block_sms", blockSms);
				// bundle.putString("reply_sms", replySms);
				intent.putExtras(bundle);
				startActivityForResult(intent, 2);
			} else {
				Log.d("scfw", "blName is null");
			}

			break;
		}
		case 2: {
			AlertDialog ad = new AlertDialog.Builder(this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle(R.string.alert_dialog_two_buttons_title)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									RemoveNumber(mListLongClickPos);
									numlist.remove(mListLongClickPos);

									refreshViewList_4();

								}
							})
					.setNegativeButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* User clicked Cancel so do some stuff */
								}
							}).create();
			ad.show();

			break;

		}
		case 4:
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		mListLongClickPos = position;
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case 1: // add result
		{
			String[] addedNumbers = data.getExtras().getStringArray(
					"added_numbers_result");
			String[] addedNames = data.getExtras().getStringArray(
					"added_names_result");
			// String addedReplySms =
			// data.getExtras().getString("added_reply_sms_result");
			boolean blockCall = data.getExtras().getBoolean("call_block");
			boolean blockSms = data.getExtras().getBoolean("sms_block");
			boolean isRemoveFromContact = data.getExtras().getBoolean(
					"is_remove_from_contact");

			// //phone_id is useless, always set it to 0 is OK
			for (int i = 0; i < addedNumbers.length; i++) {
				if (PhoneNumberManager.getIntance(this).blacklistAddNumber(
						addedNumbers[i], blockCall, blockSms, addedNames[i]) == PhoneNumberManager.INSERT_ERROR_AREADY_EXIST) {
					continue;
				}

				addNumToView(addedNumbers[i], blockCall, blockSms,
						addedNames[i]);

				if (isRemoveFromContact
						&& PhoneNumberManager.getIntance(this).isContact(
								addedNumbers[i])) {
					PhoneNumberManager.getIntance(this).removeFromContact(
							addedNumbers[i]);
				}
			}

			refreshViewList();
			break;
		}
		case 2: // edit result
		{
			String editNum = data.getExtras().getString("edited_number_return");
			boolean blockCall = data.getExtras().getBoolean("call_block");
			boolean blockSms = data.getExtras().getBoolean("sms_block");
			String name = data.getExtras().getString("edited_name_return");
			// String replySms =
			// data.getExtras().getString("edited_reply_sms_return");

			UpdateList(data.getExtras().getInt("position"), blockCall,
					blockSms, name);

			editNum = PhoneNumberHelpers.removeNonNumbericChar(editNum);
			PhoneNumberManager.getIntance(this).blacklistUpdateNumber(editNum,
					blockCall, blockSms, name);

			break;
		}
		case 3: {
			String number = data.getExtras().getString("search_number");
			boolean blockCall = data.getExtras().getBoolean("search_callblock");
			boolean blockSms = data.getExtras().getBoolean("search_smsblock");
			String tag = data.getExtras().getString("search_tag");
			// String replySms = data.getExtras().getString("search_reply_sms");
			int pos = Integer.valueOf(data.getExtras().getString(
					"search_position"));

			if (number != null) {
				Intent intent = new Intent();
				intent.setClass(BlackListManageRootView.this, EditBlackListNumberView.class);

				Bundle bundle = new Bundle();
				bundle.putInt("POSITION", pos);
				bundle.putString("EDIT_NUMBER", number);
				bundle.putString("EDIT_NAME", tag);
				bundle.putBoolean("block_call", blockCall);
				bundle.putBoolean("block_sms", blockSms);
				// bundle.putString("reply_sms", replySms);

				intent.putExtras(bundle);
				startActivityForResult(intent, 2);
			} else {
				Log.d("scfw", "blName is null");
			}
			break;
		}
		case 4: {
			String number = data.getExtras().getString("search_number");

			RemoveNumberFromSearch(PhoneNumberHelpers
					.removeNonNumbericChar(number));
			// silenceRejectCall.removePhone(number);

			refreshViewList();
			break;
		}
		case 5: {
			numlist.clear(); 

        	ActivityLog.logInfo(this,
        			getString(R.string.app_name),getString(R.string.BlacklistCleared));
        	
    		refreshViewList_4();
		}

		default:
			break;
		}

	}

	public void RemoveNumber(int selPosition) {
		try {
			@SuppressWarnings("unchecked")
			Map<String, String> map = (Map<String, String>) mListview
					.getItemAtPosition(mListLongClickPos);
			String number = PhoneNumberHelpers.removeNonNumbericChar(map
					.get("number"));

			PhoneNumberManager.getIntance(this).blacklistDeleteNumber(number);
		} catch (Exception e) {
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
		return;
	}

	private void RemoveNumberFromSearch(String number) {
		String[][] blNumbers = PhoneNumberManager.getIntance(this)
				.getBlacklistNumbers();
		int pos = 0;

		if (blNumbers != null) {
			for (; pos < blNumbers.length; pos++) {
				if (blNumbers[pos][0].equals(number)) {
					break;
				}
			}

			numlist.remove(pos);
			PhoneNumberManager.getIntance(this).blacklistDeleteNumber(number);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();

		menu.add(0, 0, 0, R.string.ClearAll).setIcon(
				android.R.drawable.ic_menu_delete);

		if (numlist == null || numlist.size() == 0) {
			menu.getItem(0).setEnabled(false);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0: {

			AlertDialog ad = new AlertDialog.Builder(this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle(R.string.alert_dialog_two_buttons_title_3)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									if (numlist != null) {

										String[] blNumbers = new String[numlist
												.size()];

										for (int i = 0; i < numlist.size(); i++) {
											blNumbers[i] = PhoneNumberHelpers
													.removeNonNumbericChar((String) numlist
															.get(i).get(
																	"number"));
										}

										Intent intent = new Intent();
										intent.setClass(
												BlackListManageRootView.this,
												ClearWaitingDialog.class);
										intent.putExtra("array", blNumbers);
										intent.putExtra("clear_type",
												CLEAR_BLACK_LIST_NUMBER);
										startActivityForResult(intent, 5);
									}
								}
							})
					.setNegativeButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

								}
							}).create();
			ad.show();

			break;
		}
		default:
			break;
		}
		return true;
	}

	private void UpdateList(int position, boolean blockCall, boolean blockSms,
			String tag) {
		((Map<String, Object>) numlist.get(position)).put("name", tag);

		if (blockCall)
			((Map<String, Object>) numlist.get(position)).put("cbimg",
					R.drawable.rejectcall);
		else
			((Map<String, Object>) numlist.get(position)).put("cbimg",
					R.drawable.empty);

		if (blockSms)
			((Map<String, Object>) numlist.get(position)).put("sbimg",
					R.drawable.sms_reject);
		else
			((Map<String, Object>) numlist.get(position)).put("sbimg",
					R.drawable.empty);

		SimpleAdapter adapter = ((SimpleAdapter) mListview.getAdapter());
		adapter.notifyDataSetInvalidated();
	}

	public void refreshViewList_2() {
		if (numlist.size() == 0) {
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(FC,
					FC);
			ScrollView sv = (ScrollView) findViewById(R.id.bl_root_mgr_user_guide_text);
			sv.setLayoutParams(param);

			// TextView tv = (TextView)findViewById(R.id.blName);
			// tv.setVisibility(TextView.INVISIBLE);
			LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(0,
					0);
			TextView tv = (TextView) findViewById(R.id.add_bl_number_edit_guide_text);
			tv.setLayoutParams(param1);
		} else {
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(0,
					0);
			ScrollView sv = (ScrollView) findViewById(R.id.bl_root_mgr_user_guide_text);
			sv.setLayoutParams(param);

			// TextView tv = (TextView)findViewById(R.id.blName);
			// tv.setVisibility(TextView.VISIBLE);
			LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(
					WC, WC);
			param1.gravity = Gravity.CENTER_HORIZONTAL;
			TextView tv = (TextView) findViewById(R.id.add_bl_number_edit_guide_text);
			tv.setLayoutParams(param1);
		}

		((SimpleAdapter) mListview.getAdapter()).notifyDataSetInvalidated();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	}

	public void refreshViewList_3(String num, boolean blockCall,
			boolean blockSms, String tag) {
		addNumToView(num, blockCall, blockSms, tag);
		refreshViewList();
	}

	private void refreshViewList_4() {
		if (numlist.size() == 0) {
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(FC,
					FC);
			ScrollView sv = (ScrollView) findViewById(R.id.bl_root_mgr_user_guide_text);
			sv.setLayoutParams(param);

			// TextView tv = (TextView)findViewById(R.id.blName);
			// tv.setVisibility(TextView.INVISIBLE);
			LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(0,
					0);
			TextView tv = (TextView) findViewById(R.id.add_bl_number_edit_guide_text);
			tv.setLayoutParams(param1);
		} else {
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(0,
					0);
			ScrollView sv = (ScrollView) findViewById(R.id.bl_root_mgr_user_guide_text);
			sv.setLayoutParams(param);

			// TextView tv = (TextView)findViewById(R.id.blName);
			// tv.setVisibility(TextView.VISIBLE);
			LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(
					WC, WC);
			param1.gravity = Gravity.CENTER_HORIZONTAL;
			TextView tv = (TextView) findViewById(R.id.add_bl_number_edit_guide_text);
			tv.setLayoutParams(param1);
		}

		mListview.invalidateViews();
	}
}
