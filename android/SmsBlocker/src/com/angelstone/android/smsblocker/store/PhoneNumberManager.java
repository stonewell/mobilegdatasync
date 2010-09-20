package com.angelstone.android.smsblocker.store;

import java.io.FileInputStream;
import java.util.Iterator;
import java.util.TreeMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.Contacts;

import com.angelstone.android.smsblocker.SysCompat;
import com.angelstone.android.utils.PhoneNumberHelpers;

/** 
* call firewall database interface 
* 
* @see com.angelstone.android.smsblocker.store.PhoneNumberDisposition 
* @see com.angelstone.android.smsblocker.store.Scene 
*/ 
public class PhoneNumberManager {
	
	private static final String DATABASE_NAME = "cfw1.db";	//call firewall database name
	private static final String SETTING_TABLE = "setting";
	private static final String SCENE_TABLE = "scenes";
	private static final String BLIST_TABLE = "blists";	
	private static final String PATTERN_TABLE = "filters_pattern";	 
	private static final String LOG_TABLE = "event_logs";
	private static final String BLIST_ID_OPTION = "max_blist_id";
	private static final String ACTIVE_SCENE_OPTION = "active_scene";
	private static final String BLACK_LIST_TABLE = "black_list";
	private static final String ADV_SCENE_SMS = "adv_scene_sms";
	private static final String ADV_SCENE_TABLE = "adv_scenes";
	private static final String DONT_ASK_TABLE = "dont_ask";
	private static final String CONTACT_RINGTONE = "contact_ringtone";
	private static final int UNKNOWN_BLIST_ID = 0;
	public static final int INSERT_ERROR_NONE = 0;
	public static final int INSERT_ERROR_AREADY_EXIST = 2;
	public static final int INSERT_ERROR_EXCEPTION_OCCUR = -1;
	
	private SQLiteDatabase m_db = null;
	private Context m_ctx = null;
	
	private static PhoneNumberManager _intance = null;
	
	private static int refcount = 0;
	
	public enum LogSelection { call_log, sms_log, both_log };
	
    public void close()
    {
    	if (refcount == 0)
    	{
    		if (m_db != null)
    		{
    			m_db.close();
    		}
    		
    		_intance = null;
    	}
    	else 
    	{
    		refcount--;
    	}
    }
    
    public static PhoneNumberManager getIntance (Context ctx)
    {
    	if (_intance == null)
    	{
    		String lockStr = "lock";
    		synchronized (lockStr)
    		{
    			if (_intance == null)
    			{
    				_intance = new PhoneNumberManager (ctx);
    			}
    		}
    	}
    	
    	refcount++;
    	
    	return _intance;
    }
    
	private PhoneNumberManager( Context ctx )
	{
		m_ctx = ctx;
		initDatabase();		
	}
	public boolean cmpNumber( String num, String blnum )
	{
		if (num == null || blnum == null)
		{
			return false;
		}
		
		if (num.length() < 7 || blnum.length() < 7)
		{
			if (num.equals(blnum))
			{
				return true;
			}
			else 
			{
				return false;
			}
		}
		
		if (num.length() > blnum.length())
		{
			//must be appendix matching
			//if (num.indexOf(blnum) != -1)
			if (num.substring(num.length() - blnum.length()).compareTo(blnum) == 0)
			{
				return true;
			}else
			{
				return false;
			}
		}
		else
		{
			//if (blnum.indexOf(num) != -1)
			if (blnum.substring(blnum.length() - num.length()).compareTo(num) == 0)
			{
				return true;
			}else
			{
				return false;
			}
		}
	}
	public PhoneNumberDisposition queryAction( String number )
	{
		PhoneNumberDisposition disp = new PhoneNumberDisposition();		
		queryBlackList(number, disp);
		
        return disp;		
	}
	
