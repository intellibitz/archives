package intellibitz.intellidroid.task;

import android.os.AsyncTask;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Represents an asynchronous registration task used to authenticate
 * the mobile number.
 */
public class OTPCallTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "OTPCallTask";
    private OTPCallTaskListener otpCallTaskListener;
    private String device;
    private String mobile;
    private String url;
    private JSONObject response;

    public OTPCallTask(String device, String mobile, String url) {
        super();
        this.device = device;
        this.mobile = mobile;
        this.url = url;
    }

    public void setOtpCallTaskListener(OTPCallTaskListener otpCallTaskListener) {
        this.otpCallTaskListener = otpCallTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        HashMap<String, String> data = new HashMap<>();
        data.put(MainApplicationSingleton.DEVICE_PARAM, device);
        data.put(MainApplicationSingleton.MOBILE_PARAM, mobile);
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data);
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        otpCallTaskListener.setOTPCallTaskToNull();
        if (success) {
            otpCallTaskListener.onPostOTPCallExecute(response);
        } else {
            otpCallTaskListener.onPostOTPCallExecuteFail(response);
        }
//        hideProgress();
    }

    @Override
    protected void onCancelled() {
        otpCallTaskListener.setOTPCallTaskToNull();
//        hideProgress();
    }

    public interface OTPCallTaskListener {
        void onPostOTPCallExecute(JSONObject response);

        void onPostOTPCallExecuteFail(JSONObject response);

        void setOTPCallTaskToNull();
    }
}
