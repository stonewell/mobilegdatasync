package com.angelstone.android.smsblocker.store;

import com.angelstone.android.smsblocker.store.PhoneNumberDisposition;

/** 
* a class representing a phone-using scene   
* 
* @see com.angelstone.android.smsblocker.store.PhoneNumberDisposition 
*/ 
public class BlackList {
	/** constructor
	 * @param name		the name of the scene
	 * */
	public BlackList( String name )
	{
		m_Name      = name;
		m_NumDisp   = null;
	}
	String dump()
	{
		String txt = String.format( "blacklist: '%s', \n",	m_Name );
		if( m_NumDisp != null )
		{
			for( int i = 0; i < m_NumDisp.length; i++ )
			{
				txt += String.format( "    #%d %s\n", i, m_NumDisp[i].dump() );
			}
		}
		return txt;
	}
	String                 m_Name;
	PhoneNumberDisposition m_NumDisp[];
}