	public void queryBlackList(String number, PhoneNumberDisposition disp)
	{
		Cursor cur = null;
		
		try
		{		
			cur = m_db.query( BLACK_LIST_TABLE,
								new String[]{"number", "block_call", "block_sms", "tag"},
								null, null, null, null, null );
			
			if (cur == null || cur.getCount() <= 0)
			{
				if (cur != null)
				{
					cur.close();
				}
				
				disp.m_CallAction = PhoneNumberDisposition.CALL_ACCEPT;
	        	disp.m_SmsAction  = PhoneNumberDisposition.SMS_ACCEPT; 
				
				return;
			}
			
			//cur.moveToFirst();
			cur.close();
			
			StringBuffer blockCallFlag = new StringBuffer();
			StringBuffer blockSmsFlag = new StringBuffer();
			
			//Currently we would not to reply sms, so it set to null just ok;
			StringBuffer replySms = null;
			
			if (getBlacklistNumberInfo(number, blockCallFlag, blockSmsFlag, replySms))
			{	
				if( blockCallFlag.toString().equals("1") )
				{
	        		disp.m_CallAction = PhoneNumberDisposition.CALL_REJECT;
	        	}
				else
	        	{
	        		disp.m_CallAction = PhoneNumberDisposition.CALL_ACCEPT;        		
	        	}
				
	        	if( blockSmsFlag.toString().equals("1") )
	        	{
	        		disp.m_SmsAction = PhoneNumberDisposition.SMS_REJECT;
	        	}
	        	else
	        	{
	        		disp.m_SmsAction = PhoneNumberDisposition.SMS_ACCEPT;
	        	}
	        	
	        	disp.m_ReplySms = null;
	        	
	        	cur.close();
	        	return;
			}
		}
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			
			disp.m_CallAction = PhoneNumberDisposition.CALL_ACCEPT;
        	disp.m_SmsAction  = PhoneNumberDisposition.SMS_ACCEPT; 
				
			return;
		}
	}
	
	public boolean isInBlacklist(String number)
	{
		Cursor cur = null;
		
		if (number.length() < 7)
		{
			String whereString = "number = '" + number + "'";
			
			cur = m_db.query( BLACK_LIST_TABLE,
					          new String[]{"number"},
					          whereString, null, null, null, null );
			
			if (cur != null && cur.getCount() >= 1)
			{
				cur.close();
				
				return true;
			}
			
			if (cur != null)
			{
				cur.close();
			}
		}
		else
		{
			String whereString = "number LIKE '" + "%" + number + "'";
			
			cur = m_db.query( BLACK_LIST_TABLE,
			                  new String[]{"number"},
			                  whereString, null, null, null, null );
			
			if (cur != null && cur.getCount() >= 1)
			{
				cur.close();
				
				return true;
			}
			
			if (cur != null)
			{
				cur.close();
			}

		
			for (int i = 0; i < number.length() - 7; i++)
			{
				String tempNum = number.substring(i + 1);
				
				whereString = "number = '" + tempNum + "'";
				
				cur = m_db.query( BLACK_LIST_TABLE,
						  		  new String[]{"number"},
						  		  whereString, null, null, null, null );
				
				if (cur != null && cur.getCount() >= 1)
				{
					cur.close();
					
					return true;
				}
				
				if (cur != null)
				{
					cur.close();
				}
			}
		}

		return false;
	}
	
	public boolean getBlacklistNumberInfo(String number, 
			                              StringBuffer blockCallFlag,
			                              StringBuffer blockSmsFlag,
			                              StringBuffer reply_sms)
	{
		Cursor cur = null;
		
		if (number.length() < 7)
		{
			String whereString = "number = '" + number + "'";
			
			cur = m_db.query( BLACK_LIST_TABLE,
					          new String[]{"number", "block_call", "block_sms", "tag"},
					          whereString, null, null, null, null );
			
			if (cur != null && cur.getCount() >= 1)
			{
				cur.moveToFirst();
				blockCallFlag.append(Integer.toString(cur.getInt(cur.getColumnIndex("block_call"))));
				blockSmsFlag.append(Integer.toString(cur.getInt(cur.getColumnIndex("block_sms"))));
				reply_sms = null;
				
				cur.close();
	        
				return true;
			}
			
			if (cur != null)
			{
				cur.close();
			}
		}
		else
		{
			
			String whereString = "number LIKE '" + "%" + number + "'";
			
			cur = m_db.query( BLACK_LIST_TABLE,
			                  new String[]{"number", "block_call", "block_sms", "tag"},
			                  whereString, null, null, null, null );
			
			if (cur != null && cur.getCount() >= 1)
			{		
				cur.moveToFirst();
				blockCallFlag.append(Integer.toString(cur.getInt(cur.getColumnIndex("block_call"))));
				blockSmsFlag.append(Integer.toString(cur.getInt(cur.getColumnIndex("block_sms"))));
				reply_sms = null;
				
				cur.close();
				return true;
			}
			
			if (cur != null)
			{
				cur.close();
			}
			
			for (int i = number.length() - 7; i > 0 ; i--)
			{
				String tempNum = number.substring(i);
				
				whereString = "number = '" + tempNum + "'";
				
				cur = m_db.query( BLACK_LIST_TABLE,
						  		  new String[]{"number", "block_call", "block_sms", "tag"},
						  		  whereString, null, null, null, null );
				
				
				if (cur != null && cur.getCount() >= 1)
				{
					cur.moveToFirst();
					blockCallFlag.append(Integer.toString(cur.getInt(cur.getColumnIndex("block_call"))));
					blockSmsFlag.append(Integer.toString(cur.getInt(cur.getColumnIndex("block_sms"))));
					reply_sms = null;
					
					cur.close();
					return true;
				}
				
				if (cur != null)
				{
					cur.close();
				}
				
			}
			
		}
		return false;
	}
	
	/**  Get the disposition of specific phone number 
	* @param query    caller fill the 'm_Number' field. 
	* callee fill the 'm_call_disposition' and 'm_smsg_disposition' fields
	*/ 
	public int queryDispostion( String number, PhoneNumberDisposition query )
	{	
		int err = 0;
		Cursor cur = null;
		try
		{
			String where_str = "option='" + ACTIVE_SCENE_OPTION +"'";
			cur = m_db.query( SETTING_TABLE, new String[]{"value"}, where_str, null, null, null, null );
			
			if( cur == null ) 
			{ 
				return -1; 
			}	//unexpected

			cur.moveToFirst();
			
			if( cur.getCount() != 1 )
			{	
				query.setDefaultPass();
				//temp code begin ->
				query.m_SmsAction = check_sms_blacklist( number );
				
				cur.close();
				//temp code end <-
				return 0;
			}
			
			String scene = cur.getString( cur.getColumnIndex( "value" ) );
			
			if( !is_scene_existed( scene ) ) 
			{ 
				cur.close();
				return -1; 
			} //internal state error!
			
			cur = m_db.query( SCENE_TABLE, new String[]{"call_action, sms_action, blist_id"}, "name='" + scene + "'", null, null, null, null );
			
			if (cur == null || cur.getCount() <= 0)
			{
				if (cur != null)
				{
					cur.close();
				}
				return -1;
			}
			
			cur.moveToFirst();
			String blist_id_str = cur.getString(cur.getColumnIndex("blist_id"));
			String call_action = cur.getString(cur.getColumnIndex("call_action"));
			String sms_action = cur.getString(cur.getColumnIndex("sms_action"));
			
			if( Integer.parseInt( blist_id_str ) == UNKNOWN_BLIST_ID )
			{	//this scene has never bound to any blacklist
				query.setDefaultPass();
				//temp code begin ->
				query.m_SmsAction = check_sms_blacklist( number );
				//temp code end <-
				
				cur.close();
				
				return 0;
			}
			
			String tblname = getBlacklistTableName( blist_id_str );
			cur = m_db.query( tblname, new String[]{"number"}, "number='" + number + "'", null, null, null, null );
			
			if (cur == null)
			{
				return -1;
			}
			
			if( cur.getCount() == 1)
			{
				query.m_CallAction = Integer.parseInt( call_action );
				query.m_SmsAction = Integer.parseInt( sms_action );
			}else
			{	//not in blacklist
				query.setDefaultPass();
			}	
			//should stop here, yet current design support a strange sms scene...
			//temp code begin ->
			query.m_SmsAction = check_sms_blacklist( number );
			
			cur.close();
			//temp code end <-
		}catch( Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			err = -1;
		}
		
		
		return err;
	}
	//temp code begin ->
	public int setSmsBlacklist( String bl_name )
	{	
		if( !is_blist_existed( bl_name ) ) { return 1; }
		int err = 0;
		Cursor cur = null;
		try
		{
			String where_str = "option='sms_blist'";
			cur = m_db.query( SETTING_TABLE, new String[]{"value"}, where_str, null, null, null, null );
			
			if( cur == null ) 
			{ 
				return -1; 
			}	//unexpected
			
			ContentValues args = new ContentValues();
			cur.moveToFirst();
			
			if( cur.getCount() == 0 )
			{	//insert 
				args.put( "option", "sms_blist");
				args.put( "value", bl_name );
				m_db.insert( SETTING_TABLE, null, args );
				
				cur.close();
			}else
			{
				args.put( "value", bl_name );
				m_db.update( SETTING_TABLE, args, where_str, null );
				
				cur.close();
			}
		}catch( Exception e)
		{
			err = -1;
			
			if (cur != null)
			{
				cur.close();
			}
		}
		return err;
	}
	
	public String getSmsBlacklist() 
	{
		Cursor cur = null;
		
		try 
		{
			String where_str = "option='sms_blist'";
			cur = m_db.query(SETTING_TABLE, new String[] { "value" }, where_str, null, null, null, null);
			
			if (cur == null)
			{ 
				return null; 
			} // unexpected
			
			cur.moveToFirst();
			
			String vStr = cur.getString(cur.getColumnIndex("value"));
			
			cur.close();
			return vStr;
		} 
		catch (Exception e) 
		{
			if (cur != null)
			{
				cur.close();
			}
			return null;
		}
	}
	
	private int check_sms_blacklist( String number )
	{
		int disp = PhoneNumberDisposition.SMS_ACCEPT;
		Cursor cur = null;
		
		try {
			String where_str = "option='sms_blist'";
			cur = m_db.query(SETTING_TABLE, new String[] { "value" }, where_str, null, null, null, null);
			
			if (cur == null || cur.getCount() !=1  ) 
			{ 
				if (cur != null)
				{
					cur.close();
				}
				return disp; 
			} //unexpected or ms blacklist not set yet
			cur.moveToFirst();
			String sms_bl_name = cur.getString( cur.getColumnIndex( "value" ) );	
			
			if( sms_bl_name == null )
			{
				cur.close();
				return disp;
			}
			
			String tblname = getBlistTblByName( sms_bl_name );
			where_str = "number='" + number + "'";
			cur = m_db.query( tblname, new String[]{ "number"}, where_str, null, null, null, null );
			
			if( cur != null && cur.getCount() == 1)
			{ 	//found the number in sms blacklist
				disp = PhoneNumberDisposition.SMS_REJECT;
			}
			
			cur.close();
		} 
		catch (Exception e) 
		{
			if (cur != null)
			{
				cur.close();
			}
		}
		
		return disp;
	}
	//temp code end <-
	/** Get count of all the scenes 
	* @return total number of all the scenes
	*/ 
	public int getBlacklistCount()
	{ 
		Cursor cur = m_db.query( BLIST_TABLE, new String[]{"id"}, null, null, null, null, null );  
		
		if( cur == null) 
		{ 
			return 0; 
		}
		cur.moveToFirst();
		
		int count = cur.getCount();
		return count;
	}
	/** 
	* get the names of all the scenes 
	* @param names a String array to get scenes' names
	* @return number of the scenes actually got. -1 if error occur.
	*/
	public int getBlacklistNames( String names[] )
	{     
		return getNames( BLIST_TABLE, names );
	}	
	/** 
	* add a new blacklist 
	* @param name		name of the new scene to add
	* @return '0' if the insertion is successful.
	*/
	public int addBlacklist( String bl_name )
	{
		if( bl_name == null ) { return 1;}
		int err = 0;
		Cursor cur = null;
		
		try
		{	
			if( is_blist_existed( bl_name ) ) { return 1; }
			String blist_id_str = null, sql = null;			
			cur = m_db.query( SETTING_TABLE, new String[]{"value"}, "option='" + BLIST_ID_OPTION + "'",
					null, null, null, null );			
			
			if( cur == null ) 
			{	
				return -2; 
			}//unexpected 
			
			cur.moveToFirst();
			
			if( !cur.isAfterLast() ) 
			{   //get current value, then increase it by 1  
				blist_id_str = cur.getString( cur.getColumnIndex( "value") );
				int blist_id = Integer.parseInt( blist_id_str );
				blist_id++;
				ContentValues args = new ContentValues();
				args.put( "value", String.format( "%d", blist_id ) );
				m_db.update( SETTING_TABLE, args, "option='" + BLIST_ID_OPTION + "'", null );
			}
			else
			{	//insert the initial value
				sql = "INSERT INTO " + SETTING_TABLE + " (option, value )" + " VALUES ('" + BLIST_ID_OPTION + "', '2' );";
				m_db.execSQL( sql  );
				blist_id_str = "1";
			}	
			
			ContentValues blist= new ContentValues();
			blist.put( "name", bl_name );
			blist.put( "id", blist_id_str );
			m_db.insert( BLIST_TABLE, null, blist );
			sql = "CREATE TABLE IF NOT EXISTS " + getBlacklistTableName( blist_id_str ) + " (number VARCHAR PRIMARY KEY );" ;
			m_db.execSQL( sql);		
			
			cur.close();
		} 
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			err = -1;
		}  
		return err;
	}		
	
	private String getBlacklistTableName( String id_str )
	{
		return String.format( "blist_%s", id_str );
	}
	
	public int getBlacklistNumbersCount( String name )
	{
		Cursor cur = null;
		int count = 0;
		
		try 
		{
		
			String tblname = getBlistTblByName( name );
			
			if( tblname == null ) 
			{ 
				return 0; 
			}
			
			cur = m_db.query( tblname, new String[]{"number"}, null, null, null, null, null );
			cur.moveToFirst();
			
			count = cur.getCount();
			cur.close();
		}
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			
			e.printStackTrace();
		}
		
		return count;
	}
	
	public int getBlacklistNumbersCount()
	{
		Cursor cur = null;
		int count = 0;
		
		try
		{
			cur = m_db.query( "black_list", new String[]{"number"}, null, null, null, null, null );
		
			if (cur == null)
			{
				return 0;
			}
			
			cur.moveToFirst();
			
			count = cur.getCount();
			cur.close();
		}
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			
			e.printStackTrace();
		}
		
		return count;
	}

	public String[][] getBlacklistNumbers()
	{
		Cursor cur = null;
		
		try
		{
			cur = m_db.query( BLACK_LIST_TABLE,
								new String[]{"number", "block_call", "block_sms", "tag"},
								null, null, null, null, null );
			if (cur == null || cur.getCount() <= 0)
			{
				if (cur != null)
				{
					cur.close();
				}
				
				return null;
			}
			
		
			cur.moveToFirst();
			int i = 0;
			String [][] ret = new String[cur.getCount()][5];
			while( !cur.isAfterLast() )
			{	
				ret[i][0] = new String( cur.getString( cur.getColumnIndex( "number" ) ) );
				ret[i][1] = new String( Integer.toString(cur.getInt( cur.getColumnIndex( "block_call" ) ) ));
				ret[i][2] = new String( Integer.toString(cur.getInt( cur.getColumnIndex( "block_sms" ) ) ));
				ret[i][3] = new String( cur.getString( cur.getColumnIndex( "tag" ) ));
				i++;
				cur.moveToNext();
			}
			if (cur != null)
				cur.close();
			return ret;
		}
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			e.printStackTrace();
			
			return null;
		}
	}
	
	public String[][] getBlockCallNumbers()
	{
		Cursor cur = null;
		
		try
		{
		
			cur = m_db.query( BLACK_LIST_TABLE,
								new String[]{"number", "block_call", "block_sms", "tag"},
								"block_Call = 1",
								null, null, null, null );
		
			if (cur == null || cur.getCount() <= 0)
			{
				if (cur != null)
				{
					cur.close();
				}
				return null;
			}
			
			cur.moveToFirst();
			int i = 0;
			String [][] ret = new String[cur.getCount()][5];
			while( !cur.isAfterLast() )
			{	
				ret[i][0] = new String( cur.getString( cur.getColumnIndex( "number" ) ) );
				ret[i][1] = new String( Integer.toString(cur.getInt( cur.getColumnIndex( "block_call" ) ) ));
				ret[i][2] = new String( Integer.toString(cur.getInt( cur.getColumnIndex( "block_sms" ) ) ));
				ret[i][3] = new String( cur.getString( cur.getColumnIndex( "tag" ) ));
				//ret[i][4] = new String( Integer.toString(cur.getInt( cur.getColumnIndex( "phone_id" ) ) ));
				i++;
				cur.moveToNext();
			}
			if (cur != null)
				cur.close();
			return ret;
		}
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			e.printStackTrace();
			
			return null;
		}
	}	
	
	public int getBlacklistNumbers( String name, String Numbers[] )
	{	
		Cursor cur = null;

		try
		{
			cur = m_db.query( BLACK_LIST_TABLE, new String[]{"number"}, null, null, null, null, null );
		
			if (cur == null || cur.getCount() <= 0)
			{
				if (cur != null)
				{
					cur.close();
				}
				return -1;
			}
			
			cur.moveToFirst();
			int i = 0;
			int num_col_idx = cur.getColumnIndex( "number" );
			while( !cur.isAfterLast() )
			{	
				if (i + 1 > Numbers.length) { // not enough space
					break;
				}
				Numbers[i] = new String( cur.getString( num_col_idx ) );
				i++;
				cur.moveToNext();
			}
			
			cur.close();
			
			return i;
		}
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			e.printStackTrace();
			
			return -1;
		}
	}
	
	public int syncBlacklistReference( String name, boolean do_update )
	{
		if( !is_blist_existed( name ) ) { return 0; }
		int err = 0, count = err;
		Cursor cur = null;
		try
		{	
			String where_str = "name='" + name +"'";
			cur = m_db.query( BLIST_TABLE, new String[]{"id"}, where_str, null, null, null, null );
			
			if( cur == null || cur.getCount() != 1 ) 
			{ 
				if (cur != null)
				{
					cur.close();
				}
				
				return -2; 
			}
			
			cur.moveToFirst();
			String blist_id = cur.getString( cur.getColumnIndex( "id" ) );
			where_str = "blist_id='" + blist_id + "'";
			cur = m_db.query( SCENE_TABLE, new String[]{"name"}, where_str, null, null, null, null );
			
			if (cur == null)
			{
				return -2;
			}
			
			cur.moveToFirst();
			count = cur.getCount();
			
			if( count > 0 && do_update )
			{
				ContentValues args = new ContentValues();
				args.put( "blist_id", UNKNOWN_BLIST_ID );
				m_db.update( SCENE_TABLE, args, where_str, null );
			}
			
			cur.close();
			
			//temp code begin ->
			where_str = "option='sms_blist'";
			cur = m_db.query( SETTING_TABLE, new String[]{"value"}, where_str, null, null, null, null);
			
			if( cur != null && cur.getCount() == 1 )
			{
				cur.moveToFirst();
				String sms_blist = cur.getString( cur.getColumnIndex( "value" ) );
				if( sms_blist.equals( name ) )
				{
					count++;
					if( do_update )
					{
						ContentValues args = new ContentValues();
						String nullStr = null;
						args.put( "value", nullStr );
						m_db.update( SETTING_TABLE, args, where_str, null );
					}
				}
				
				cur.close();
			}
			else
			{
				if (cur != null)
				{
					cur.close();
				}
			}
			//temp code end <-
		} catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			err = -1;
		}  
		return count;		
	}
	public int getBlacklistRefCount( String name )
	{ 
		return syncBlacklistReference( name, false );		
	}
	/** 
	* delete a scene 
	* @param name		name of the new scene to delete
	* @return '0' if the deletion is successful. 
	*/
	public int delBlacklist( String name )
	{
		String tblname = getBlistTblByName(name);
		int ref = 0;

		try {
			if (tblname != null) {
				syncBlacklistReference( name, true );
				m_db.execSQL("DROP TABLE IF EXISTS " + tblname);
				m_db.delete( BLIST_TABLE, "name='" + name + "'", null);
				return 0;
			} else
				return 1;
		} catch (Exception e) {
			ref = -1;
		}
		return ref;
	}	
	/**
	 * edit a scene
	 * 
	 * @param name
	 *            name of the scene to edit
	 * @param update_or_del
	 *            'true' if the number_dispostion is to be inserted, 'false'
	 *            otherwise
	 * @param number_disposition
	 *            the phone number disposition information
	 * @return '0' if the editing is successful
	 */
	public int blacklistAddNumber( String bl_name, String number )
	{	
		int err = 0;
		Cursor cur = null;
		
		try
		{
			String tblname = getBlistTblByName( bl_name );
			
			if( tblname == null )
			{	//try inserting into a none-existed blacklist
				return 3; 
			} 
			
			String where_str = new String( "number='" + number + "'" );
			cur = m_db.query( tblname, new String[]{"number"}, where_str, null, null, null, null ); 
			
			if( cur == null ) 
			{ 
				return -2; 
			} //unexpected
			
			cur.moveToFirst();
			if( cur.getCount() != 0 )
			{ 
				cur.close();
				return 2; 
			} //number already existed
			
			ContentValues args = new ContentValues();
			args.put( "number", number );
			m_db.insert( tblname, null, args );
			
			cur.close();
		}catch( Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			err = -1;
		}
		return err;
	}
	
	public int blacklistAddNumber(String number, boolean blockCall, boolean blockSms, String tag)
	{	
		int err = INSERT_ERROR_NONE;
		Cursor cur = null;
		
		try
		{
			String where_str = new String( "number='" + number + "'" );
			cur = m_db.query( BLACK_LIST_TABLE, new String[]{"number"}, where_str, null, null, null, null ); 
			//if( cur == null ) { return -2; } //unexpected
			//cur.moveToFirst();
			if( cur != null && cur.getCount() != 0 ) 
			{ 
				cur.close();
				return INSERT_ERROR_AREADY_EXIST; 
			} //number already existed
			
			ContentValues args = new ContentValues();
			args.put( "number", number );
			args.put( "block_call", blockCall ? 1 : 0 );
			args.put( "block_sms", blockSms ? 1 : 0);
			args.put( "tag", tag);
			//args.put( "reply_sms", replySms);
			//args.put( "phone_id", 0);
			m_db.insert( BLACK_LIST_TABLE, null, args );
			
			if (cur != null)
			{
				cur.close();
			}
		}
		catch( Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			err = INSERT_ERROR_EXCEPTION_OCCUR;
		}
		doAsk(number);
		
		return err;
	}
	
	public int blacklistDeleteNumber( String number )
	{	
		int err = 0;
		Cursor cur = null;
		
		try
		{
			String where_str = new String( "number='" + number + "'" );
			cur = m_db.query( BLACK_LIST_TABLE, new String[]{"number"}, where_str, null, null, null, null ); 
			
			if( cur == null ) 
			{
				return -1; 
			} //unexpected
			
			cur.moveToFirst();
			
			if( cur.getCount() != 1 ) 
			{ 
				cur.close();
				return 2; 
			} //number not existed
			
			m_db.delete( BLACK_LIST_TABLE, where_str, null );
			
			cur.close();
		}
		catch( Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			err = -1;
		}
		return err;
	}
	
	public int blacklistUpdateNumber(String number, boolean blockCall, boolean blockSms, String tag, int id)
	{	
		int err = 0;
		try
		{
			/*String where_str = new String( "number='" + number + "'" );
			Cursor cur = m_db.query( BLACK_LIST_TABLE, new String[]{"number"}, where_str, null, null, null, null ); 
			//if( cur == null ) { return -2; } //unexpected
			//cur.moveToFirst();
			if( cur != null && cur.getCount() != 0 ) { return 2; } //number already existed
			*/
			ContentValues args = new ContentValues();
			args.put( "block_call", blockCall ? 1 : 0 );
			args.put( "block_sms", blockSms ? 1 : 0);
			args.put( "tag", tag);
			//args.put( "phone_id", id);
			m_db.update( BLACK_LIST_TABLE, args, "number='" + number + "'", null );
			}
		catch( Exception e)
		{
			err = -1;
		}
		return err;
	}
	
	public int blacklistUpdateNumber(String number, boolean blockCall, boolean blockSms, String tag)
	{	
		int err = 0;
		try
		{
			/*String where_str = new String( "number='" + number + "'" );
			Cursor cur = m_db.query( BLACK_LIST_TABLE, new String[]{"number"}, where_str, null, null, null, null ); 
			//if( cur == null ) { return -2; } //unexpected
			//cur.moveToFirst();
			if( cur != null && cur.getCount() != 0 ) { return 2; } //number already existed
			*/
			ContentValues args = new ContentValues();
			args.put( "block_call", blockCall ? 1 : 0 );
			args.put( "block_sms", blockSms ? 1 : 0);
			args.put( "tag", tag);
			m_db.update( BLACK_LIST_TABLE, args, "number='" + number + "'", null );
			}
		catch( Exception e)
		{
			err = -1;
		}
		return err;
	}
	
	public int blacklistUpdateNumber(int id, String number, boolean blockCall, boolean blockSms, String tag)
	{	
		int err = 0;
		try
		{
			/*String where_str = new String( "number='" + number + "'" );
			Cursor cur = m_db.query( BLACK_LIST_TABLE, new String[]{"number"}, where_str, null, null, null, null ); 
			//if( cur == null ) { return -2; } //unexpected
			//cur.moveToFirst();
			if( cur != null && cur.getCount() != 0 ) { return 2; } //number already existed
			*/
			ContentValues args = new ContentValues();
			args.put( "number", number );
			args.put( "block_call", blockCall ? 1 : 0 );
			args.put( "block_sms", blockSms ? 1 : 0);
			args.put( "tag", tag);
			m_db.update(BLACK_LIST_TABLE, args,
					null, null );
			}
		catch( Exception e)
		{
			err = -1;
		}
		return err;
	}
	
	private int addScene( String name )
	{	
		int err = 0;		
		try
		{
			if( is_scene_existed( name ) ) { return 1; }
			
			ContentValues args = new ContentValues();
			args.put( "name",        name );
			args.put( "call_action", PhoneNumberDisposition.CALL_REJECT );
			args.put( "sms_action",  PhoneNumberDisposition.SMS_REJECT );
			args.put( "blist_id",    UNKNOWN_BLIST_ID );
			m_db.insert( SCENE_TABLE, null, args );
		}catch( Exception e)
		{
			err = -1;
		}
		return err;
	}
	private int delScene( String name )
	{	
		int err = 0;
		
		try
		{	
			if( !is_scene_existed( name ) ) { return 1; }
			m_db.delete( SCENE_TABLE, "name='" + name + "'", null );
		}catch( Exception e)
		{
			err = -1;
		}
		return err;
	}
	private int getSceneCount()
	{	
		Cursor cur = null;
		try
		{
			cur = m_db.query( SCENE_TABLE, new String[]{"name"}, null, null, null, null, null );
		
			if (cur == null)
			{
				return 0;
			}
			
			int count = cur.getCount();
			
			cur.close();
			
			return count;
		}
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			
			e.printStackTrace();
			
			return 0;
		}
	}
	private int getSceneNames( String names[] )
	{
		return getNames( SCENE_TABLE, names );	
	}
	private int getNames(String table_name, String names[]) 
	{
		Cursor cur = null;
		
		try
		{
			cur = m_db.query( table_name, new String[] { "name" }, null, null, null, null, null );
		
			if (cur == null) 
			{
				return -1;
			} // unexpected
			
			cur.moveToFirst();
			int i = 0;
			int name_col_idx = cur.getColumnIndex("name");
			while (!cur.isAfterLast()) {
				if (i + 1 > names.length) { // not enough space
					break;
				}
				names[i] = new String( cur.getString( name_col_idx ) );
				i++;
				cur.moveToNext();
			}
			cur.close();
			
			return i;
		}
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			
			e.printStackTrace();
			
			return -1;
		}
	}
	public int SetSceneBlacklist( String name, String blist_name )
	{	
		return 0;
	/*	int err = 0;
		
		try
		{		
			if( !is_scene_existed( name ) || !is_blist_existed( blist_name )) { return 1; }

			Cursor cur = m_db.query( BLIST_TABLE, new String[]{"id"},
					"name='" + blist_name + "'", null, null, null, null );
			cur.moveToFirst();
			int id = Integer.parseInt( cur.getString( cur.getColumnIndex( "id") ) );
			ContentValues args = new ContentValues();
			args.put( "blist_id", id );
			m_db.update( SCENE_TABLE, args, "name='" + name + "'", null );			
		}catch( Exception e)
		{
			err = -1;
		}
		return err;*/
	}
	public int SetSceneDisposition( String name, PhoneNumberDisposition disp )
	{	
		return 0;
	/*	int err = 0;
		
		try
		{	
			if( !is_scene_existed( name ) ) { return 1; }	
			ContentValues args = new ContentValues();
			args.put( "call_action", disp.m_CallAction );
			args.put( "sms_action",  disp.m_SmsAction );
			m_db.update( SCENE_TABLE, args, "name='" + name + "'", null );				
		}catch( Exception e)
		{
			err = -1;
		}
		return err;*/
	}
	/** 
	* query the detailed information of a scene 
	* @param scene		the the scene object to be filled
	* @return '0' if the query is successful, '1' if the scene doesn't exist.
	*/
	public String getSceneBlacklist( String name )
	{	
		Cursor cur = null;
		
		try
		{
			if( !is_scene_existed( name ) ) 
			{ 
				return null; 
			}
			
			cur = m_db.query( SCENE_TABLE, new String[]{"blist_id"}, "name='" + name + "'", null, null, null, null );
			
			if (cur == null || cur.getCount() <= 0)
			{
				if (cur != null)
				{
					cur.close();
				}
				
				return null;
			}
			
			cur.moveToFirst();
			String id_str = cur.getString( cur.getColumnIndex( "blist_id") );
			int id = Integer.parseInt( id_str);
			
			if( id != UNKNOWN_BLIST_ID )
			{
				cur = m_db.query( BLIST_TABLE, new String[]{ "name" }, "id='" + id_str + "'", null, null, null, null );
				
				if (cur == null)
				{
					return null;
				}
				
				if( cur.getCount() != 1 ) 
				{ 
					cur.close();
					return null; 
				} //internal table relation corrupted!
				cur.moveToFirst();
				
				String temp = cur.getString( cur.getColumnIndex( "name") );	
				
				cur.close();
				
				return temp;
			}
		}catch( Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
		}
		return null;
	}
	/** rename a scene 
	* @param name		current name of a scene
	* @param new_name	new name of a scene
	* @return '0' if the renaming is successful, '1' if scene doesn't exist
	*/
	public int renameScene( String name, String new_name)
	{
		int err = 0;
		try
		{			
		}catch( Exception e)
		{
			err = -1;
		}
		return err;
	}	
	/** set the active scene 
	* @param name		the name of the scene to activate
	* @return '0' if the activating is successful
	*/
	private int setActiveScene( String name )
	{	
		int err = 0;
		Cursor cur = null;
		
		try
		{
			//if( !is_scene_existed( name ) ) { return 1; }
			
			String where_str = "option='" + ACTIVE_SCENE_OPTION +"'";
			cur = m_db.query( SETTING_TABLE, new String[]{"value"}, where_str, null, null, null, null );
			
			if( cur == null ) 
			{
				return 1; 
			}	//unexpected
			
			ContentValues args = new ContentValues();
			
			if( cur.getCount() != 0 )
			{	//already existed, do updating
				args.put( "value", name );
				m_db.update( SETTING_TABLE, args, where_str, null );
				
				cur.close();
			}
			else
			{	//insert new record
				args.put( "option", ACTIVE_SCENE_OPTION );
				args.put( "value", name );
				m_db.insert( SETTING_TABLE, null, args );
				cur.close();
			}
		}
		catch( Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			err = -1;
		}
		return err;
	}
	private String getActiveScene()
	{
		Cursor cur = null;
		
		try 
		{
			String where_str = "option='" + ACTIVE_SCENE_OPTION + "'";
			cur = m_db.query(SETTING_TABLE, new String[] { "value" }, where_str, null, null, null, null );
			
			if (cur == null || cur.getCount() != 1) 
			{// unexpected
				if (cur != null)
				{
					cur.close();
				}
				return null;
			}
			else
			{
				cur.moveToFirst();
				
				String sceneName = cur.getString( cur.getColumnIndex( "value" ) );
				cur.close();
				return sceneName;
			}
		}
		catch (Exception e) 
		{
			if (cur != null)
			{
				cur.close();
			}
			
			return null;
		}	
	}
	/** 
	* add a new short message filter string 
	* @param pat_str of the filter to add
	* @return 'true' if the insertion is successful, 'false' otherwise
	*/
	public int addSmsFilter( String pat_str )
	{
		if( is_pattern_existed( pat_str ) ) { return 1; }
		ContentValues args = new ContentValues();
		args.put( "name", pat_str );
		m_db.insert( PATTERN_TABLE, null, args );
		return 0;
	}
	/** 
	* delete a short message filter string 
	* @param pat_str of the filter to delete
	* @return 'true' if the deletion is successful, 'false' otherwise
	*/
	public int deleteSmsFilter( String pat_str )
	{	
		if( !is_pattern_existed( pat_str ) ) { return 1; }
		m_db.delete( PATTERN_TABLE, "name='" + pat_str + "'", null );
		return 0;
	}
	/** 
	* get count of all short message filter strings 
	* @return total number of all the filter strings
	*/ 
	public int getSmsFilterCount()
	{
		Cursor cur = m_db.query( PATTERN_TABLE, new String[]{"name"}, null, null, null, null, null );
		
		if (cur == null)
		{
			return 0;
		}
		
		int count = cur.getCount();
		
		cur.close();
		
		return count;
	}
	/** 
	* query all the filter strings
	* @param filters a String array to hold the filter strings returned
	* @return total number of the filters actually got
	*/ 
	public int getSmsFilters( String filters[])
	{
		Cursor cur = null;
		
		try
		{
			cur = m_db.query( PATTERN_TABLE, new String[]{"name"}, null, null, null, null, null );
		
			if (cur == null)
			{
				return 0;
			}
			
			cur.moveToFirst();
			int col_idx = cur.getColumnIndex( "name"), i = 0;
			for( ; i < filters.length && !cur.isAfterLast(); i++, cur.moveToNext() )
			{
				filters[i] = cur.getString( col_idx);
			}
			
			cur.close();
			
			return i;
		}
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			
			e.printStackTrace();
			
			return 0;
		}
	}	
	String[] getReservedSettingOptions()
	{
		String options[] = new String[2];
		
		options[0] = new String( BLIST_ID_OPTION );
		options[1] = new String( ACTIVE_SCENE_OPTION );
		return options;
	}
	public boolean writeSettingString( String option_name, String option_value )
	{	
		if (option_name.equals(BLIST_ID_OPTION)	|| option_name.equals(ACTIVE_SCENE_OPTION)) {
			// conflict with keywords
			return false;
		}
		
		Cursor cur = null;
		
		try 
		{
			String where_str = "option='" + option_name + "'";
			cur = m_db.query(SETTING_TABLE, new String[] { "value" }, where_str, null, null, null, null);
			
			if (cur == null)
			{
				return false;
			}
			
			cur.moveToFirst();
			ContentValues args = new ContentValues();
			
			if (cur.getCount() == 0) {
				args.put("option", option_name);
				args.put("value", option_value );
				m_db.insert(SETTING_TABLE, null, args);
			}
			else
			{
				args.put("value", option_value );
				m_db.update(SETTING_TABLE, args, where_str, null);
				
			}
			
			cur.close();
		} 
		catch (Exception e) 
		{
			if (cur != null)
			{
				cur.close();
			}
			return false;
		}
		
		return true;
	}
	public boolean writeSetting( String option_name, boolean option_value )
	{	
		if( option_name.equals( BLIST_ID_OPTION ) || option_name.equals(ACTIVE_SCENE_OPTION))
		{	//conflict with keywords
			return false;
		}
		Cursor cur = null;
		try 
		{
			String where_str = "option='" + option_name + "'";
			cur = m_db.query(SETTING_TABLE, new String[] { "value" }, where_str, null, null, null, null);
			
			if (cur == null)
			{
				return false;
			}
			
			cur.moveToFirst();
			ContentValues args = new ContentValues();
			
			if (cur.getCount() == 0) 
			{
				args.put("option", option_name);
				args.put("value", option_value ? "1" : "0");
				m_db.insert(SETTING_TABLE, null, args);
			} 
			else 
			{
				args.put("value", option_value ? "1" : "0");
				m_db.update(SETTING_TABLE, args, where_str, null);
			}
			
			cur.close();
		} 
		catch (Exception e) 
		{
			if (cur != null)
			{
				cur.close();
			}
			return false;
		}
		return true;
	}
	public String readSettingString( String option_name )
	{	
		Cursor cur = null;
		
		try
		{
			String where_str = "option='" + option_name + "'";
			cur = m_db.query( SETTING_TABLE, new String[]{"value"}, where_str, null, null, null, null );
		
			if (cur == null)
			{
				return null;
			}
			
			cur.moveToFirst();
			
			if( cur.getCount() == 0 ) 
			{ 
				cur.close();
				return null;
			}
			
			int idx = cur.getColumnIndex( "value" );
			
			String value = cur.getString( idx );
			cur.close();
			
			return value;
		}
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			
			e.printStackTrace();
			
			return null;
		}
	}
	public boolean readSetting( String option_name )
	{	
		Cursor cur = null;
		
		try
		{
			String where_str = "option='" + option_name + "'";
			cur = m_db.query( SETTING_TABLE, new String[]{"value"}, where_str, null, null, null, null );
			
			if (cur == null)
			{
				return false;
			}
			
			cur.moveToFirst();
			
			if( cur.getCount() == 0 ) 
			{
				cur.close();
				return false; 
			}
		
			
			int idx = cur.getColumnIndex( "value" );
			String opt_str = cur.getString( idx );
			
			cur.close();
			
			return opt_str.equals( "1");
		}
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			
			e.printStackTrace();
			
			return false;
		}
	}
	
	public boolean writeLog( EventLog log )
	{
		Cursor cur = null;
		try 
		{
			ContentValues args = new ContentValues();
			//long tm = log.getTime().getTime();
			args.put( "time", Long.valueOf(System.currentTimeMillis()) );
			args.put( "number", log.getNumber() );
			args.put( "type", log.getType() );
			args.put( "block_type", log.getBlockType());
			args.put( "action", 0 );
			args.put( "sms_text", log.getType() == EventLog.LOG_TYPE_SMS ? log.getSmsTxt() : null );
			args.put( "contact_name", log.getTagOrName() );
			args.put( "scene_name", log.getSceneOrKeyword() );
			args.put( "reply_sms_text", log.getReplySmsTxt());
			m_db.insert( LOG_TABLE, null, args);
			
			String where_str = "block_type='" + log.getBlockType() + "'";
			int cnt = 0;
			
			cur = m_db.query( LOG_TABLE, null,
					where_str, null, null, null, "time ASC" );
			
			if (cur != null)
			{	
				cur.moveToFirst();
				cnt = cur.getCount();
				int id = cur.getInt(cur.getColumnIndexOrThrow("_id"));
				
				if (cnt > 300)
				{
					m_db.delete(LOG_TABLE, "_id = '" + id + "'", null);
				}
				cur.close();
			}
			
		} 
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			return false;
		}
		return true;
	}
	private String getTypeWhereStr( int Type )
	{		
		if( Type == EventLog.LOG_TYPE_CALL || Type == EventLog.LOG_TYPE_SMS )
		{
			return String.format( "type='%d'", Type );
		}
		else	
		{
			return null;
		}		
	}
	public EventLog[] getLogs( int Type, int Scope, int Block_Type ) //Block_Type == 0 for blacklist, 1 for scene or keyword
	{	
		boolean get_intercepted = (Scope & EventLog.LOG_SCOPE_INTERCEPTED) != 0;
		boolean get_received    = (Scope & EventLog.LOG_SCOPE_RECEIVED) != 0;	
		
		Cursor cur = null;
		
		try
		{
			int log_count = 0;
			String where_str = null;
			final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
			final Uri SMS_INBOX_CONTENT_URI = Uri.withAppendedPath(SMS_CONTENT_URI, "inbox");		
			String SORT_ORDER = "date DESC";
			TreeMap log_sorter = new TreeMap();
			
			if( get_intercepted ){
				where_str = getTypeWhereStr( Type );
				cur = m_db.query( LOG_TABLE, new String[]{"time", "number", "type", "sms_text"},
						where_str, null, null, null, null );
				
				if (cur == null)
				{
					return null;
				}
				
				cur.moveToFirst();
				log_count += cur.getCount();
				
				cur.close();
			}
			
			if( get_received )
			{
				if( (Type & EventLog.LOG_TYPE_CALL) != 0 )
				{		
					cur = m_ctx.getContentResolver().query(CallLog.Calls.CONTENT_URI,
							null, null, null, SORT_ORDER);//CallLog.Calls.DEFAULT_SORT_ORDER);
					
					if (cur == null)
					{
						return null;
					}
					
					cur.moveToFirst();	
					
				}else if( (Type & EventLog.LOG_TYPE_SMS) != 0 )
				{	// "_id", "thread_id", "address", "person", "date", "body"
					cur = m_ctx.getContentResolver().query( SMS_INBOX_CONTENT_URI, 
							null,	null, null, SORT_ORDER );
					cur.moveToFirst();
				}	
				log_count += cur.getCount();
				
				cur.close();
			}
			
			if( log_count == 0 ) { 
				return null; 
			}
	 
			EventLog logs[] = new EventLog[log_count];
			int i = 0, k = 0;
			int idx_date = 0, idx_number = 0, idx_smstext = 0, idx_tag_or_name = 0, idx_scene_or_keyword = 0, idx_reply_sms_txt = 0;
			
			if( get_intercepted )
			{
				//String[][] blist = getBlacklistNumbers();
				
				where_str = getTypeWhereStr( Type );
				cur = m_db.query( LOG_TABLE, new String[]{"time", "number", "type", "sms_text", "contact_name", "scene_name", "reply_sms_text"},
						where_str, null, null, null, null );
				
				if (cur == null)
				{
					return null;
				}
				
				cur.moveToFirst();
				idx_date    = cur.getColumnIndex( "time" );
				idx_number  = cur.getColumnIndex( "number" );
				idx_smstext = cur.getColumnIndex( "sms_text" );
				idx_tag_or_name =  cur.getColumnIndex( "contact_name");
				idx_scene_or_keyword = cur.getColumnIndex( "scene_name");
				idx_reply_sms_txt = cur.getColumnIndex("reply_sms_text");
				k = cur.getCount();
				int j = 0;
				boolean selected = false;
				
				for ( i = 0; i < cur.getCount(); i++ )
				{
					String number = cur.getString( idx_number );
					String contact_name = cur.getString( idx_tag_or_name );
					String scene_name = cur.getString(idx_scene_or_keyword );
					String reply_sms_txt = cur.getString(idx_reply_sms_txt);
					
					number = Delete86String(number);
					
					long time = Long.parseLong( cur.getString( idx_date ) );
					logs[i] = new EventLog( number, Type, -1, time );
					selected = false;
					
					//check for tag if number is in blacklist
					/*for( j = 0; j < blist.length; j++ )
					{
						//if( number.equals( blist[j][0] ) )
						if (isEqualFromLast(number, blist[j][0]))
						{	//the number is in blacklist
							logs[i].setTagOrName(blist[j][3]);
							break;
						}
					}*/
					if ( Block_Type == 0 )
					{ //only get the records blocked by blacklist 
						if ( scene_name == null )
						{
							selected = true;						
						}					
					}
					else if ( Block_Type == 1 )
					{	//only get the records blocked by scene or keyword 
						if( scene_name != null )
						{
							selected = true;
						}
					}
					/*if( logs[i].getTagOrName() == null)
					{	//blocked by scene
						logs[i].setTagOrName( contact_name );
						logs[i].setSceneOrKeyword(scene_name);
					}else
					{	//blocked by black list
						logs[i].setSceneOrKeyword(null);
					}*/
					if ( Type == EventLog.LOG_TYPE_SMS )
					{
						logs[i].setSmsTxt( cur.getString( idx_smstext ) );
					}
					if ( selected ){
						logs[i].setTagOrName(contact_name );
						logs[i].setSceneOrKeyword(scene_name);
						logs[i].setReplySmsTxt(reply_sms_txt);
						log_sorter.put( Long.valueOf( time ), logs[i] );
					}
					cur.moveToNext();
				}
				
				cur.close();
			}	
			
			if ( get_received )
			{
				idx_smstext = -1;
				int idx_type = -1;
				
				if ( Type == EventLog.LOG_TYPE_CALL )
				{	
					cur = m_ctx.getContentResolver().query(CallLog.Calls.CONTENT_URI,
						null, null, null, SORT_ORDER);//CallLog.Calls.DEFAULT_SORT_ORDER);
					
					if (cur == null)
					{
						return null;
					}
					
					cur.moveToFirst();	
					idx_date    = cur.getColumnIndex(CallLog.Calls.DATE );
					idx_number  = cur.getColumnIndex(CallLog.Calls.NUMBER );
					idx_type = cur.getColumnIndex( CallLog.Calls.TYPE );
					idx_smstext = -1;	//mark this index is not applicable
				}
				else if ( Type == EventLog.LOG_TYPE_SMS)
				{	// "_id", "thread_id", "address", "person", "date", "body"
					cur = m_ctx.getContentResolver().query( SMS_INBOX_CONTENT_URI, 
							new String[] { "address", "date", "body", "person" },	null, null, SORT_ORDER ); 
					
					if (cur == null)
					{
						return null;
					}
					
					cur.moveToFirst();				
					idx_date    = cur.getColumnIndex( "date" );
					idx_number  = cur.getColumnIndex( "address" );
					idx_smstext = cur.getColumnIndex( "body" );
					idx_type = -1;				
				}
				
				for ( i = 0; i < cur.getCount() ; i++ )
				{
					String number = cur.getString( idx_number );
					long time = Long.parseLong( cur.getString( idx_date ) );
					int  CallType = -1;
					
					if ( idx_type != -1 )
					{
						CallType = Integer.parseInt( cur.getString( idx_type ));					
					}
					logs[i + k] = new EventLog( number, Type, CallType, time );
					
					if ( idx_smstext != -1 )
					{
						logs[i + k].setSmsTxt( cur.getString( idx_smstext ) );
					}
					
					if (  Type == EventLog.LOG_TYPE_CALL )
					{
						logs[i+k].setTagOrName( cur.getString( cur.getColumnIndex( CallLog.Calls.CACHED_NAME )) );
					}
					else if ( Type == EventLog.LOG_TYPE_SMS)
					{
						if (cur.getString( cur.getColumnIndex("person") ) != null)
						{
							int name_id =  Integer.parseInt( cur.getString( cur.getColumnIndex("person") ) );
							logs[i+k].setTagOrName( getNameById( name_id ) );
						}
					}
					logs[i+k].setSceneOrKeyword(null);
					log_sorter.put( Long.valueOf( time ), logs[i + k] );
					cur.moveToNext();
				}
				
				cur.close();
			}
			
			EventLog ret_logs[] = new EventLog[log_sorter.size()];
			 
	         Iterator iter = log_sorter.values().iterator();
	         i = 0;
	         k = log_sorter.size();
	         
			 while(iter.hasNext())
			 {
				 ret_logs[k - 1 - i] =(EventLog)iter.next();
				 i++;
			}
			return ret_logs;
		}
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			
			e.printStackTrace();
			
			return null;
		}
		
	}	
	
	public Cursor getCallRejectLogCursor(int Block_Type)
	{
		String where_str = "block_type='" + Block_Type + "'";
		
		Cursor cur = m_db.query( LOG_TABLE, null, where_str, null, null, null, "time DESC" );
		
		return cur;
	}
	
	public boolean deleteLog(Cursor cur, int position)
	{
		if (cur == null)
		{
			return false;
		}
		
		boolean bIsExist = cur.moveToPosition(position);
		
		if (!bIsExist)
		{
			return false;
		}
		
		int id = cur.getInt(cur.getColumnIndex("_id"));
		
		m_db.delete(LOG_TABLE, "_id = '" + id + "'", null);
			
		return true;
	}
	
	public void deleteLog( EventLog log )
	{
		String where_str = String.format( "time='%d' and number='%s' and type='%d'",
				log.getTime().getTime(), log.getNumber(), log.getType() );
		if( log.getType() == EventLog.LOG_TYPE_SMS )
		{
			where_str += String.format( " and sms_text='%s'", log.getSmsTxt() );
		}
		m_db.delete( LOG_TABLE, where_str, null );
	}

	public void deleteLogs( int Type )
	{		
		String where_str = getTypeWhereStr( Type );
		m_db.delete( LOG_TABLE, where_str, null );
	}
	
	public void deleteLogs( int Type, int Block_Type )
	{		
		String where_str = getTypeWhereStr( Type );
	
		where_str += " and block_type = '" + Block_Type + "'" ;
		
		m_db.delete( LOG_TABLE, where_str, null );
	}
	
	public void initDatabase()
	{
		boolean first_time = false;
		try {
			FileInputStream fis = new FileInputStream("/data/data/com.symantec.callfirewall/databases/cfw1.db");
			fis.close();
		} catch (Exception e) {
			first_time = true;
		}
		
		m_db = m_ctx.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE,
				null);

		m_db.execSQL("CREATE TABLE IF NOT EXISTS " + SETTING_TABLE
				+ " (option VARCHAR PRIMARY KEY , value VARCHAR);");
		m_db.execSQL("CREATE TABLE IF NOT EXISTS "	+ SCENE_TABLE
						+ " (name VARCHAR PRIMARY KEY , call_action INTEGER, sms_action INTEGER, blist_id INTEGER );");
		m_db.execSQL("CREATE TABLE IF NOT EXISTS " + BLIST_TABLE
				+ " (id INTEGER PRIMARY KEY , name VARCHAR );");
		m_db.execSQL("CREATE TABLE IF NOT EXISTS " + PATTERN_TABLE + " (name VARCHAR PRIMARY KEY );");
		m_db.execSQL("CREATE TABLE IF NOT EXISTS " + LOG_TABLE + 
				" (_id INTEGER PRIMARY KEY, time LONG, number VARCHAR, type INTEGER, block_type INTEGER, action INTEGER, sms_text VARCHAR, contact_name VARCHAR, scene_name VARCHAR, reply_sms_text VARCHAR );");
		
		m_db.execSQL("CREATE TABLE IF NOT EXISTS " + BLACK_LIST_TABLE + 
		" (number VARCHAR PRIMARY KEY, block_call INTEGER, block_sms INTEGER, tag VARCHAR, reply_sms VARCHAR);");
		
		
		m_db.execSQL("CREATE TABLE IF NOT EXISTS "	+ ADV_SCENE_TABLE
				+ " (name VARCHAR PRIMARY KEY , target_group INTEGER, action INTEGER, sms VARCHAR );");

		m_db.execSQL("CREATE TABLE IF NOT EXISTS "	+ CONTACT_RINGTONE
				+ " (id VARCHAR PRIMARY KEY , ringtone VARCHAR );");

		m_db.execSQL("CREATE TABLE IF NOT EXISTS " + ADV_SCENE_SMS + " (name VARCHAR PRIMARY KEY );");

		m_db.execSQL("CREATE TABLE IF NOT EXISTS " + DONT_ASK_TABLE + " (number VARCHAR PRIMARY KEY );");
	}
	
	public void advAddSms( String sms_str )
	{
		if( !find_by_name( ADV_SCENE_SMS, sms_str ) )
		{		
			ContentValues args = new ContentValues();
			args.put( "name", sms_str );
			m_db.insert( ADV_SCENE_SMS, null, args );			
		}
	}
	public void advDeleteSms( String sms_str )
	{
		if( find_by_name( ADV_SCENE_SMS, sms_str ) )
		{	
			sms_str = sms_str.replaceAll("'", "''");	
			m_db.delete( ADV_SCENE_SMS, "name='" + sms_str + "'", null );
		}		
	}
	public boolean advUpdateReplySms( String oldSmsStr, String newSmsStr )
	{
		if(  find_by_name( ADV_SCENE_SMS, oldSmsStr ) )
		{
		//	advDeleteSms( oldSmsStr );
		//	advAddSms( newSmsStr );
			ContentValues args = new ContentValues();
			args.put("name", newSmsStr );

			oldSmsStr = oldSmsStr.replaceAll("'", "''");
			m_db.update(ADV_SCENE_SMS, args, "name='" + oldSmsStr + "'", null);
			return true;
		}else
		{
			return false;
		}
	}
	public String[] advGetAllSms()
	{
		Cursor cur = null;
		
		try
		{
			cur = m_db.query( ADV_SCENE_SMS, new String[]{"name"}, null, null, null, null, null );
			
			if( cur == null )
			{ 
				return null; 
			}
			
			cur.moveToFirst();
			
			if( cur.getCount() != 0 )
			{
				String strs[] = new String[cur.getCount()];
				getNames( ADV_SCENE_SMS, strs );
				
				cur.close();
				
				return strs;
			}else
			{
				cur.close();
				return null;
			}
		}
		catch( Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			return null;
		}
	}
	public String[] advGetAllScenes()
	{
		Cursor cur = null;
		
		try 
		{
			cur = m_db.query(ADV_SCENE_TABLE, new String[] { "name" },
					null, null, null, null, null);
			
			if (cur == null) 
			{ 
				return null;	
			}
			
			cur.moveToFirst();
			if (cur.getCount() != 0) 
			{
				String names[] = new String[cur.getCount()];
				getNames(ADV_SCENE_TABLE, names);
			
				cur.close();
				
				return names;
			} 
			else 
			{
				cur.close();
				return null;
			}
		} 
		catch (Exception e) 
		{
			if (cur != null)
			{
				cur.close();
			}
			return null;
		}
	}
	public boolean advAddNewScene( String scene_name, int target_group, int action, String sms_str )
	{
		if (find_by_name(ADV_SCENE_TABLE, scene_name)) 
		{
			return false;
		}
		
		try
		{
			ContentValues args = new ContentValues();
			args.put("name", scene_name);
			args.put("target_group", target_group);
			args.put("action", 0);
			args.put("sms", sms_str);
			m_db.insert(ADV_SCENE_TABLE, null, args);
		} 
		catch (Exception e) 
		{
			return false;
		}
		
		return true;
	}
	public boolean advUpdateScene( String old_scene_name, String scene_name, int target_group, int action, String sms_str )
	{
		if (!find_by_name( ADV_SCENE_TABLE, old_scene_name))
		{
			return false;
		}
		
		ContentValues args = new ContentValues();
		args.put("name", scene_name);
		args.put("target_group", target_group);
		args.put("action", 0);
		args.put("sms", sms_str);

		old_scene_name = old_scene_name.replaceAll("'", "''");
		m_db.update(ADV_SCENE_TABLE, args, "name='" + old_scene_name + "'",
				null);
		
		return true;
	}
	public boolean advGetSceneInfo( String name, AdvScene info )
	{	
		if( find_by_name( ADV_SCENE_TABLE, name) )
		{	
			Cursor cur = null;
			
			try
			{//target_group INTEGER, action INTEGER, sms VARCHAR
				String where_str = "name='" + name + "'";
				cur = m_db.query( ADV_SCENE_TABLE, 
						new String[] { "target_group", "action", "sms" }, where_str, null, null, null, null );
				
				if (cur == null || cur.getCount() != 1 ) 
				{
					if (cur != null)
					{
						cur.close();
					}
					
					return false;
				} // unexpected
				
				cur.moveToFirst();
				info.m_Name = name;
				info.m_Target = Integer.parseInt( cur.getString(cur.getColumnIndex("target_group")));
				info.m_Action = Integer.parseInt( cur.getString(cur.getColumnIndex("action")));
				info.m_Sms =  cur.getString(cur.getColumnIndex("sms"));
				
				cur.close();
				
				return true;
			}
			catch( Exception e )
			{
				if (cur != null)
				{
					cur.close();
				}
				return false;
			}
		}else
		{
			return false;
		}
	}
	public boolean advDeleteScene( String scene_name )
	{	
		if( find_by_name( ADV_SCENE_TABLE, scene_name) )
		{
			scene_name = scene_name.replaceAll("'", "''");
			m_db.delete( ADV_SCENE_TABLE, "name='" + scene_name + "'", null );
			return true;			
		}
		else
		{
			return true;			
		}
	}
	public void advRenameScene( String old_name, String new_name )
	{
	}
	public int advSetActiveScene( String name )
	{
		return setActiveScene( name );
	}
	public String advGetActiveScene()
	{
		return getActiveScene();
	}
	
	public AdvScene getActiveAdvScene()
	{
		AdvScene ret = new AdvScene();
		ret.m_Sms = "";
		
		Cursor cur = null;
		
		try
		{
		
			String where_str = "option='" + ACTIVE_SCENE_OPTION +"'";
			cur = m_db.query( SETTING_TABLE, new String[]{"value"}, where_str, null, null, null, null );
		
			if( cur == null || cur.getCount() != 1 ) 
			{
				if (cur != null)
				{
					cur.close();
				}
				return null; 
			}	// unexpected
			
			cur.moveToFirst();
			String advSceneName = cur.getString( cur.getColumnIndex( "value") );
			
			cur.close();
			
			where_str = "name='" + advSceneName +"'";
			cur = m_db.query( ADV_SCENE_TABLE, 
					new String[] { "target_group", "action", "sms" }, where_str, null, null, null, null );
			
			if (cur == null || cur.getCount() == 0 ) 
			{
				if (cur != null)
				{
					cur.close();
				}
				
				return null; 
			} // unexpected
			
			cur.moveToFirst();
			
			ret.m_Name = advSceneName;
			ret.m_Target = Integer.parseInt( cur.getString(cur.getColumnIndex("target_group")));
			ret.m_Action = Integer.parseInt( cur.getString(cur.getColumnIndex("action")));
			if (!cur.isNull(cur.getColumnIndex("sms")))
				ret.m_Sms = cur.getString(cur.getColumnIndex("sms"));
			
			
			cur.close();
			
			return ret;
		}
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			
			e.printStackTrace();
			
			return null;
		}
	}
	
	private boolean find_by_name( String table_name, String name )
	{	
		Cursor cur = null;
		
		try
		{
			name = name.replaceAll("'", "''");
			cur = m_db.query( table_name, new String[]{"name"}, "name='" + name + "'", null, null, null, null );
			
			if( cur == null ) 
			{ 
				return false; 
			}
			cur.moveToFirst();
			
			if( cur.getCount() != 0 )
			{
				cur.close();
				return true;
			}
			else
			{
				cur.close();
				return false;
			}
		}catch( Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			return false;
		}		
	}
	private boolean is_scene_existed( String name )
	{	
		return find_by_name( SCENE_TABLE, name );	
	}
	private boolean is_blist_existed( String name )
	{	
		return find_by_name( BLIST_TABLE, name );		
	}
	private boolean is_pattern_existed( String pat_str )
	{	
		return find_by_name( PATTERN_TABLE, pat_str );	
	}
	
	private String getBlistTblByName( String bl_name )
	{
		String blist_tblname = null;
		Cursor cur = null;
		
		try
		{
			cur = m_db.query( BLIST_TABLE, new String[]{"id"}, "name='" + bl_name + "'", null, null, null, null );
			if( cur == null || cur.getCount() == 0 ) 
			{ 
				if (cur != null)
				{
					cur.close();
				}
				return null; 
			}
			
			cur.moveToFirst();
			String id_str = cur.getString( cur.getColumnIndex( "id") );
			blist_tblname= getBlacklistTableName( id_str);
			
			cur.close();
		}catch( Exception e )
		{
			if (cur != null)
			{
				cur.close();
			}
		}
		return blist_tblname;
	}
	
	public String[] blacklistItem( String phoneNum )
	{	
		String[][] blnums = getBlacklistNumbers();
		if (blnums != null)
		{
			for(int i = 0; i < blnums.length; i++) {
				if (cmpNumber(phoneNum, blnums[i][0]))
					return blnums[i];					
			}
		}
		return null;
	}
	
	public boolean isBlacklisted( String phoneNum )
	{	
		return blacklistItem(phoneNum) != null;
	}

	public boolean isContact( String phoneNum )
	{
		Cursor cur = null;
		
		try
		{
			phoneNum = this.Delete86String(phoneNum);
		
			cur = m_ctx.getContentResolver().query(SysCompat.PHONE_URI,
	    		new String[] {SysCompat.COLUMN_PHONE_NUMBER, SysCompat.COLUMN_PHONE_NAME },
	    		null, null, null);
				
		    if (cur != null && cur.getCount() > 0) 
		    {
		    	//int contact_id = -1;
		    	
		    	cur.moveToFirst();	    	
		    	while( !cur.isAfterLast() )
		    	{
		    		String tempNumber = cur.getString(cur.getColumnIndex(SysCompat.COLUMN_PHONE_NUMBER));
		    		tempNumber = PhoneNumberHelpers.removeNonNumbericChar(tempNumber);
		    		tempNumber = this.Delete86String(tempNumber);
		    		
		    		if (cmpNumber(tempNumber, phoneNum))
		    		{
		    			cur.close();
		    			return true;
		    		}	    		
		    		cur.moveToNext();
		    	}
		    }
		    if (cur != null)
		    	cur.close();	
	    	
		    return false;  
		}
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			
			e.printStackTrace();
			
			return false;
		}
	}
	
	public String getContactName( String phoneNum )
	{
		Cursor cur = null;
		
		try
		{
			phoneNum = this.Delete86String(phoneNum);
		
			cur = m_ctx.getContentResolver().query(SysCompat.PHONE_URI,
	    		new String[] {SysCompat.COLUMN_PHONE_NUMBER, SysCompat.COLUMN_PHONE_NAME },
	    		null, null, null);
				
		    if (cur != null && cur.getCount() > 0) 
		    {
		    	//int contact_id = -1;
		    	
		    	cur.moveToFirst();	    	
		    	while( !cur.isAfterLast() )
		    	{
		    		String tempNumber = cur.getString(cur.getColumnIndex(SysCompat.COLUMN_PHONE_NUMBER));
		    		tempNumber = PhoneNumberHelpers.removeNonNumbericChar(tempNumber);
		    		tempNumber = this.Delete86String(tempNumber);
		    		
		    		if (cmpNumber(tempNumber, phoneNum))
		    		{
		    			String ret = null;
		    			ret = cur.getString(cur.getColumnIndex(SysCompat.COLUMN_PHONE_NAME));
		    			cur.close();
		    			return ret;
		    		}	    		
		    		cur.moveToNext();
		    	}
		    }
		    if (cur != null)
		    	cur.close();	
	    	
		    return null;  
		}
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			
			e.printStackTrace();
			
			return null;
		}
	}
	
	public boolean isDontAsk( String phoneNum )
	{	
		Cursor cur = null;
		
		try
		{
			cur = m_db.query( DONT_ASK_TABLE,
				new String[]{"number"},
				null, null, null, null, null );
		    if (cur != null && cur.getCount() > 0) {
		    	cur.moveToFirst();
		    	while( !cur.isAfterLast() )
				{
		    		String num = cur.getString(cur.getColumnIndex("number"));
					if (num != null && (
						phoneNum.indexOf(num) != -1 ||
						num.indexOf(phoneNum) != -1))
					{
		    	        cur.close();
		    			return true;
		    		}
					cur.moveToNext();
				}
		    }
		    if (cur != null)
		    	cur.close();
	        return false;
		}
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			
			e.printStackTrace();
			
			return false;
		}
	}
	
	public void dontAsk( String phoneNum )
	{	
		Cursor cur = null;
		
		try
		{
		
			cur = m_db.query( DONT_ASK_TABLE,
				new String[]{"number"},
				null, null, null, null, null );
		    if (cur != null && cur.getCount() > 0) {
		    	cur.moveToFirst();
		    	while( !cur.isAfterLast() )
				{
		    		String num = cur.getString(cur.getColumnIndex("number"));
					if (num != null && (
						phoneNum.indexOf(num) != -1 ||
						num.indexOf(phoneNum) != -1))
					{
		    	        cur.close();
		    			return;
		    		}
					cur.moveToNext();
				}
		    }
		    if (cur != null)
		    	cur.close();
		    ContentValues args = new ContentValues();
			args.put( "number", phoneNum );
		    m_db.insert(DONT_ASK_TABLE, null, args);
	        return;
		}
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			
			e.printStackTrace();
			
			return;
		}
	}
	
	public void doAsk( String phoneNum )
	{
	    //m_db.delete(DONT_ASK_TABLE, "number = '" + phoneNum + "'", null);
		
		Cursor cursor = null;
		
		try
		{
			cursor = m_db.query(DONT_ASK_TABLE, null, null, null, null, null, null);
		
			if (cursor == null || cursor.getCount() <= 0)
			{
				if (cursor != null)
				{
					cursor.close();
				}
				
				return;
			}
			
			cursor.moveToFirst();
			
			for (int i = 0; i < cursor.getCount(); i++)
			{
				
				if (this.cmpNumber(phoneNum, cursor.getString(cursor.getColumnIndex("number"))))
				{
					m_db.delete(DONT_ASK_TABLE, "number = '" + cursor.getString(cursor.getColumnIndex("number")) + "'", null);
					break;
				}
				cursor.moveToNext();
			}
			cursor.close();
		}
		catch (Exception e)
		{
			if (cursor != null)
			{
				cursor.close();
			}
			
			e.printStackTrace();
		}
		
        return;
	}
	
	public String getNameById(int Contact_id)
	{
		Cursor cursor = m_ctx.getContentResolver().query(Contacts.People.CONTENT_URI,
				                                         new String[] {Contacts.People.NAME},
				                                         "people." + Contacts.People._ID + " = '" + Contact_id + "'", 
				                                         null,
				                                         null);

		if (cursor != null && cursor.getCount() != 0)
		{
			cursor.moveToFirst();
			String name = cursor.getString(cursor.getColumnIndex(Contacts.People.NAME));
			cursor.close();
			return name;
		}
		
		if (cursor != null)
		{
			cursor.close();
		}
		
		return null;
	}
	
	public String getNameByNumber(String number)
	{		
		Cursor cursor = null;
		
		try
		{
		
			cursor = m_ctx.getContentResolver().query(SysCompat.PHONE_URI,
	    		new String[] {SysCompat.COLUMN_PHONE_NUMBER,
							  SysCompat.COLUMN_PHONE_NAME },
	    		null, null, null);
		
			number = this.Delete86String(number);
			number = PhoneNumberHelpers.removeNonNumbericChar(number);
			
			if (cursor != null && cursor.getCount() != 0)
			{
				cursor.moveToFirst();
				while (!cursor.isAfterLast())
				{
					String ContactNum = cursor.getString(
									cursor.getColumnIndex(SysCompat.COLUMN_PHONE_NUMBER));
					ContactNum = PhoneNumberHelpers.removeNonNumbericChar(ContactNum);
					ContactNum = this.Delete86String(ContactNum);
					
					if (this.cmpNumber(number, ContactNum))
					{
						String name = cursor.getString(
									cursor.getColumnIndex(SysCompat.COLUMN_PHONE_NAME));
						cursor.close();
						
						name = name.replace("-", "");
						name = this.Delete86String(name);
						
						if (!name.equals(ContactNum))
						{
							return name;
						}
						else
						{
							return null;
						}
					}
					
					cursor.moveToNext();
				}
				
				
			}
			
			if (cursor != null)
			{
				cursor.close();
			}
			return null;
		}
		catch (Exception e)
		{
			if (cursor != null)
			{
				cursor.close();
			}
			
			e.printStackTrace();
			
			return null;
		}
	}
	
	private String Delete86String(String number)
	{
		int pos = number.indexOf("+86");
		
		if (pos != -1)
		{
			number = number.substring(pos + 3, number.length());
		}
		return number;
	}
	
	private boolean isEqualFromLast(String src, String dest)
	{
		int pos = dest.lastIndexOf(src);
		
		if (pos == -1)
		{
			return false;
		}
		
		if (pos + src.length() == dest.length())
		{
			return true;
		}
		else 
		{
			return false;
		}
	}
	
	public boolean isBlBlockCall(String number)
	{
		Cursor cur = null;
		
		try
		{
			cur = m_db.query(BLACK_LIST_TABLE,
								new String[]{"block_call"},
								"number = '" + number + "'", 
								null, 
								null, 
								null, 
								null );
		
			if (cur == null)
			{
				return false;
			}
			
			cur.moveToFirst();
			
			int blockCallFlag = -1;
			
			blockCallFlag = cur.getInt( cur.getColumnIndex( "block_call" ));
				
			if (blockCallFlag == 1)
			{
				cur.close();
				return true;
			}
			else
			{
				cur.close();
				return false;
			}
		}
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			
			e.printStackTrace();
			
			return false;
		}
		
	}
	
	public boolean isBlBlockSms(String number)
	{
		Cursor cur = null;
		
		try
		{
			cur = m_db.query(BLACK_LIST_TABLE,
								new String[]{"block_sms"},
								"number = '" + number + "'", 
								null, 
								null, 
								null, 
								null );
		
			if (cur == null)
			{
				return false;
			}
			
			cur.moveToFirst();
			
			int blockSmsFlag = -1;
			
			blockSmsFlag = cur.getInt( cur.getColumnIndex( "block_sms" ));
				
			if (blockSmsFlag == 1)
			{
				cur.close();
				return true;
			}
			else
			{
				cur.close();
				return false;
			}
		}
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			e.printStackTrace();
			
			return false;
		}
		
	}
	
	public void removeFromContact(String numberToDelete)
	{
	}
	
	public String getTagByNumber(String phoneNumber)
	{
		Cursor cur = null;
		
		try
		{
		
			cur = m_db.query( BLACK_LIST_TABLE,
								new String[]{"number", "tag"},
								null, 
								null, 
								null, 
								null, 
								null );
				
			if (cur == null || cur.getCount() == 0)
			{
				if (cur != null)
				{
					cur.close();
				}
				
				return null;
			}
	
			cur.moveToFirst();
			
			phoneNumber = this.Delete86String(phoneNumber);
			
			while (!cur.isAfterLast())
			{
				String number = cur.getString(cur.getColumnIndex("number"));
				
				if (cmpNumber(phoneNumber, number))
				{
					String tag = cur.getString(cur.getColumnIndex("tag"));
					cur.close();
					return tag;
				}
				cur.moveToNext();
			}
			cur.close();
			
			
			return null;
		}
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			
			e.printStackTrace();
			
			return null;
		}
	}

	public String readContactRingtone(String option_id )
	{		
		Cursor cur = null;

		try
		{
			String where_str = "id='" + option_id + "'";
			cur = m_db.query(CONTACT_RINGTONE, new String[]{"ringtone"}, where_str, null, null, null, null );
		
			if (cur == null)
			{
				return null;
			}
			
			cur.moveToFirst();
			
			if( cur.getCount() == 0 ) 
			{ 
				cur.close();
				return null;
			}
			
			int idx = cur.getColumnIndex( "ringtone" );
			
			String rington = cur.getString( idx );
			cur.close();
			
			return rington;
		}
		catch (Exception e)
		{
			if (cur != null)
			{
				cur.close();
			}
			
			e.printStackTrace();
			
			return null;
		}
	}
	
	public boolean writeContactRington(String option_id, String option_rington )
	{	
		Cursor cur = null;
		
		try 
		{
			String where_str = "id='" + option_id + "'";
			cur = m_db.query(CONTACT_RINGTONE, new String[] { "ringtone" }, where_str, null, null, null, null);
			
			if (cur == null)
			{
				return false;
			}
			
			cur.moveToFirst();
			ContentValues args = new ContentValues();
			
			if (cur.getCount() == 0) {
				args.put("id", option_id);
				args.put("ringtone", option_rington);
				m_db.insert(CONTACT_RINGTONE, null, args);
			}
			else
			{
				args.put("ringtone", option_rington);
				m_db.update(CONTACT_RINGTONE, args, where_str, null);
				
			}
			
			cur.close();
		} 
		catch (Exception e) 
		{
			if (cur != null)
			{
				cur.close();
			}
			return false;
		}
		
		return true;
	}
	
	public void clearContactRington()
	{		
		m_db.delete(CONTACT_RINGTONE, null, null);
	}	
}

