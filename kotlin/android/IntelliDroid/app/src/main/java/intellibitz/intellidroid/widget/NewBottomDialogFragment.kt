package intellibitz.intellidroid.widget

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import intellibitz.intellidroid.R

class NewBottomDialogFragment : BottomSheetDialogFragment(), View.OnClickListener {

    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null
    private var contentView: View? = null
    var newBottomDialogListener: NewBottomDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NewBottomDialogListener) {
            newBottomDialogListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_bottom_new, container, false)
        contentView = view
        val view2 = view.findViewById<CoordinatorLayout>(R.id.cl)
        Log.d(TAG, view2.toString())
        bottomSheetBehavior = BottomSheetBehavior.from(view2.findViewById(R.id.new_bottom_dialog))
        val tv1 = view.findViewById<TextView>(R.id.create)
        tv1.setOnClickListener(this)
        val tv2 = view.findViewById<TextView>(R.id.new_chat)
        tv2.setOnClickListener(this)
        val tv3 = view.findViewById<TextView>(R.id.new_group)
        tv3.setOnClickListener(this)
        val tv4 = view.findViewById<TextView>(R.id.new_email)
        tv4.setOnClickListener(this)
        return view
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.create) {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            contentView?.visibility = View.GONE
            newBottomDialogListener?.onNewDialogClose(this)
            return
        }
        if (id == R.id.new_chat) {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            contentView?.visibility = View.GONE
            newBottomDialogListener?.onNewChat(this)
            return
        }
        if (id == R.id.new_group) {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            contentView?.visibility = View.GONE
            newBottomDialogListener?.onNewGroup(this)
            return
        }
        if (id == R.id.new_email) {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            contentView?.visibility = View.GONE
            newBottomDialogListener?.onNewEmail(this)
            return
        }
    }

    interface NewBottomDialogListener {
        fun onNewDialogClose(newBottomDialogFragment: NewBottomDialogFragment)
        fun onNewChat(newBottomDialogFragment: NewBottomDialogFragment)
        fun onNewGroup(newBottomDialogFragment: NewBottomDialogFragment)
        fun onNewEmail(newBottomDialogFragment: NewBottomDialogFragment)
    }

    companion object {
        private const val TAG = "NewBottom"
    }
}
