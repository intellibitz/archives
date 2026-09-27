package intellibitz.intellidroid.task;

import android.os.AsyncTask;

import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.HttpUrlConnectionParser;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class UploadProfilePicTask extends AsyncTask<File, Void, Boolean> {
    private static final String TAG = "UploadProfilePicTask";
    private UploadProfilePicTaskListener uploadProfilePicTaskListener;
    private String uid;
    private String token;
    private String device = "android";
    private String deviceRef;
    private String url;
    private JSONObject response;
    private String fileName;

    public UploadProfilePicTask(String uid, String token, String device, String deviceRef, String url) {
        this.uid = uid;
        this.token = token;
        this.device = device;
        this.url = url;
        this.deviceRef = deviceRef;
    }

    public void setUploadProfilePicTaskListener(UploadProfilePicTaskListener groupInfoTaskListener) {
        this.uploadProfilePicTaskListener = groupInfoTaskListener;
    }

    @Override
    protected Boolean doInBackground(File... params) {
        if (null == params || 0 == params.length) return null;
        File file = params[0];
        fileName = file.getAbsolutePath();
        String charset = "UTF-8";
        try {
            HttpUrlConnectionParser.MultipartUtility multipart =
                    new HttpUrlConnectionParser.MultipartUtility(url, charset);
            multipart.addFormField(MainApplicationSingleton.DEVICE_PARAM, device);
            multipart.addFormField(MainApplicationSingleton.DEVICE_REF_PARAM, deviceRef);
            multipart.addFormField(MainApplicationSingleton.UID_PARAM, uid);
            multipart.addFormField(MainApplicationSingleton.TOKEN_PARAM, token);
            multipart.addFilePart(MainApplicationSingleton.PROFILE_PIC_PARAM, file, fileName);
            // params comes from the execute() call: params[0] is the url.
            response = multipart.finishAsJSON(); // response from server.
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null != response;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        uploadProfilePicTaskListener.setUploadProfilePicTaskToNull();
        uploadProfilePicTaskListener.onPostUploadProfilePicExecute(response, fileName);
    }

    @Override
    protected void onCancelled() {
        uploadProfilePicTaskListener.setUploadProfilePicTaskToNull();
        uploadProfilePicTaskListener.onPostUploadProfilePicExecuteFail(response, fileName);
    }

    public interface UploadProfilePicTaskListener {
        void onPostUploadProfilePicExecute(JSONObject response, String filename);

        void onPostUploadProfilePicExecuteFail(JSONObject response, String filename);

        void setUploadProfilePicTaskToNull();
    }

}
