package com.l3cache.snapshop.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.l3cache.snapshop.R;
import com.l3cache.snapshop.R.drawable;
import com.l3cache.snapshop.SnapConstants;
import com.l3cache.snapshop.login.LoginView;

public class GcmIntentService extends IntentService {
	private final String TAG = GcmIntentService.class.getSimpleName();
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	Context mContext;

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				sendNotification("Deleted messages on server: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				sendNotification("New Awesome Snap Arrived!");
				Log.i(TAG, "Received: " + extras.toString());
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, SnapConstants.REQUEST_UPLOAD, new Intent(this,
				LoginView.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("SnapShop").setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

}