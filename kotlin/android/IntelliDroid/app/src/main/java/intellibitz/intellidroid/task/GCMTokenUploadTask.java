package intellibitz.intellidroid.task;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.VolleyError;
import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import org.json.JSONObject;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class GCMTokenUploadTask extends
        IntellibitzUserAsyncTask {
    public static final String TAG = "GCMTokenUploadTask";
    private GCMTokenUploadTaskListener gcmTokenUploadTaskListener;
    private String gcm;

    public GCMTokenUploadTask(String gcm, String uid, String token, String device, String deviceRef,
                              String url, ContactItem user, Context context) {
        super(uid, token, device, deviceRef, url, user, context);
        this.gcm = gcm;
    }

    public void setGcmTokenUploadTaskListener(GCMTokenUploadTaskListener contactsUploadTaskListener) {
        this.gcmTokenUploadTaskListener = contactsUploadTaskListener;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        if (TextUtils.isEmpty(gcm) || !prepareRequest()) {
            Log.e(TAG, "GCM param is NULL - fail");
            backgroundResult = false;
        } else {
            try {
                request.put(MainApplicationSingleton.GCM_TOKEN_PARAM, gcm);
                backgroundResult = postJsonObjectRequest();
            } catch (Throwable e) {
                e.printStackTrace();
                Log.e(TAG, TAG + e.toString());
                try {
                    response.put("error", e.toString());
                } catch (Throwable e1) {
                    e1.printStackTrace();
                    Log.e(TAG, TAG + e1.toString());
                }
                backgroundResult = false;
            }
        }
        return backgroundResult;
    }

    @Override
    protected void onPostExecute(final Object result) {
        super.onPostExecute(result);
        if (success) {
            Log.d(TAG, "onPostExecute: success!");
        } else {
            gcmTokenUploadTaskListener.onPostGCMTokenUploadErrorResponse(response);
        }
    }

    @Override
    protected void onCancelled() {
        gcmTokenUploadTaskListener.onPostGCMTokenUploadErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        gcmTokenUploadTaskListener.onPostGCMTokenUploadErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onResponse(JSONObject response) {
        gcmTokenUploadTaskListener.onPostGCMTokenUploadResponse(response, getUserItem());
        releaseContext();
    }

    public interface GCMTokenUploadTaskListener {
        void onPostGCMTokenUploadResponse(JSONObject response, ContactItem user);

        void onPostGCMTokenUploadErrorResponse(JSONObject response);

    }

}
