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
public class CompanyJoinTask extends
        IntellibitzUserAsyncTask {
    public static final String TAG = "CompJoinTask";

    private CompanyJoinTaskListener companyJoinTaskListener;
    private String invitedCompanyId;
    private ContactItem contactItem;

    public CompanyJoinTask(String invitedCompanyId, ContactItem contactItem, String uid, String token, String device,
                           String deviceRef, ContactItem user, String url, Context context) {
        super(uid, token, device, deviceRef, url, user, context);
        this.invitedCompanyId = invitedCompanyId;
        this.contactItem = contactItem;
    }

    public void setCompanyJoinTaskListener(CompanyJoinTaskListener CompanyJoinTaskListener) {
        this.companyJoinTaskListener = CompanyJoinTaskListener;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        if (!prepareRequest()) {
            return false;
        }
        if (TextUtils.isEmpty(invitedCompanyId)) {
            Log.e(TAG, "Company name param is NULL - fail");
            return false;
        }
        try {
            request.put(MainApplicationSingleton.INVITED_COMPANY_ID_PARAM, invitedCompanyId);
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
  "invited_company_id": "some-companyName-id"
}
         */
    }

    @Override
    protected void onPostExecute(final Object result) {
        super.onPostExecute(result);
        if (success) {
            Log.d(TAG, "onPostExecute: success!");
        } else {
            companyJoinTaskListener.onPostCompanyJoinErrorResponse(response);
        }
/*
{
  "status": 0,
  "err": "string"
}
         */
    }

    @Override
    protected void onCancelled() {
        companyJoinTaskListener.onPostCompanyJoinErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        companyJoinTaskListener.onPostCompanyJoinErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onResponse(JSONObject response) {
        companyJoinTaskListener.onPostCompanyJoinResponse(response, invitedCompanyId, contactItem, getUserItem());
        releaseContext();
    }

    public interface CompanyJoinTaskListener {
        void onPostCompanyJoinResponse(JSONObject response, String invitedCompanyId, ContactItem contactItem, ContactItem user);

        void onPostCompanyJoinErrorResponse(JSONObject response);

    }

}
