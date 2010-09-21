package com.angelstone.android.platform;

import android.content.Context;
import android.media.AudioManager;

public class CompatFroyo extends CompatEclair {

	public CompatFroyo(Context ctx) {
		super(ctx);
	}

	public boolean SetRingerSilence(AudioManager audioManager,
			boolean beforeQueryAction) {
		if (!beforeQueryAction) {
			audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

			return true;
		}

		return false;
	}

}
