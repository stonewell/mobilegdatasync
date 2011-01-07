package com.angelstone.android.profileswitcher.utils;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Parcel;

import com.angelstone.android.profileswitcher.store.Schedule;
import com.angelstone.android.utils.DaysOfWeek;

public class Alarms {

	// This action triggers the AlarmReceiver as well as the AlarmKlaxon. It
	// is a public action used in the manifest for receiving Alarm broadcasts
	// from the alarm manager.
	public static final String ALARM_ALERT_ACTION = "com.angelstone.android.alarm.ALARM_ALERT";

	// A public action sent by AlarmKlaxon when the alarm has stopped sounding
	// for any reason (e.g. because it has been dismissed from
	// AlarmAlertFullScreen,
	// or killed due to an incoming phone call, etc).
	public static final String ALARM_DONE_ACTION = "com.angelstone.android.alarm.ALARM_DONE";

	// AlarmAlertFullScreen listens for this broadcast intent, so that other
	// applications
	// can snooze the alarm (after ALARM_ALERT_ACTION and before
	// ALARM_DONE_ACTION).
	public static final String ALARM_SNOOZE_ACTION = "com.angelstone.android.alarm.ALARM_SNOOZE";

	// AlarmAlertFullScreen listens for this broadcast intent, so that other
	// applications
	// can dismiss the alarm (after ALARM_ALERT_ACTION and before
	// ALARM_DONE_ACTION).
	public static final String ALARM_DISMISS_ACTION = "com.angelstone.android.alarm.ALARM_DISMISS";

	// This is a private action used by the AlarmKlaxon to update the UI to
	// show the alarm has been killed.
	public static final String ALARM_KILLED = "alarm_killed";

	// Extra in the ALARM_KILLED intent to indicate to the user how long the
	// alarm played before being killed.
	public static final String ALARM_KILLED_TIMEOUT = "alarm_killed_timeout";

	// This string is used when passing an Alarm object through an intent.
	public static final String ALARM_INTENT_EXTRA = "intent.extra.alarm";

	// This extra is the raw Alarm object data. It is used in the
	// AlarmManagerService to avoid a ClassNotFoundException when filling in
	// the Intent extras.
	public static final String ALARM_RAW_DATA = "intent.extra.alarm_raw";

	// This string is used to identify the alarm id passed to SetAlarm from the
	// list of alarms.
	public static final String ALARM_ID = "alarm_id";

	// Private method to get a more limited set of alarms from the database.
	private static Cursor getFilteredAlarmsCursor(
			ContentResolver contentResolver) {
		return contentResolver.query(Schedule.CONTENT_URI, null,
				Schedule.COLUMN_ENABLE + "=1 AND " + Schedule.COLUMN_START_TIME
						+ ">0", null, null);
	}

	public static void enableAlarm(final Context context, final int id,
			boolean enabled) {
		enableAlarmInternal(context, id, enabled);
		setNextAlert(context);
	}

	private static void enableAlarmInternal(final Context context,
			final int id, boolean enabled) {
		ContentResolver resolver = context.getContentResolver();

		ContentValues values = new ContentValues(2);
		values.put(Schedule.COLUMN_ENABLE, enabled ? 1 : 0);

		resolver.update(ContentUris.withAppendedId(Schedule.CONTENT_URI, id),
				values, null, null);
	}

	private static Alarm calculateNextAlert(final Context context) {
		Alarm alarm = null;
		long minTime = Long.MAX_VALUE;
		long now = System.currentTimeMillis();
		Cursor cursor = getFilteredAlarmsCursor(context.getContentResolver());
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					Alarm a = new Alarm(cursor);
					// A time of 0 indicates this is a repeating alarm, so
					// calculate the time to get the next alert.
					if (a.time == 0) {
						a.time = calculateAlarm(a);
					} else if (a.time < now) {
						// Expired alarm, disable it and move along.
						enableAlarmInternal(context, a, false);
						continue;
					}
					if (a.time < minTime) {
						minTime = a.time;
						alarm = a;
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return alarm;
	}

	/**
	 * Disables non-repeating alarms that have passed. Called at boot.
	 */
	public static void disableExpiredAlarms(final Context context) {
		Cursor cur = getFilteredAlarmsCursor(context.getContentResolver());
		long now = System.currentTimeMillis();

		if (cur.moveToFirst()) {
			do {
				Alarm alarm = new Alarm(cur);
				// A time of 0 means this alarm repeats. If the time is
				// non-zero, check if the time is before now.
				if (alarm.time != 0 && alarm.time < now) {
					enableAlarmInternal(context, alarm, false);
				}
			} while (cur.moveToNext());
		}
		cur.close();
	}

	/**
	 * Called at system startup, on time/timezone change, and whenever the user
	 * changes alarm settings. Activates snooze if set, otherwise loads all
	 * alarms, activates next alert.
	 */
	public static void setNextAlert(final Context context) {
		Alarm alarm = calculateNextAlert(context);
		if (alarm != null) {
			enableAlert(context, alarm, alarm.time);
		} else {
			disableAlert(context);
		}
	}

	/**
	 * Sets alert in AlarmManger and StatusBar. This is what will actually
	 * launch the alert when the alarm triggers.
	 * 
	 * @param alarm
	 *            Alarm.
	 * @param atTimeInMillis
	 *            milliseconds since epoch
	 */
	private static void enableAlert(Context context, final Alarm alarm,
			final long atTimeInMillis) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(ALARM_ALERT_ACTION);

		// XXX: This is a slight hack to avoid an exception in the remote
		// AlarmManagerService process. The AlarmManager adds extra data to
		// this Intent which causes it to inflate. Since the remote process
		// does not know about the Alarm class, it throws a
		// ClassNotFoundException.
		//
		// To avoid this, we marshall the data ourselves and then parcel a plain
		// byte[] array. The AlarmReceiver class knows to build the Alarm
		// object from the byte[] array.
		Parcel out = Parcel.obtain();
		alarm.writeToParcel(out, 0);
		out.setDataPosition(0);
		intent.putExtra(ALARM_RAW_DATA, out.marshall());

		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		am.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, sender);
	}

	/**
	 * Disables alert in AlarmManger and StatusBar.
	 * 
	 * @param id
	 *            Alarm ID.
	 */
	public static void disableAlert(Context context) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0,
				new Intent(ALARM_ALERT_ACTION),
				PendingIntent.FLAG_CANCEL_CURRENT);
		am.cancel(sender);
	}

	private static long calculateAlarm(Alarm alarm) {
		return calculateAlarm(alarm.hour, alarm.minutes, alarm.daysOfWeek)
				.getTimeInMillis();
	}

	/**
	 * Given an alarm in hours and minutes, return a time suitable for setting
	 * in AlarmManager.
	 */
	private static Calendar calculateAlarm(int hour, int minute, DaysOfWeek daysOfWeek) {

		// start with now
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());

		int nowHour = c.get(Calendar.HOUR_OF_DAY);
		int nowMinute = c.get(Calendar.MINUTE);

		// if alarm is behind current time, advance one day
		if (hour < nowHour || hour == nowHour && minute <= nowMinute) {
			c.add(Calendar.DAY_OF_YEAR, 1);
		}
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		int addDays = daysOfWeek.getNextAlarm(c);
		if (addDays > 0)
			c.add(Calendar.DAY_OF_WEEK, addDays);
		return c;
	}

	private static void enableAlarmInternal(final Context context,
			final Alarm a, boolean enabled) {
		enableAlarmInternal(context, a.id, enabled);
	}
}
