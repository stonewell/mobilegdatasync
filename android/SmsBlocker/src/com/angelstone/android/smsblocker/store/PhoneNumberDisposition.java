package com.angelstone.android.smsblocker.store;

/** 
* a class representing how the incoming call or short message of a specific phone number 
* would be disposed.  
* 
* @see com.angelstone.android.smsblocker.store.PhoneNumberDisposition 
*/ 

public class PhoneNumberDisposition {
	public static final int CALL_UNKNOWN     = 0;
	public static final int CALL_ACCEPT      = 1;
	public static final int CALL_REJECT      = 2;	
	public static final int CALL_REJECT_BUSY = 3;
	
	public static final int SMS_UNKNOWN      = 0;
	public static final int SMS_ACCEPT       = 1;
	public static final int SMS_REJECT       = 2;	
	public static final int SMC_REJECT_ERASE = 3;
	
	public 
		PhoneNumberDisposition()
	    {
			m_CallAction = CALL_UNKNOWN;
			m_SmsAction  = SMS_UNKNOWN;
			m_ReplySms   = null;
	    }
		String dump()
 		{
			return String.format( " call action '%d', sms action '%d' ", m_CallAction, m_SmsAction );			
		}
		void setDefaultPass()
		{
			m_CallAction = CALL_ACCEPT;
			m_SmsAction  = SMS_ACCEPT;
		}
	public int     m_CallAction;
	public int     m_SmsAction;
	public String  m_ReplySms;
}
