package com.angelstone.android.profileswitcher.utils;

import java.util.Calendar;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.angelstone.android.profileswitcher.store.Schedule;
import com.angelstone.android.utils.DaysOfWeek;

public final class Alarm implements Parcelable {

	// ////////////////////////////
	// Parcelable apis
	// ////////////////////////////
	public static final Parcelable.Creator<Alarm> CREATOR = new Parcelable.Creator<Alarm>() {
		public Alarm createFromParcel(Parcel p) {
			return new Alarm(p);
		}

		public Alarm[] newArray(int size) {
			return new Alarm[size];
		}
	};

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel p, int flags) {
		p.writeInt(id);
		p.writeInt(enabled ? 1 : 0);
		p.writeInt(hour);
		p.writeInt(minutes);
		p.writeInt(daysOfWeek.getCoded());
		p.writeLong(time);
	}

	// ////////////////////////////
	// end Parcelable apis
	// ////////////////////////////

	// Public fields
	public int id;
	public boolean enabled;
	public int hour;
	public int minutes;
	public DaysOfWeek daysOfWeek;
	public long time;

	public Alarm(Cursor c) {
		id = c.getInt(c.getColumnIndex(Schedule.COLUMN_ID));
		enabled = c.getInt(c.getColumnIndex(Schedule.COLUMN_ENABLE)) == 1;

		time = c.getLong(c.getColumnIndex(Schedule.COLUMN_START_TIME));
		Calendar cTime = Calendar.getInstance();

		cTime.setTimeInMillis(time);

		hour = cTime.get(Calendar.HOUR_OF_DAY);
		minutes = cTime.get(Calendar.MINUTE);

		daysOfWeek = new DaysOfWeek(c.getInt(c
				.getColumnIndex(Schedule.COLUMN_REPEAT_WEEKDAYS)));
		
		if (daysOfWeek.getCoded() > 0) {
			time = 0;
		}
	}

	public Alarm(Parcel p) {
		id = p.readInt();
		enabled = p.readInt() == 1;
		hour = p.readInt();
		minutes = p.readInt();
		daysOfWeek = new DaysOfWeek(p.readInt());
		time = p.readLong();
	}

	// Creates a default alarm at the current time.
	public Alarm() {
		id = -1;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		hour = c.get(Calendar.HOUR_OF_DAY);
		minutes = c.get(Calendar.MINUTE);
		daysOfWeek = new DaysOfWeek(0);
	}
}
