package com.intellibitz.mobile.dating;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
 
        

public class DatingService extends Service {

    private static int MOOD_NOTIFICATIONS = R.layout.status_bar_notifications;
    private NotificationManager mNM;
    Thread thr;

    @Override
    protected void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent contentIntent = new Intent();
        Intent appIntent = new Intent();
        mNM.notify(MOOD_NOTIFICATIONS, new Notification(this, R.drawable.stat_sample, "Matching service started", System.currentTimeMillis(), "Matching service started", "Matching service started", contentIntent,
                R.drawable.no_picture, getText(R.string.userinfo), appIntent));
        thr = new Thread(null, mTask, "NotifyingService");
        thr.start();
    }
    private Runnable mTask = new Runnable() {

        public void run() {
            try {
                for (;;) {
                    showNotification(R.drawable.stat_happy, R.string.status_bar_chennai);
                    Thread.sleep(3000);
                    showNotification(R.drawable.stat_neutral, R.string.status_bar_coimbatore);
                    Thread.sleep(3000);
                    showNotification(R.drawable.stat_sad, R.string.status_bar_salem);
                    Thread.sleep(3000);
                    showNotification(R.drawable.stat_sad, R.string.status_bar_erode);
                    Thread.sleep(3000);
                    Thread.sleep(3000);
                    showNotification(R.drawable.stat_sad, R.string.status_bar_nellai);
                    Thread.sleep(3000);
                    Thread.sleep(3000);
                    showNotification(R.drawable.stat_sad, R.string.status_bar_madurai);
                    Thread.sleep(3000);
                }
            } catch (Exception ex) {
            }
            DatingService.this.stopSelf();
        }
          };

    @SuppressWarnings("deprecation")
    @Override
    protected void onDestroy() {
        mNM.cancel(MOOD_NOTIFICATIONS);
        thr.stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @SuppressWarnings("deprecation")
    private void showNotification(int moodId, int textId) {

        Intent contentIntent = new Intent();
        Intent appIntent = new Intent();
        CharSequence text = getText(textId);
        mNM.notify(MOOD_NOTIFICATIONS, new Notification(this, moodId, null, System.currentTimeMillis(),
                getText(R.string.status_bar_notifications_mood_title), text, contentIntent,
                R.drawable.no_picture, getText(R.string.userinfo), appIntent));
        thr.stop();
    }
    private final IBinder mBinder = new Binder() {

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
            return super.onTransact(code, data, reply, flags);
        }
    };
}
