package intellibitz.intellidroid.widget

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.util.Rfc822Token
import android.text.util.Rfc822Tokenizer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.MultiAutoCompleteTextView
import android.widget.SimpleAdapter
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import intellibitz.intellidroid.IntellibitzPermissionFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.data.ContactItem
import intellibitz.intellidroid.data.MessageItem
import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap

class NewGroupBroadcastDialogFragment : BottomSheetDialogFragment() {

    private var sub: EditText? = null
    private var to: MultiAutoCompleteTextView? = null
    private var cc: MultiAutoCompleteTextView? = null
    private var bcc: MultiAutoCompleteTextView? = null
    private var item: Int = 0
    private var user: ContactItem? = null
    private var messageItem: MessageItem? = null
    var isChatMode: Boolean = false
        private set
    private var tvIcon: TextView? = null
    private var snackView: View? = null
    private var snackbar: Snackbar? = null
    private var btnPermission: Button? = null
    private var viewPermission: View? = null
    private var contentView: View? = null
    var profilePic: File? = null
        private set
    private var mListener: OnNewGroupDialogFragmentListener? = null

    val subject: String
        get() = sub?.text?.toString() ?: ""

    fun getTo(): Array<Rfc822Token> {
        return Rfc822Tokenizer.tokenize(to?.text?.toString() ?: "")
    }

    fun getCc(): Array<Rfc822Token> {
        return Rfc822Tokenizer.tokenize(cc?.text?.toString() ?: "")
    }

    fun getBcc(): Array<Rfc822Token> {
        return Rfc822Tokenizer.tokenize(bcc?.text?.toString() ?: "")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val arguments = arguments
        if (arguments != null) {
            user = arguments.getParcelable(ContactItem.USER_CONTACT)
            item = arguments.getInt("item")
            messageItem = arguments.getParcelable(MessageItem.TAG)
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.menu_title_new_group)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_group_new, null)
        contentView = view

        tvIcon = view.findViewById(R.id.tv_icon)
        viewPermission = view.findViewById(R.id.ll_perm)
        btnPermission = view.findViewById(R.id.btn_perm)
        btnPermission?.setOnClickListener {
            val snack = getSnackView()
            val act = activity
            if (snack != null && act != null) {
                requestMediaPermissions(snack, act)
            }
        }
        getSnackView()

        val sw = view.findViewById<Switch>(R.id.sw_message)
        sw.setOnClickListener { v ->
            val checked = (v as Switch).isChecked
            if (checked) {
                v.setText(R.string.menu_title_chat)
                setChatMode()
            } else {
                v.setText(R.string.broadcast)
                setEmailMode()
            }
        }

        sub = view.findViewById(R.id.new_email_sub)
        to = view.findViewById(R.id.new_email_to)
        cc = view.findViewById(R.id.new_email_cc)
        bcc = view.findViewById(R.id.new_email_bcc)

        val msgItem = messageItem
        if (msgItem != null) {
            sub?.setText(msgItem.subject)
            to?.setText(msgItem.to)
            cc?.setText(msgItem.cc)
            bcc?.setText(msgItem.bcc)
        }

        if (2 == item) {
            setChatMode()
        } else if (1 == item) {
            sw.visibility = View.GONE
            setEmailMode()
        } else if (0 == item) {
            sw.visibility = View.VISIBLE
            sw.isChecked = false
            setEmailMode()
        }

        builder.setView(view)
            .setPositiveButton(R.string.menu_title_ok) { _, _ ->
                mListener?.onDialogPositiveClick(this@NewGroupBroadcastDialogFragment)
            }
            .setNegativeButton(R.string.menu_title_cancel) { _, _ ->
                mListener?.onDialogNegativeClick(this@NewGroupBroadcastDialogFragment)
            }

        sw.text = "CHAT"
        sw.isChecked = true
        setChatMode()

        tvIcon?.let { setupMediaChooserOnPermissions(it) }

