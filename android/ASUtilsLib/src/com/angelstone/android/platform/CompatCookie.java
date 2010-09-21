package com.angelstone.android.platform;

import android.content.Context;
import android.media.AudioManager;

public class CompatCookie extends CompatEclair
{

	public CompatCookie(Context ctx)
	{
		super(ctx);
		// TODO Auto-generated constructor stub
	}
	
	public boolean SetRingerSilence(AudioManager audioManager, boolean beforeQueryAction)
	{
		if (!beforeQueryAction)
		{
			audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			
			return true;
		}
		
		return false;
	}

	

}
