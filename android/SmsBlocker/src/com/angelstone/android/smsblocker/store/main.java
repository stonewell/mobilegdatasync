package com.angelstone.android.smsblocker.store;

import android.widget.TextView; 
import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
//import com.symantec.cfw.db.Scene;

public class main extends Activity {
    /** example of how to use phone number database */
	void showBlacklistTabExample(PhoneNumberManager db) 
	{
		String list[] = null;
		int ret, cnt;

		// 'blacklist' tab example
		ret = db.addBlacklist("blist-01");
		ret = db.addBlacklist("blist-02");
		ret = db.addBlacklist("blist-03");
		ret = db.addBlacklist("blist-03"); // test duplicated add
		ret = db.delBlacklist("blist-02");
		ret = db.blacklistAddNumber("blist-01", "13366363992");
		ret = db.blacklistAddNumber("blist-01", "13311119999");
		ret = db.blacklistAddNumber("blist-03", "13900001111");
		ret = db.blacklistAddNumber("blist-03", "13900002222");
		cnt = db.getBlacklistNumbersCount( "blist-03" );
		ret = db.blacklistAddNumber("blist-01", "13366363992"); // test duplicated number insertion
		ret = db.blacklistAddNumber("blist-02", "1111"); // test insertion to  none-existed blacklist
		ret = db.blacklistDeleteNumber("13900002222");
		ret = db.blacklistDeleteNumber("13900002222"); // test deletion of none-existed number
		cnt = db.getBlacklistNumbersCount( "blist-03" );
		ret = db.blacklistDeleteNumber("1111"); // test deletion from none-existed blacklist
		cnt = db.getBlacklistCount();
		if (cnt > 0) {
			list = new String[cnt];
			ret = db.getBlacklistNames(list);
		}
		cnt = ret; //suppress warning only
	}
	void showCallMgmtTabExample( PhoneNumberManager db )
	{		
/*		String list[] = null;
		String str;
		int ret, cnt;
		
		ret = db.addScene( "meeting");
		ret = db.addScene( "normal");
		ret = db.addScene( "normal"); //test duplicated add
		ret = db.addScene( "holiday");
		ret = db.delScene( "normal");
		ret = db.delScene( "normal");	//test deletion of nonw-existed scene
		str = db.getSceneBlacklist( "meeting");
		ret = db.SetSceneBlacklist( "meeting", "blist-01");
		ret = db.SetSceneBlacklist( "meeting", "blist-03"); //test blacklist bound change
		str = db.getSceneBlacklist( "meeting");
		ret = db.SetSceneBlacklist( "meeting", "aaa");	//test bounding to none-existed blacklist
		cnt = db.getBlacklistRefCount( "blist-03" );
		ret = db.setSmsBlacklist( "blist-03");
		str = db.getSmsBlacklist();
		cnt = db.getBlacklistRefCount( "blist-03" );
		ret = db.delBlacklist( "blist-03");
		str = db.getSceneBlacklist( "meeting");
		str = db.getSmsBlacklist();
		cnt = db.getSceneCount();
		if (cnt > 0) {
			list = new String[cnt];
			ret = db.getSceneNames( list );
		}	
		cnt = ret; //suppress warning only*/
	}
	void showSmsMgmtTabExample( PhoneNumberManager db )
	{	
		String list[] = null;
		int ret, cnt;
		
		//ret = db.setSmsBlacklist( "blist-01");
		ret = db.setSmsBlacklist( "aaa");	//test bounding to none-existed blacklist 
		ret = db.addSmsFilter( "*.exe");
		ret = db.addSmsFilter( "b.a.c");
		ret = db.deleteSmsFilter( "*.exe");
		cnt = db.getSmsFilterCount();
		if (cnt > 0) {
			list = new String[cnt];
			ret = db.getSmsFilters( list );
		}
		cnt = ret; //suppress warning only		
	}
	void showSettingTabExample( PhoneNumberManager db )
	{
		boolean ret = false;
		
		String keywords[] = db.getReservedSettingOptions();
		ret = db.writeSetting( "xxx", true );
		ret = db.writeSetting( "zzzz", false);
		ret = db.readSetting( "xxx");
		ret = db.writeSetting( "xxx", false );
		ret = db.writeSetting( "active_scene", true ); //test conflict with keyword
	}
	void showLogTabExample( PhoneNumberManager db )
	{
		EventLog evt = new EventLog( "1234", EventLog.LOG_TYPE_CALL );
		
		db.writeLog( evt );
		evt = new EventLog( "8888", EventLog.LOG_TYPE_CALL );
		db.writeLog( evt );
		evt = new EventLog( "123456", EventLog.LOG_TYPE_SMS );
		evt.setSmsTxt( "hello");
		db.writeLog( evt );
		
	/*	EventLog logs[] = db.getLogs( EventLog.LOG_TYPE_SMS );
		logs = db.getLogs( EventLog.LOG_TYPE_CALL );
		logs = db.getLogs( 0 );
		db.deleteLog( logs[1]);
		logs = db.getLogs( 0 );
		db.deleteLogs( EventLog.LOG_TYPE_CALL );
		logs = db.getLogs( 0 );
		db.deleteLogs( 0 );
		logs = db.getLogs( 0 );*/		
	}
    @Override
    public void onCreate( Bundle icicle ) {
        super.onCreate(icicle); 
        // We want to view some very simple text, so we need a TextView 
        TextView tv = new TextView(this); 
        PhoneNumberManager db = null;
        
        try {
        	//db = new PhoneNumberManager( (Context)this );	//use a Activity derived class instance as parameter
        	db = PhoneNumberManager.getIntance(this);
        } catch (Exception e) { 
        	tv.setText( "exception occur while creating phone number manager\n");
            setContentView(tv); 
        	return; 
        } 
        showBlacklistTabExample( db );
        showCallMgmtTabExample( db );
        showSmsMgmtTabExample( db );
        showSettingTabExample( db );
        showLogTabExample( db );
        
        setContentView( tv ); 
        String msg = "Reserved setting options:";
        String opts[] = db.getReservedSettingOptions();
        for( int i = 0; i < opts.length; i++ )
        {
        	msg += String.format( " '%s'", opts[i] );
        }
        msg += "\n";
        tv.setText( msg );        
        
        PhoneNumberDisposition disp = new PhoneNumberDisposition();
        db.queryDispostion( "13366363992", disp );        
 
        db.advSetActiveScene( "meeting");        
        db.queryDispostion( "13366363992", disp );
        msg += String.format( "scence set to '%s'\n", db.advGetActiveScene() );
        tv.setText( msg  );
        
        db.SetSceneBlacklist( "meeting", "blist-01");
        db.queryDispostion( "13366363992", disp );
        
        db.advSetActiveScene( "holiday");
        db.queryDispostion( "13366363992", disp );
        
        db.setSmsBlacklist( "blist-01");  
        db.queryDispostion( "13366363992", disp );
        db.queryDispostion( "11223344", disp );
    }
}