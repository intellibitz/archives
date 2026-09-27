package com.mobeegal.android.service;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.widget.Toast;
import com.mobeegal.android.R;

public class CatalogService
        extends Service
{

    final RemoteCallbackList<ICatalogServiceCallback> mCallbacks
            = new RemoteCallbackList<ICatalogServiceCallback>();

    int mValue = 0;
    NotificationManager mNM;

    @Override
    public void onCreate()
    {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.
        showNotification();

        // While this service is running, it will continually increment a
        // number.  Send the first message that is used to perform the
        // increment.
        mHandler.sendEmptyMessage(REPORT_MSG);
    }

    @Override
    public void onDestroy()
    {
        // Cancel the persistent notification.
        mNM.cancel(R.string.remote_service_started);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.remote_service_stopped,
                Toast.LENGTH_SHORT).show();

        // Unregister all callbacks.
        mCallbacks.kill();

        // Remove the next pending message to increment the counter, stopping
        // the increment loop.
        mHandler.removeMessages(REPORT_MSG);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // Select the interface to return.  If your service only implements
        // a single interface, you can just return it here without checking
        // the Intent.
        if (ICatalogService.class.getName().equals(intent.getAction()))
        {
            return mBinder;
        }
        if (ISecondary.class.getName().equals(intent.getAction()))
        {
            return mSecondaryBinder;
        }
        return null;
    }

    /**
     * The IRemoteInterface is defined through IDL
     */
    private final ICatalogService.Stub mBinder = new ICatalogService.Stub()
    {
        public void registerCallback(ICatalogServiceCallback cb)
        {
            if (cb != null)
            {
                mCallbacks.register(cb);
            }
        }

        public void unregisterCallback(ICatalogServiceCallback cb)
        {
            if (cb != null)
            {
                mCallbacks.unregister(cb);
            }
        }
    };

    /**
     * A secondary interface to the service.
     */
    private final ISecondary.Stub mSecondaryBinder = new ISecondary.Stub()
    {
        public int getPid()
        {
            return Process.myPid();
        }

        public void basicTypes(int anInt, long aLong, boolean aBoolean,
                float aFloat, double aDouble, String aString)
        {
        }
    };
    private static final int REPORT_MSG = 1;
    /**
     * Our Handler used to execute operations on the main thread.  This is used
     * to schedule increments of our value.
     */
    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                // It is time to bump the value!
                case REPORT_MSG:
                {
                    // Up it goes.
                    int value = ++mValue;

                    // Broadcast to all clients the new value.
                    final int N = mCallbacks.beginBroadcast();
                    for (int i = 0; i < N; i++)
                    {
                        try
                        {
                            mCallbacks.getBroadcastItem(i).valueChanged(value);
                        }
                        catch (DeadObjectException e)
                        {
                            // The RemoteCallbackList will take care of removing
                            // the dead object for us.
                        }
                    }
                    mCallbacks.finishBroadcast();

                    // Repeat every 1 second.
                    sendMessageDelayed(obtainMessage(REPORT_MSG), 1 * 1000);
                }
                break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    /**
     * Show a notification while this service is running.
     */
    private void showNotification()
    {
        // This is who should be launched if the user selects our notification.
        Intent contentIntent = new Intent();

        // This is who should be launched if the user selects the app icon in the notification,
        // (in this case, we launch the same activity for both)
        Intent appIntent = new Intent();

        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.remote_service_started);

        mNM.notify(R.string.remote_service_started,
                // we use a string id because it is a unique
                // number.  we use it later to cancel the
                // notification
                new Notification(
                        this,                        // our context
                        R.drawable.stat_sample,
                        // the icon for the status bar
                        text,
                        // the text to display in the ticker
                        System.currentTimeMillis(),
                        // the timestamp for the notification
                        getText(R.string.remote_service_label),
                        // the title for the notification
                        text,
                        // the details to display in the notification
                        contentIntent));                 // the appIntent (see above)
    }
}
