package intellibitz.intellidroid.task;

import android.os.AsyncTask;
import android.text.TextUtils;

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
public class MobileActivateTask extends AsyncTask<Void, Void, Boolean> {
    private MobileActivateTaskListener mobileActivateTaskListener;
    private String device;
    private String deviceId;
    private String deviceName;
    private String mobile;
    private String otp;
    private String name;
    private String url;
    private JSONObject response;

    public MobileActivateTask(String mobile, String otp, String name,
                              String deviceId, String device, String deviceName, String url) {
        super();
        this.deviceId = deviceId;
        this.device = device;
        this.deviceName = deviceName;
        this.mobile = mobile;
        this.otp = otp;
        this.name = name;
        this.url = url;
    }

    public void setMobileActivateTaskListener(MobileActivateTaskListener mobileActivateTaskListener) {
        this.mobileActivateTaskListener = mobileActivateTaskListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (TextUtils.isEmpty(otp)) return false;
        if (TextUtils.isEmpty(mobile)) return false;
        HashMap<String, String> data = new HashMap<>();
        data.put(MainApplicationSingleton.DEVICE_PARAM, device);
        data.put(MainApplicationSingleton.DEVICE_ID_PARAM, deviceId);
        data.put(MainApplicationSingleton.DEVICE_NAME_PARAM, deviceName);
//            adds the country code to the mobile number
        data.put(MainApplicationSingleton.MOBILE_PARAM, mobile);
        data.put(MainApplicationSingleton.OTP_PARAM, otp);

        if (!TextUtils.isEmpty(name))
            data.put(MainApplicationSingleton.NAME_PARAM, name);
        // params comes from the execute() call: params[0] is the url.
        response = HttpUrlConnectionParser.postHTTP(url, data);
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        mobileActivateTaskListener.setMobileActivateTaskToNull();
        if (success) {
            mobileActivateTaskListener.onPostMobileActivateExecute(response);
        } else {
            mobileActivateTaskListener.onPostMobileActivateExecuteFail(response);
        }
    }

    @Override
    protected void onCancelled() {
        mobileActivateTaskListener.setMobileActivateTaskToNull();
    }

    public interface MobileActivateTaskListener {
        void onPostMobileActivateExecute(JSONObject response);

        void onPostMobileActivateExecuteFail(JSONObject response);

        void setMobileActivateTaskToNull();
    }

}
