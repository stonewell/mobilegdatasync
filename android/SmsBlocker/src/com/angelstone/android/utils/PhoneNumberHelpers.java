package com.angelstone.android.utils;


public class PhoneNumberHelpers
{
	public static int mCurrentRingtoneMode = 0;
	public static boolean mIsRingToneModeChgByFwService = false;
	

	public static int CHECK_DISABLE = -1;
	public static int CHECK_ON = 1;
	public static int CHECK_OFF = 0;
	
	
	public static String removeNonNumbericChar(String source)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(source);
		
		for (int i = 0; i < sb.length(); i++)
		{
			if (!Character.isDigit(sb.charAt(i)))
			{
				sb.setCharAt(i, '#');
			}
		}
		
		String temp = sb.toString();
		
		temp = temp.replace("#", "");
		
		return temp;
	}
}
