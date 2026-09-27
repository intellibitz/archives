package intellibitz.intellidroid.task;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import intellibitz.intellidroid.IntellibitzUserAsyncTask;

import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import org.json.JSONObject;

/**
 */
public class GetIpTask extends
        IntellibitzUserAsyncTask {
    private static final String TAG = "GetIpTask";

    private GetIpTaskListener getIpTaskListener;
    private boolean b;

    public GetIpTask(boolean b, String uid, String token,
                     String device, String deviceRef, String url, Context context) {
        super(uid, token, device, deviceRef, url, context);
        this.b = b;
    }

    public void setGetIpTaskListener(GetIpTaskListener getIpTaskListener) {
        this.getIpTaskListener = getIpTaskListener;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        if (!prepareRequest()) {
            backgroundResult = false;
        } else {
            try {
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
            getIpTaskListener.onPostGetIpErrorResponse(response, b);
        }
    }

    @Override
    protected void onCancelled() {
        getIpTaskListener.onPostGetIpErrorResponse(response, b);
        releaseContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        getIpTaskListener.onPostGetIpErrorResponse(response, b);
        releaseContext();
    }

    @Override
    public void onResponse(JSONObject response) {
        getIpTaskListener.onPostGetIpResponse(response, b);
        releaseContext();
    }

    public interface GetIpTaskListener {
        void onPostGetIpResponse(JSONObject response, boolean b);

        void onPostGetIpErrorResponse(JSONObject response, boolean b);

    }

}
