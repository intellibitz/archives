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
public class CompanyListTask extends
        IntellibitzUserAsyncTask {
    public static final String TAG = "CompanyListTask";

    private CompanyListTaskListener companyListTaskListener;

    public CompanyListTask(String uid, String token, String device,
                           String deviceRef, ContactItem user, String url, Context context) {
        super(uid, token, device, deviceRef, url, user, context);
    }

    public void setCompanyListTaskListener(CompanyListTaskListener CompanyListTaskListener) {
        this.companyListTaskListener = CompanyListTaskListener;
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
            companyListTaskListener.onPostCompanyListErrorResponse(response);
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
        companyListTaskListener.onPostCompanyListErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        companyListTaskListener.onPostCompanyListErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onResponse(JSONObject response) {
        companyListTaskListener.onPostCompanyListResponse(response, getUserItem());
        releaseContext();
    }

    public interface CompanyListTaskListener {
        void onPostCompanyListResponse(JSONObject response, ContactItem user);

        void onPostCompanyListErrorResponse(JSONObject response);

    }

}
