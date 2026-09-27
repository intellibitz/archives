package intellibitz.intellidroid.account;

import android.content.Context;
import android.util.Log;
import com.android.volley.VolleyError;
import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import intellibitz.intellidroid.data.ContactItem;
import org.json.JSONObject;

/**
 */
public class GetProfileTask extends
        IntellibitzUserAsyncTask {
    public static final String TAG = "GetProfileTask";

    private GetProfileTaskListener getProfileTaskListener;

    public GetProfileTask(String uid, String token, String device,
                          String deviceRef, ContactItem user, String url, Context context) {
        super(uid, token, device, deviceRef, url, user, context);
    }

    public void setGetProfileTaskListener(GetProfileTaskListener GetProfileTaskListener) {
        this.getProfileTaskListener = GetProfileTaskListener;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        if (!prepareRequest()) {
            return false;
        }
        try {
            if (postJsonObjectRequest()) {
                return true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, TAG + e.toString());
            try {
                response.put("error", e.toString());
            } catch (Throwable e1) {
                e1.printStackTrace();
                Log.e(TAG, TAG + e1.toString());
            }
        }
        return false;
/*
{
}
         */
    }

    @Override
    protected void onPostExecute(final Object result) {
        super.onPostExecute(result);
        if (success) {
            Log.d(TAG, "onPostExecute: success!");
        } else {
            getProfileTaskListener.onPostGetProfileErrorResponse(response);
        }
/*
{
  "status": 0,
  "err": "string",
  "companies": "string"
}
  */
    }

    @Override
    protected void onCancelled() {
        getProfileTaskListener.onPostGetProfileErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        getProfileTaskListener.onPostGetProfileErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onResponse(JSONObject response) {
        if (null == response) getProfileTaskListener.onPostGetProfileErrorResponse(response);
        getProfileTaskListener.onPostGetProfileResponse(response, getUserItem());
        releaseContext();
    }

    public interface GetProfileTaskListener {
        void onPostGetProfileResponse(JSONObject response, ContactItem user);

        void onPostGetProfileErrorResponse(JSONObject response);

    }

}
