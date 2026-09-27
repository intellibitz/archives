package intellibitz.intellidroid.company;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.android.volley.VolleyError;
import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.IntellibitzUserAsyncTask;
import intellibitz.intellidroid.data.ContactItem;
import org.json.JSONObject;

/**
 */
public class CompanyCreateTask extends
        IntellibitzUserAsyncTask {
    public static final String TAG = "CompCreateTask";

    private CompanyCreateTaskListener companyCreateTaskListener;
    private String companyName;

    public CompanyCreateTask(String companyName, String uid, String token, String device,
                             String deviceRef, ContactItem user, String url, Context context) {
        super(uid, token, device, deviceRef, url, user, context);
        this.companyName = companyName;
    }

    public void setCompanyCreateTaskListener(CompanyCreateTaskListener CompanyCreateTaskListener) {
        this.companyCreateTaskListener = CompanyCreateTaskListener;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        if (!prepareRequest()) {
            return false;
        }
        if (TextUtils.isEmpty(companyName)) {
            Log.e(TAG, "Company name param is NULL - fail");
            return false;
        }
        try {
            request.put(MainApplicationSingleton.COMPANY_NAME_PARAM, companyName);
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
  "company_name": "Demo Company"
}
         */
    }

    @Override
    protected void onPostExecute(final Object result) {
        super.onPostExecute(result);
        if (success) {
            Log.d(TAG, "onPostExecute: success!");
        } else {
            companyCreateTaskListener.onPostCompanyCreateErrorResponse(response);
        }
/*
{
  "status": 0,
  "err": "string",
  "company_id": "string"
}
         */
    }

    @Override
    protected void onCancelled() {
        companyCreateTaskListener.onPostCompanyCreateErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        companyCreateTaskListener.onPostCompanyCreateErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onResponse(JSONObject response) {
        companyCreateTaskListener.onPostCompanyCreateResponse(response, companyName, getUserItem());
        releaseContext();
    }

    public interface CompanyCreateTaskListener {
        void onPostCompanyCreateResponse(JSONObject response, String companyName, ContactItem user);

        void onPostCompanyCreateErrorResponse(JSONObject response);

    }

}
