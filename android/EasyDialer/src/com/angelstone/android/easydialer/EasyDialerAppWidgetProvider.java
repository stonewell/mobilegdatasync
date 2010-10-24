package com.angelstone.android.easydialer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

public class EasyDialerAppWidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int N = appWidgetIds.length;

		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];

			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.twelve_key_dialer_widget);

			setClickIntent(context, views, Constants.ACTION_DIAL_PAD, appWidgetId,
					R.id.one);
			setClickIntent(context, views, Constants.ACTION_DIAL_PAD, appWidgetId,
					R.id.two);
			setClickIntent(context, views, Constants.ACTION_DIAL_PAD, appWidgetId,
					R.id.three);
			setClickIntent(context, views, Constants.ACTION_DIAL_PAD, appWidgetId,
					R.id.four);
			setClickIntent(context, views, Constants.ACTION_DIAL_PAD, appWidgetId,
					R.id.five);
			setClickIntent(context, views, Constants.ACTION_DIAL_PAD, appWidgetId,
					R.id.six);
			setClickIntent(context, views, Constants.ACTION_DIAL_PAD, appWidgetId,
					R.id.seven);
			setClickIntent(context, views, Constants.ACTION_DIAL_PAD, appWidgetId,
					R.id.eight);
			setClickIntent(context, views, Constants.ACTION_DIAL_PAD, appWidgetId,
					R.id.nine);
			setClickIntent(context, views, Constants.ACTION_DIAL_PAD, appWidgetId,
					R.id.zero);
			setClickIntent(context, views, Constants.ACTION_DIAL_PAD, appWidgetId,
					R.id.star);
			setClickIntent(context, views, Constants.ACTION_DIAL_PAD, appWidgetId,
					R.id.pound);

			setClickIntent(context, views, Constants.ACTION_SPEED_DIAL, appWidgetId,
					R.id.speed_one);
			setClickIntent(context, views, Constants.ACTION_SPEED_DIAL, appWidgetId,
					R.id.speed_two);
			setClickIntent(context, views, Constants.ACTION_SPEED_DIAL, appWidgetId,
					R.id.speed_three);
			setClickIntent(context, views, Constants.ACTION_SPEED_DIAL, appWidgetId,
					R.id.speed_four);
			setClickIntent(context, views, Constants.ACTION_SPEED_DIAL, appWidgetId,
					R.id.speed_five);
			setClickIntent(context, views, Constants.ACTION_SPEED_DIAL, appWidgetId,
					R.id.speed_six);

			setClickIntent(context, views, Constants.ACTION_VOICE_MAIL, appWidgetId,
					R.id.voicemailButton);
			setClickIntent(context, views, Constants.ACTION_DELTE, appWidgetId,
					R.id.deleteButton);
			setClickIntent(context, views, Constants.ACTION_DIAL, appWidgetId,
					R.id.dialButton);

			if (TextUtils.isEmpty(EasyDialerService.mTextViewHelper)) {
				views.setViewVisibility(R.id.digits, View.GONE);
				views.setViewVisibility(R.id.digits_empty, View.VISIBLE);
			} else {
				views.setViewVisibility(R.id.digits, View.VISIBLE);
				views.setTextViewText(R.id.digits, EasyDialerService.mTextViewHelper);
				views.setViewVisibility(R.id.digits_empty, View.GONE);
			}

			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	private void setClickIntent(Context context, RemoteViews views,
			String action, int appWidgetId, int viewId) {
		Intent intent = new Intent(context, EasyDialerAppWidgetEventsReceiver.class);
		intent.putExtra(Constants.EXTRA_WIDGET_ID, appWidgetId);
		intent.putExtra(Constants.EXTRA_VIEW_ID, viewId);
		intent.setAction(action + "#" + viewId + "#" + appWidgetId);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				intent, 0);
		views.setOnClickPendingIntent(viewId, pendingIntent);
	}

}
