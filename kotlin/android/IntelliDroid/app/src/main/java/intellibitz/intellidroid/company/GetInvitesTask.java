package intellibitz.intellidroid.company;

import android.content.Context;
import android.util.Log;
import com.android.volley.VolleyError;
import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import intellibitz.intellidroid.data.ContactItem;
import org.json.JSONObject;

/**
 */
public class GetInvitesTask extends
        IntellibitzUserAsyncTask {
    public static final String TAG = "GetInvitesTask";

    private GetInvitesTaskListener getInvitesTaskListener;

    public GetInvitesTask(String uid, String token, String device,
                          String deviceRef, ContactItem user, String url, Context context) {
        super(uid, token, device, deviceRef, url, user, context);
    }

    public void setGetInvitesTaskListener(GetInvitesTaskListener GetInvitesTaskListener) {
        this.getInvitesTaskListener = GetInvitesTaskListener;
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
            getInvitesTaskListener.onPostGetInvitesErrorResponse(response);
        }
/*
{
  "status": 0,
  "err": "string",
  "invites": "string"
}
         */
    }

    @Override
    protected void onCancelled() {
        getInvitesTaskListener.onPostGetInvitesErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        getInvitesTaskListener.onPostGetInvitesErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onResponse(JSONObject response) {
        getInvitesTaskListener.onPostGetInvitesResponse(response, getUserItem());
        releaseContext();
    }

    public interface GetInvitesTaskListener {
        void onPostGetInvitesResponse(JSONObject response, ContactItem user);

        void onPostGetInvitesErrorResponse(JSONObject response);

    }

}