        return builder.create()
    }

    fun getSnackView(): View? {
        if (null == snackView) {
            snackView = contentView?.findViewById(R.id.mr_art)
        }
        return snackView
    }

    private fun setEmailMode() {
        tvIcon?.visibility = View.GONE
        cc?.visibility = View.VISIBLE
        bcc?.visibility = View.VISIBLE
        isChatMode = false
        to?.setTokenizer(Rfc822Tokenizer())
        cc?.setTokenizer(Rfc822Tokenizer())
        bcc?.setTokenizer(Rfc822Tokenizer())
        SetupEmailAutoCompleteTask().execute()
    }

    private fun setChatMode() {
        tvIcon?.visibility = View.VISIBLE
        cc?.visibility = View.GONE
        bcc?.visibility = View.GONE
        isChatMode = true
        to?.setTokenizer(Rfc822Tokenizer())
        cc?.setTokenizer(Rfc822Tokenizer())
        bcc?.setTokenizer(Rfc822Tokenizer())
        SetupPhoneAutoCompleteTask().execute()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnNewGroupDialogFragmentListener) {
            addNewEmailDialogListener(context)
        }
    }

    private fun setupMediaChooserOnPermissions(view: View) {
        setupMediaChooser(view)
        if (isCameraStoragePermissionsGranted()) {
            viewPermission?.visibility = View.GONE
        } else {
            val snack = snackView
            val act = activity
            if (snack != null && act != null) {
                requestMediaPermissions(snack, act)
            }
        }
    }

    private fun isCameraStoragePermissionsGranted(): Boolean {
        val ctx = context ?: return false
        return IntellibitzPermissionFragment.isCameraPermissionGranted(ctx) &&
                IntellibitzPermissionFragment.isReadPhoneStatePermissionGranted(ctx) &&
                IntellibitzPermissionFragment.isWriteExternalStoragePermissionGranted(ctx)
    }

    private fun requestMediaPermissions(view: View, activity: Activity) {
        IntellibitzPermissionFragment.mayRequestCamera(view, activity)
        IntellibitzPermissionFragment.mayRequestReadExternalStorage(view, activity)
        IntellibitzPermissionFragment.mayRequestWriteExternalStorage(view, activity)
    }

    private fun setupMediaChooser(view: View) {
        view.setOnClickListener {
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun displayImage(file: String?) {
        if (!file.isNullOrEmpty()) {
            Log.d(TAG, "Profile image change: $file")
            val bitmap = BitmapFactory.decodeFile(file)
            val drawable = BitmapDrawable(resources, bitmap)
            drawable.setBounds(Rect(0, 0, 60, 60))
            tvIcon?.setCompoundDrawables(null, drawable, null, null)
        }
    }

    fun addNewEmailDialogListener(listener: OnNewGroupDialogFragmentListener?) {
        mListener = listener
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun addEmailsToAutoComplete(contacts: List<Map<String, String>>) {
        val context = context ?: return
        val adapter = SimpleAdapter(
            context, contacts,
            android.R.layout.simple_dropdown_item_1line, arrayOf("text1", "text2"),
            intArrayOf(android.R.id.text1, android.R.id.text1)
        )
        adapter.viewBinder = SimpleAdapter.ViewBinder { view, data, _ ->
            if (data == null || "" == data) return@ViewBinder true
            val textView = view as TextView
            textView.setCompoundDrawables(null, null, null, null)
            val photo = data as String
            if (photo.startsWith("content")) {
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        requireContext().contentResolver, Uri.parse(photo)
                    )
                    val drawable = BitmapDrawable(resources, bitmap)
                    drawable.setBounds(Rect(0, 0, 60, 60))
                    textView.setCompoundDrawables(null, null, drawable, null)
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }
                return@ViewBinder true
            }
            false
        }

        to?.setAdapter(adapter)
        cc?.setAdapter(adapter)
        bcc?.setAdapter(adapter)
    }

    private fun addPhonesToAutoComplete(contacts: List<Map<String, String>>) {
        val context = context ?: return
        val adapter = SimpleAdapter(
            context, contacts,
            android.R.layout.simple_dropdown_item_1line, arrayOf("text1", "text2"),
            intArrayOf(android.R.id.text1, android.R.id.text1)
        )
        adapter.viewBinder = SimpleAdapter.ViewBinder { view, data, _ ->
            if (data == null || "" == data) return@ViewBinder true
            val textView = view as TextView
            textView.setCompoundDrawables(null, null, null, null)
            val photo = data as String
            if (photo.startsWith("content")) {
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        requireContext().contentResolver, Uri.parse(photo)
                    )
                    val drawable = BitmapDrawable(resources, bitmap)
                    drawable.setBounds(Rect(0, 0, 60, 60))
                    textView.setCompoundDrawables(null, null, drawable, null)
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }
                return@ViewBinder true
            }
            false
        }

        to?.setAdapter(adapter)
        cc?.setAdapter(adapter)
        bcc?.setAdapter(adapter)
    }

    interface OnNewGroupDialogFragmentListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    internal inner class SetupEmailAutoCompleteTask : AsyncTask<Void?, Void?, List<Map<String, String>>>() {
        override fun doInBackground(vararg voids: Void?): List<Map<String, String>> {
            val contacts = ArrayList<Map<String, String>>()
            val context = context ?: return contacts
            val cr = context.contentResolver
            val cursor = cr.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                null, null, null
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val valStr = cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.DATA)
                    )
                    var photo = cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)
                    )
                    val vals = object : HashMap<String, String>() {
                        override fun toString(): String {
                            return get("text1") ?: ""
                        }
                    }
                    vals["text1"] = valStr
                    if (null == photo) photo = ""
                    vals["text2"] = photo
                    contacts.add(vals)
                }
                cursor.close()
            }
            return contacts
        }

        override fun onPostExecute(emailAddressCollection: List<Map<String, String>>) {
            addEmailsToAutoComplete(emailAddressCollection)
        }
    }

    internal inner class SetupPhoneAutoCompleteTask : AsyncTask<Void?, Void?, List<Map<String, String>>>() {
        override fun doInBackground(vararg voids: Void?): List<Map<String, String>> {
            val contacts = ArrayList<Map<String, String>>()
            val context = context ?: return contacts
            val cr = context.contentResolver
            val cursor = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                null, null, null
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val valStr = cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DATA)
                    )
                    var photo = cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)
                    )
                    val vals = object : HashMap<String, String>() {
                        override fun toString(): String {
                            return get("text1") ?: ""
                        }
                    }
                    vals["text1"] = valStr
                    if (null == photo) photo = ""
                    vals["text2"] = photo
                    contacts.add(vals)
                }
                cursor.close()
            }
            return contacts
        }

        override fun onPostExecute(contacts: List<Map<String, String>>) {
            addPhonesToAutoComplete(contacts)
        }
    }

    companion object {
        private const val TAG = "NewGroupDialog"

        @JvmStatic
        fun newMessageDialog(
            listener: OnNewGroupDialogFragmentListener?,
            item: Int,
            user: ContactItem?,
            messageItem: MessageItem?
        ): NewGroupBroadcastDialogFragment {
            val dialog = NewGroupBroadcastDialogFragment()
            dialog.addNewEmailDialogListener(listener)
            val bundle = Bundle()
            bundle.putParcelable(ContactItem.USER_CONTACT, user)
            bundle.putParcelable(MessageItem.TAG, messageItem)
            bundle.putInt("item", item)
            dialog.arguments = bundle
            return dialog
        }

        @JvmStatic
        fun newMessageDialog(
            listener: OnNewGroupDialogFragmentListener?,
            item: Int,
            user: ContactItem?
        ): NewGroupBroadcastDialogFragment {
            return newMessageDialog(listener, item, user, null)
        }
    }
}
