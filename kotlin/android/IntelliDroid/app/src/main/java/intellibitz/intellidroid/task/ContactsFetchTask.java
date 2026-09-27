package intellibitz.intellidroid.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;

import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.service.ContactFetch;
import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.service.ContactFetch;


/**
 * Use an AsyncTask to fetch the user's email addresses on a background thread, and update
 * the email text field with results on the main UI thread.
 */
public class ContactsFetchTask extends
        AsyncTask<Object, Object, SparseArray<ContactItem>> {
    private static final String TAG = "ContactsFetchTask";
    private ContactsFetchTaskListener contactsFetchTaskListener;
    private Context context;
    private ContactItem user;
    private int flag = -1;

    public ContactsFetchTask(ContactItem user, int flag, Context applicationContext) {
        super();
        this.user = user;
        this.flag = flag;
        this.context = applicationContext;
    }

    public void setContactsFetchTaskListener(ContactsFetchTaskListener contactsFetchTaskListener) {
        this.contactsFetchTaskListener = contactsFetchTaskListener;
    }

    @Override
    protected SparseArray<ContactItem> doInBackground(Object... voids) {
        return new ContactFetch(context).getDetailedContactList(null);
    }

    @Override
    protected void onPostExecute(SparseArray<ContactItem> contactItems) {
//        contactsFetchTaskListener.setContactsFetchTaskToNull();
        contactsFetchTaskListener.onPostContactsFetchExecute(user, contactItems, flag);
    }

    @Override
    protected void onCancelled() {
        contactsFetchTaskListener.onPostContactsFetchExecuteFail(user, flag);
//        contactsFetchTaskListener.setContactsFetchTaskToNull();
    }

    public interface ContactsFetchTaskListener {
        void onPostContactsFetchExecute(ContactItem user, SparseArray<ContactItem> contactItems, int flag);

        void onPostContactsFetchExecuteFail(ContactItem user, int flag);

//        void setContactsFetchTaskToNull();
    }
}
