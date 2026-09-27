package intellibitz.intellidroid.task;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class DeleteMsgsTask extends
        IntellibitzUserAsyncTask {
    private static final String TAG = "DeleteMsgsTask";
    private DeleteMsgsTaskListener deleteMsgsTaskListener;
    private String[] ids;

    public DeleteMsgsTask(String[] ids, String uid, String token,
                          String device, String deviceRef, String url, Context context) {
        super(uid, token, device, deviceRef, url, context);
        this.ids = ids;
    }

    public void setDeleteMsgsTaskListener(DeleteMsgsTaskListener groupInfoTaskListener) {
        this.deleteMsgsTaskListener = groupInfoTaskListener;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        if (!prepareRequest()) {
            return false;
        }
        if (null == ids || 0 == ids.length) {
            Log.e(TAG, "Msgs ids params is NULL - fail");
            return false;
        }
        try {
            JSONArray array = new JSONArray();
            for (String id : ids) {
                array.put(id);
            }
            request.put(MainApplicationSingleton.MSGS_PARAM, array.toString());
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
    }

    @Override
    protected void onPostExecute(final Object result) {
        super.onPostExecute(result);
        if (success) {
            deleteMsgsTaskListener.onPostDeleteMsgsResponse(response, ids);
        } else {
            Log.e(TAG, ":error - " + response);
            deleteMsgsTaskListener.onPostDeleteMsgsErrorResponse(response, ids);
        }
    }

    @Override
    protected void onCancelled() {
        deleteMsgsTaskListener.onPostDeleteMsgsErrorResponse(response, ids);
        releaseContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        deleteMsgsTaskListener.onPostDeleteMsgsErrorResponse(response, ids);
        releaseContext();
    }

    @Override
    public void onResponse(JSONObject response) {
        deleteMsgsTaskListener.onPostDeleteMsgsResponse(response, ids);
        releaseContext();
    }

    public interface DeleteMsgsTaskListener {
        void onPostDeleteMsgsResponse(JSONObject response, String[] item);

        void onPostDeleteMsgsErrorResponse(JSONObject response, String[] item);
    }

}
