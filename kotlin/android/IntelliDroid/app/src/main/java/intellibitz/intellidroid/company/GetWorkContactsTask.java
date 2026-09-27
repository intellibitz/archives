package intellibitz.intellidroid.company;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.android.volley.VolleyError;
import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import intellibitz.intellidroid.data.ContactItem;
import org.json.JSONObject;

/**
 */
public class GetWorkContactsTask extends
        IntellibitzUserAsyncTask {
    public static final String TAG = "GetWorkContactsTask";

    private GetWorkContactsTaskListener getWorkContactsTaskListener;
    private String companyId;

    public GetWorkContactsTask(String companyId, String uid, String token, String device,
                               String deviceRef, ContactItem user, String url, Context context) {
        super(uid, token, device, deviceRef, url, user, context);
        this.companyId = companyId;
    }

    public void setGetWorkContactsTaskListener(GetWorkContactsTaskListener GetWorkContactsTaskListener) {
        this.getWorkContactsTaskListener = GetWorkContactsTaskListener;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        if (TextUtils.isEmpty(companyId)) {
            Log.e(TAG, "Company name param is NULL - fail");
            return false;
        }
        if (!prepareRequest()) {
            return false;
        }
        try {
            request.put("company_id", companyId);
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
            getWorkContactsTaskListener.onPostGetWorkContactsErrorResponse(response);
        }
/*
{"status":1,"err":"","contacts":[{"name":"bharath","profile_pic":""}]}
  */
    }

    @Override
    protected void onCancelled() {
        getWorkContactsTaskListener.onPostGetWorkContactsErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        getWorkContactsTaskListener.onPostGetWorkContactsErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onResponse(JSONObject response) {
        getWorkContactsTaskListener.onPostGetWorkContactsResponse(response, companyId, getUserItem());
        releaseContext();
    }

    public interface GetWorkContactsTaskListener {
        void onPostGetWorkContactsResponse(JSONObject response, String companyId, ContactItem user);

        void onPostGetWorkContactsErrorResponse(JSONObject response);

    }

}
