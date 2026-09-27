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
import org.json.JSONObject;

/**
 */
public class InviteUsersTask extends
        IntellibitzUserAsyncTask {
    public static final String TAG = "InviteUsersTask";

    private InviteUsersTaskListener inviteUsersTaskListener;
    private String invitedCompanyId;
    private String inviteEmails;

    public InviteUsersTask(String inviteEmails, String invitedCompanyId, String uid, String token, String device,
                           String deviceRef, ContactItem user, String url, Context context) {
        super(uid, token, device, deviceRef, url, user, context);
        this.inviteEmails = inviteEmails;
        this.invitedCompanyId = invitedCompanyId;
    }

    public void setInviteUsersTaskListener(InviteUsersTaskListener InviteUsersTaskListener) {
        this.inviteUsersTaskListener = InviteUsersTaskListener;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        if (TextUtils.isEmpty(inviteEmails) || TextUtils.isEmpty(invitedCompanyId) || !prepareRequest()) {
            backgroundResult = false;
            Log.e(TAG, "Invite param is NULL - fail: invited contacts - " +
                    inviteEmails + " : companyId - " + invitedCompanyId);
        } else {
            try {
                request.put(MainApplicationSingleton.INVITED_COMPANY_ID_PARAM, invitedCompanyId);
                request.put(MainApplicationSingleton.INVITE_EMAILS_PARAM, inviteEmails);
                backgroundResult = postJsonObjectRequest();
            } catch (Throwable e) {
                e.printStackTrace();
                Log.e(TAG, TAG + e);
                try {
                    response.put("error", TAG + e);
                } catch (Throwable e1) {
                    e1.printStackTrace();
                    Log.e(TAG, TAG + e1);
                }
                backgroundResult = false;
            }
        }
        return backgroundResult;
/*
{
  "invite_emails": "[\"nishanth@intellibitz.com\",\"sujith@intellibitz.com\"]"
}
         */
    }

    @Override
    protected void onPostExecute(final Object result) {
        super.onPostExecute(result);
        if (success) {
            Log.d(TAG, "onPostExecute: success!");
        } else {
            inviteUsersTaskListener.onPostInviteUsersErrorResponse(response);
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
        inviteUsersTaskListener.onPostInviteUsersErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        super.onErrorResponse(error);
        inviteUsersTaskListener.onPostInviteUsersErrorResponse(response);
        releaseContext();
    }

    @Override
    public void onResponse(JSONObject response) {
        inviteUsersTaskListener.onPostInviteUsersResponse(response, inviteEmails, getUserItem());
        releaseContext();
    }

    public interface InviteUsersTaskListener {
        void onPostInviteUsersResponse(JSONObject response, String companyName, ContactItem user);

        void onPostInviteUsersErrorResponse(JSONObject response);

    }

}
