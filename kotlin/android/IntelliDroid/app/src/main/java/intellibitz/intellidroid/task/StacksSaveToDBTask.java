package intellibitz.intellidroid.task;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import intellibitz.intellidroid.data.MessageItem;

import java.util.Collection;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class StacksSaveToDBTask extends AsyncTask<Void, Void, Boolean> {
    private StacksSaveToDBTaskListener nestsSaveToDBTaskListener;
    private Collection<MessageItem> nestItems;
    private Context context;
    private Uri uri;

    public StacksSaveToDBTask(Collection<MessageItem> nestItems, Context context) {
        super();
        this.nestItems = nestItems;
        this.context = context;
    }

    public void setStacksSaveToDBTaskListener(StacksSaveToDBTaskListener nestsSaveToDBTaskListener) {
        this.nestsSaveToDBTaskListener = nestsSaveToDBTaskListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
//        uri = MsgsStackNestContentProvider.saveStacksInDB(nestItems, context);
        return (uri != null);
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        nestsSaveToDBTaskListener.setStacksSaveToDBTaskToNull();
        if (success) {
            nestsSaveToDBTaskListener.onPostStacksSaveToDBExecute(uri, nestItems);
        } else {
            nestsSaveToDBTaskListener.onPostStacksSaveToDBExecuteFail(uri, nestItems);
        }
    }

    @Override
    protected void onCancelled() {
        nestsSaveToDBTaskListener.setStacksSaveToDBTaskToNull();
    }

    public interface StacksSaveToDBTaskListener {
        void onPostStacksSaveToDBExecute(Uri uri, Collection<MessageItem> nestItems);

        void onPostStacksSaveToDBExecuteFail(Uri uri, Collection<MessageItem> nestItems);

        void setStacksSaveToDBTaskToNull();
    }

}
