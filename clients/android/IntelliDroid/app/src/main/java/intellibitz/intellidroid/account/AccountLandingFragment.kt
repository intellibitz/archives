package intellibitz.intellidroid.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import intellibitz.intellidroid.IntellibitzUserFragment
import intellibitz.intellidroid.R
import intellibitz.intellidroid.data.ContactItem

class AccountLandingFragment : IntellibitzUserFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_accountlanding, container, false)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (null == savedInstanceState) {
            user = arguments?.getParcelable(ContactItem.USER_CONTACT)
        } else {
            user = savedInstanceState.getParcelable(ContactItem.USER_CONTACT)
        }

        val btnGotInvite = view.findViewById<View>(R.id.btn_gotInvite)
        btnGotInvite?.setOnClickListener {
        }
        val txtSignIn = view.findViewById<View>(R.id.txt_signIn)
        txtSignIn?.setOnClickListener {
        }
        val btnCreateTeam = view.findViewById<View>(R.id.btn_newTeam)
        btnCreateTeam?.setOnClickListener {
        }
    }

    companion object {
        private const val TAG = "AcctLandingFrag"

        @JvmStatic
        fun newInstance(user: ContactItem?): AccountLandingFragment {
            val fragment = AccountLandingFragment()
            fragment.user = user
            val args = Bundle()
            args.putParcelable(ContactItem.USER_CONTACT, user)
            fragment.arguments = args
            return fragment
        }
    }
}
