package intellibitz.intellidroid.domain.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class IntroScreenFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutId = arguments?.getInt(LAYOUT_ID, -1) ?: -1
        return inflater.inflate(layoutId, container, false)
    }

    companion object {
        const val LAYOUT_ID = "layoutId"

        @JvmStatic
        fun newInstance(layoutId: Int): IntroScreenFragment {
            val pane = IntroScreenFragment()
            val bundle = Bundle()
            bundle.putInt(LAYOUT_ID, layoutId)
            pane.arguments = bundle
            return pane
        }
    }
}
