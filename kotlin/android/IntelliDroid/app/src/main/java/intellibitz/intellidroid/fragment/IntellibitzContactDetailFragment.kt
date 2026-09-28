package intellibitz.intellidroid.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import intellibitz.intellidroid.IntellibitzActivity
import intellibitz.intellidroid.R
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.listener.ContactListener

/**
 *
 */
class IntellibitzContactDetailFragment : Fragment() {
    /**
     * The email content this fragment is presenting.
     */
    private var contactItem: ContactItem? = null
    private var user: ContactItem? = null

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    constructor() : super()

    fun setContactItem(contactItem: ContactItem?) {
        this.contactItem = contactItem
    }

    fun setUser(user: ContactItem?) {
        this.user = user
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = arguments!!.getParcelable(ContactItem.USER_CONTACT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.contact_detail, container, false)
        val textView = rootView.findViewById<View>(R.id.contact_detail) as TextView
        // Show the email content as text in a TextView.
        if (contactItem != null) {
            textView.text = contactItem.toString()
        }
        return rootView
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "item_id"

        fun newInstance(
            emailListener: ContactListener?,
            c: IntellibitzActivity?, twoPane: Boolean,
            contactItem: ContactItem?, user: ContactItem?
        ): IntellibitzContactDetailFragment {
            val fragment = IntellibitzContactDetailFragment()
//        fragment.setFragmentContext(c);
//        fragment.setContactMessageHeaderListener((ContactMessageHeaderListener) emailListener);
//        fragment.setContactMessageListener((ContactMessageListener) emailListener);
            fragment.setUser(user)
            fragment.setContactItem(contactItem)
            val args = Bundle()
            args.putBoolean("twoPane", twoPane)
            args.putParcelable(ContactItem.USER_CONTACT, user)
            args.putParcelable(ContactItem.DEVICE_CONTACT, contactItem)
            fragment.arguments = args
            return fragment
        }
    }
}
