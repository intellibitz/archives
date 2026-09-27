package intellibitz.intellidroid.domain.help;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class IntroScreenFragment extends Fragment {

    final static String LAYOUT_ID = "layoutId";

    public static IntroScreenFragment newInstance(int layoutId) {
        IntroScreenFragment pane = new IntroScreenFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(LAYOUT_ID, layoutId);
        pane.setArguments(bundle);
        return pane;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(getArguments().getInt(LAYOUT_ID, -1), container, false);
        return root;
    }
}