package intellibitz.intellidroid.task;

import android.os.AsyncTask;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Represents an asynchronous registration task used to authenticate
 * the mobile number.
 */
public class MobileGetCodeTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "CreateGroupTask";
    private MobileGetCodeTaskListener mobileGetCodeTaskListener;
    private String device;
    private String mobile;
    private String url;
    private JSONObject response;

    public MobileGetCodeTask(String device, String mobile, String url) {
        super();
        this.device = device;
        this.mobile = mobile;
        this.url = url;
    }

    public void setMobileGetCodeTaskListener(MobileGetCodeTaskListener mobileGetCodeTaskListener) {
        this.mobileGetCodeTaskListener = mobileGetCodeTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        // TODO: attempt authentication against a network service.

        HashMap<String, String> data = new HashMap<>();
        data.put(MainApplicationSingleton.DEVICE_PARAM, device);
//            normalizeUserMobile();
        data.put(MainApplicationSingleton.MOBILE_PARAM, mobile);
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data);
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        mobileGetCodeTaskListener.setMobileGetCodeTaskToNull();
        if (success) {
            mobileGetCodeTaskListener.onPostMobileGetCodeExecute(response);
        } else {
            mobileGetCodeTaskListener.onPostMobileGetCodeExecuteFail(response);
        }
    }

    @Override
    protected void onCancelled() {
        mobileGetCodeTaskListener.setMobileGetCodeTaskToNull();
    }


    public interface MobileGetCodeTaskListener {
        void onPostMobileGetCodeExecute(JSONObject response);

        void onPostMobileGetCodeExecuteFail(JSONObject response);

        void setMobileGetCodeTaskToNull();
    }
}
