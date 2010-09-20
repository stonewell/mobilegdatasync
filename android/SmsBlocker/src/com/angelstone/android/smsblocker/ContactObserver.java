package com.angelstone.android.smsblocker;

import java.util.ArrayList;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.provider.Contacts;
import android.util.Log;

import com.angelstone.android.smsblocker.store.AdvScene;
import com.angelstone.android.smsblocker.store.PhoneNumberManager;
import com.angelstone.android.utils.PhoneNumberHelpers;

public class ContactObserver extends ContentObserver {
	private Context contex = null;
	
	private static int bound = 0;
	private int contactCount = 0;
	private long maxContactId = 0;
	private String SORT_ORDER = "_id DESC";
	
	public ContactObserver(Handler handler, Context _ctx) {
		super(handler);
		contex = _ctx;
	}
	
	@Override
	public void onChange(boolean arg0) { 
		super.onChange(arg0);	
	
		if (!PhoneNumberManager.getIntance(contex).readSetting("Firewall_switch") ||
			!PhoneNumberManager.getIntance(contex).readSetting("call_reject_switch"))
			return;
		
		if (isNewContactAdded()) {
			return;
		} 
		
		bound++;
		if (SmsBlocker.getSysCompat(contex).ContactObserverNeedToUpdate(bound)) {
			updateBlDatabase();
			bound = 0;
		}
		
		Log.d("scfw", "Contact changed.");
	}
	
	@Override     
    public boolean deliverSelfNotifications() {      
        return super.deliverSelfNotifications();
    } 
	
	public void setInitialContactInfo() {
		Cursor cursor = contex.getContentResolver().query(
				Contacts.People.CONTENT_URI,
				new String[] {Contacts.People._ID},
				null, null, SORT_ORDER); 
		
		if (cursor != null) {
			contactCount = cursor.getCount();
			if (contactCount > 0)
			{
				cursor.moveToFirst();
				maxContactId = cursor.getLong(
						cursor.getColumnIndex(Contacts.People._ID));
			}
			
			cursor.close();
		}
	}
	
	private boolean isNewContactAdded() {
		boolean ret = true;
		
		Cursor cursor = contex.getContentResolver().query(
				Contacts.People.CONTENT_URI,
				new String[] {Contacts.People._ID},
				null, null, SORT_ORDER); 

		if (cursor != null) {
			long id = 0;
			int count = cursor.getCount();
			if (count > 0)
			{
				cursor.moveToFirst();
				id = cursor.getLong(
					cursor.getColumnIndex(Contacts.People._ID));
			}
			
			if ((count == contactCount) && (id == maxContactId)) {
				ret = false;
			} else {
				contactCount = count;
				maxContactId = id;
				ret = true;
				
				 if (PhoneNumberManager.getIntance(contex).readSetting("Firewall_switch") &&
					 PhoneNumberManager.getIntance(contex).readSetting("call_reject_switch") &&
					 PhoneNumberManager.getIntance(contex).readSetting("advance_switch"))
				 {
					 AdvScene as = PhoneNumberManager.getIntance(contex).getActiveAdvScene();
					 if (as != null && (
						 as.m_Target == AdvScene.TARGET_GROUP_UNKNOWN ||
						 as.m_Target == AdvScene.TARGET_GROUP_CONTACTS))
					 {
					 }						 
				 }
			}
			
			cursor.close();
		} else {
			contactCount = 0;
			maxContactId = 0;
			ret = true;
		}
		
		return ret;
	}
	
	private class BlNumberRecord {
		public int id;
		public String number;
	}
	
	private void updateBlDatabase() {
		ArrayList<BlNumberRecord> blNumbersChanged = getBlNumbersChanged();
		String[][] blNumbers = PhoneNumberManager.getIntance(contex).getBlockCallNumbers();
		
		if (blNumbersChanged == null) {
			return;
		}
		
		int i, j;
		if (blNumbers != null) {
			for (i = 0; i < blNumbers.length; i++) {
				for (j = 0; j < blNumbersChanged.size(); j++) {
					if (Integer.parseInt(blNumbers[i][4]) == 
						blNumbersChanged.get(j).id) {
						PhoneNumberManager.getIntance(contex).blacklistUpdateNumber(
								blNumbersChanged.get(j).id, 
								PhoneNumberHelpers.removeNonNumbericChar(blNumbersChanged.get(j).number), 
								blNumbers[i][1].equals("1"), 
								blNumbers[i][2].equals("1"), 
								blNumbers[i][3]);
						break;
					}
				}
				
				if (j >= blNumbersChanged.size()) {
					PhoneNumberManager.getIntance(contex).blacklistDeleteNumber(blNumbers[i][0]);
				} else {
					blNumbersChanged.remove(j);
				}
			}
		}
		
		for (int k = 0; k < blNumbersChanged.size(); k++) {
			PhoneNumberManager.getIntance(contex).blacklistAddNumber(blNumbersChanged.get(k).number.replace("-", ""), 
										  true, true, new String());
		}
	}
	
	private ArrayList<BlNumberRecord> getBlNumbersChanged() {
//		String blContactId = silenceRejectCall.getBlContactID();
//		Cursor cursor = contex.getContentResolver().query(
//	    		Contacts.Phones.CONTENT_URI,
//	    		new String[] {Contacts.Phones._ID, 
//	    				      Contacts.Phones.NUMBER},
//	    		Contacts.Phones.PERSON_ID + " = '" + blContactId + "'", 
//	    		null, null);
//		if (cursor != null) {
//			ArrayList<BlNumberRecord> blNumbersChanged = 
//				new ArrayList<BlNumberRecord>();
//		
//			cursor.moveToFirst();
//			for (int i = 0; i < cursor.getCount(); i++) {
//				BlNumberRecord record = new BlNumberRecord();
//			
//				record.id = cursor.getInt(
//					cursor.getColumnIndex(Contacts.Phones._ID));
//				record.number = new String(cursor.getString(
//					cursor.getColumnIndex(Contacts.Phones.NUMBER)));
//				blNumbersChanged.add(record);
//				cursor.moveToNext();
//			}
//			
//			cursor.close();
//			return blNumbersChanged;
//		} else {
//			return null;
//		}
		return new ArrayList<BlNumberRecord>();
	}
}
