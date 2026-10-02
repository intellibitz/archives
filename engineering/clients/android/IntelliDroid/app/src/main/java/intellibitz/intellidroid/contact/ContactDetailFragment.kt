package intellibitz.intellidroid.contact

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import intellibitz.intellidroid.IntellibitzActivityFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.util.MainApplicationSingleton
import intellibitz.intellidroid.util.NetworkImageView
import org.json.JSONException
import java.io.IOException

class ContactDetailFragment : IntellibitzActivityFragment() {

    private var contactItem: ContactItem? = null
    private var user: ContactItem? = null

    fun setContactItem(contactItem: ContactItem?) {
        this.contactItem = contactItem
    }

    fun setUser(user: ContactItem?) {
        this.user = user
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = arguments?.getParcelable(ContactItem.USER_CONTACT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contactdetail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (null == savedInstanceState) {
            user = arguments?.getParcelable(ContactItem.USER_CONTACT)
            contactItem = arguments?.getParcelable(ContactItem.TAG)
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
            contactItem = savedInstanceState.getParcelable(ContactItem.TAG)
        }
        setupAppBar()
        setupContactPic()
        setupContactInfo()
    }

    private fun setupContactInfo() {
        val rootView = view ?: return
        val linearLayout = rootView.findViewById<LinearLayout>(R.id.list_item_contactinfo)
        linearLayout.removeAllViews()

        val item = contactItem ?: return
        val emails = item.emails
        for (i in 0 until emails.length()) {
            try {
                val email = emails.getString(i)
                val view = LayoutInflater.from(context).inflate(
                    R.layout.list_item_contactinfo, linearLayout, false
                )
                val tv = view.findViewById<TextView>(R.id.tv_contactinfo)
                tv.text = email
                val act = activity
                if (act != null) {
                    val draw = ContextCompat.getDrawable(act, R.drawable.ic_email_black_24dp)
                    draw?.bounds = Rect(0, 0, 40, 40)
                    tv.setCompoundDrawables(null, null, draw, null)
                }
                linearLayout.addView(view)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        val phones = item.mobiles
        for (i in 0 until phones.length()) {
            try {
                val phone = phones.getString(i)
                val view = LayoutInflater.from(context).inflate(
                    R.layout.list_item_contactinfo, linearLayout, false
                )
                val tv = view.findViewById<TextView>(R.id.tv_contactinfo)
                tv.text = phone
                val act = activity
                if (act != null) {
                    val draw = ContextCompat.getDrawable(act, R.drawable.phone_icon)
                    draw?.bounds = Rect(0, 0, 40, 40)
                    tv.setCompoundDrawables(null, null, draw, null)
                }
                linearLayout.addView(view)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private fun setupContactPic() {
        try {
            val pic = contactItem?.profilePic
            val view = view
            if (view != null && pic != null) {
                val imageView = view.findViewById<ImageView>(R.id.iv_contact)
                val bitmap = MainApplicationSingleton.getBitmapDecodeAnyUri(pic, context)
                if (bitmap != null) {
                    val croppedBitmap = NetworkImageView.getCroppedBitmap(bitmap, 100)
                    setImageDrawable(imageView, croppedBitmap)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, e.message ?: "")
        }
    }

    private fun setupAppBar() {
        val view = view ?: return
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        tvTitle.text = contactItem?.displayName
        val tvClose = view.findViewById<TextView>(R.id.tv_close)
        tvClose.setOnClickListener { onCancel() }
        compatActivity?.setSupportActionBar(toolbar)
    }

    private fun onCancel() {
        val activity = activity ?: return
        val intent = activity.intent.apply {
            putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
            putExtra(ContactItem.TAG, contactItem as? Parcelable)
        }
        activity.setResult(Activity.RESULT_CANCELED, intent)
        activity.finish()
    }

    companion object {
        private const val TAG = "ContactDetailFRAG"

        @JvmStatic
        fun newInstance(contactItem: ContactItem?, user: ContactItem?): ContactDetailFragment {
            val fragment = ContactDetailFragment().apply {
                setUser(user)
                setContactItem(contactItem)
            }
            val args = Bundle().apply {
                putParcelable(ContactItem.USER_CONTACT, user as? Parcelable)
                putParcelable(ContactItem.TAG, contactItem as? Parcelable)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
