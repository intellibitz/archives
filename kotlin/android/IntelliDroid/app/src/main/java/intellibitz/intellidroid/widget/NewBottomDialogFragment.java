package intellibitz.intellidroid.widget;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import intellibitz.intellidroid.R;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;


/**
 *
 */
public class NewBottomDialogFragment extends
        BottomSheetDialogFragment implements
        View.OnClickListener {

    private static final String TAG = "NewBottom";
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private View view;
    private NewBottomDialogListener newBottomDialogListener;

    public NewBottomDialogFragment() {
        super();

    }

    public void setNewBottomDialogListener(NewBottomDialogListener newBottomDialogListener) {
        this.newBottomDialogListener = newBottomDialogListener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NewBottomDialogListener) {
            newBottomDialogListener = (NewBottomDialogListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_bottom_new, container, false);
        CoordinatorLayout view2 = (CoordinatorLayout) view.findViewById(R.id.cl);
        Log.d(TAG, view2.toString());
        bottomSheetBehavior = BottomSheetBehavior.from(view2.findViewById(R.id.new_bottom_dialog));
        TextView tv1 = (TextView) view.findViewById(R.id.create);
        tv1.setOnClickListener(this);
        TextView tv2 = (TextView) view.findViewById(R.id.new_chat);
        tv2.setOnClickListener(this);
        TextView tv3 = (TextView) view.findViewById(R.id.new_group);
        tv3.setOnClickListener(this);
        TextView tv4 = (TextView) view.findViewById(R.id.new_email);
        tv4.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.create) {
//            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            view.setVisibility(View.GONE);
            if (newBottomDialogListener != null)
                newBottomDialogListener.onNewDialogClose(this);
/*
            Toast.makeText(getActivity(),
                    "BottomSheetDialogFragment click ", Toast.LENGTH_SHORT).show();
*/
            return;
        }
        if (id == R.id.new_chat) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            view.setVisibility(View.GONE);
            if (newBottomDialogListener != null)
                newBottomDialogListener.onNewChat(this);
            return;
        }
        if (id == R.id.new_group) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            view.setVisibility(View.GONE);
            if (newBottomDialogListener != null)
                newBottomDialogListener.onNewGroup(this);
            return;
        }
        if (id == R.id.new_email) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            view.setVisibility(View.GONE);
            if (newBottomDialogListener != null)
                newBottomDialogListener.onNewEmail(this);
            return;
        }
    }

    public interface NewBottomDialogListener {
        void onNewDialogClose(NewBottomDialogFragment newBottomDialogFragment);

        void onNewChat(NewBottomDialogFragment newBottomDialogFragment);

        void onNewGroup(NewBottomDialogFragment newBottomDialogFragment);

        void onNewEmail(NewBottomDialogFragment newBottomDialogFragment);
    }
}
