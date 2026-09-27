package intellibitz.intellidroid.content.task;

import android.content.Context;
import android.os.AsyncTask;

import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.content.UserEmailContentProvider;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.content.UserEmailContentProvider;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 */
public class FetchUserEmailTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "FetchUserEmailTask";
    private final WeakReference<Context> context;
    private final SoftReference<ContactItem> user;
    private FetchUserEmailTaskListener fetchUserEmailTaskListener;

    public FetchUserEmailTask(ContactItem user, Context context) {
        super();
        this.user = new SoftReference<>(user);
        this.context = new WeakReference<>(context);
    }

    public void setFetchUserEmailTaskListener(
            FetchUserEmailTaskListener fetchUserEmailTaskListener) {
        this.fetchUserEmailTaskListener = fetchUserEmailTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (null == user || null == user.get()) return false;
        if (null == context || null == context.get()) return false;
        UserEmailContentProvider.populateUserEmailsJoinByDataId(user.get(), context.get());
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        fetchUserEmailTaskListener.setFetchUserEmailTaskToNull();
        if (success) {
            fetchUserEmailTaskListener.onFetchUserEmailTaskExecute(user.get());
        } else {
            fetchUserEmailTaskListener.onFetchUserEmailTaskExecuteFail(user.get());
        }
    }

    @Override
    protected void onCancelled() {
        fetchUserEmailTaskListener.setFetchUserEmailTaskToNull();
    }

    public interface FetchUserEmailTaskListener {
        void onFetchUserEmailTaskExecute(ContactItem userItem);

        void onFetchUserEmailTaskExecuteFail(ContactItem userItem);

        void setFetchUserEmailTaskToNull();
    }
}
