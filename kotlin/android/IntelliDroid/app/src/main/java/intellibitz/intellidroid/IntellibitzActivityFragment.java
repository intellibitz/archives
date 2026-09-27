package intellibitz.intellidroid;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import intellibitz.intellidroid.listener.ViewModeListener;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import intellibitz.intellidroid.listener.ViewModeListener;


/**
 *
 */
public class IntellibitzActivityFragment extends
        IntellibitzUserFragment implements
        ViewModeListener {
    private static final String TAG = "MainActivityFrag";

    protected IntellibitzActivity mainActivity;
    protected boolean twoPane = false;
    protected VIEW_MODE viewMode = VIEW_MODE.ITEM;
    protected ViewModeListener viewModeListener;

    public IntellibitzActivityFragment() {
        // Required empty public constructor
        super();
    }

    @Override
    public void onViewModeChanged() {
        viewMode = VIEW_MODE.DETAIL;
    }

    @Override
    public void onViewModeItem() {
        viewMode = VIEW_MODE.DETAIL;
    }

    public void setViewModeItem() {
        viewMode = VIEW_MODE.ITEM;
    }

    public void setViewModeListener(ViewModeListener viewModeListener) {
        this.viewModeListener = viewModeListener;
    }

    @Override
    public void onDestroy() {
//        mainActivity = null;
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (null == mainActivity && IntellibitzActivity.class.isInstance(context)) {
            mainActivity = (IntellibitzActivity) context;
        }
    }

    public IntellibitzActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(IntellibitzActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    protected AppCompatActivity getAppCompatActivity() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (null == activity) {
            activity = mainActivity;
        }
        return activity;
    }

    @Nullable
    public IntellibitzActivity removeSelf() {
        return removeSelf(getMainActivity());
    }

    public IntellibitzActivity removeSelf(IntellibitzActivity intellibitzActivity) {
        if (null == intellibitzActivity) {
            Log.e(TAG, "onOkPressed: FAIL - intellibitz activity is NULL");
            return null;
        }
//        intellibitzActivity.hideDetailFilters();
//        mainActivity.removeChildFragment(this);
        View view = getView();
        if (view != null) {
            view.setVisibility(View.GONE);
        }
//        intellibitzActivity.popFragment();
//        intellibitzActivity.removeFragment(this);
        return intellibitzActivity;
    }

    public boolean isTwoPane() {
        return twoPane;
    }

    public void setTwoPane(boolean twoPane) {
        this.twoPane = twoPane;
    }

    public Drawable getDrawable(int id, Resources.Theme theme) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getHost() != null) {
                return getResources().getDrawable(id, theme);
            }
        } else {
            return ContextCompat.getDrawable(getAppCompatActivity(), id);
//            return getResources().getDrawable(id);
        }
        return ContextCompat.getDrawable(getAppCompatActivity(), id);
    }

    public Drawable getDrawable(int id) {
        return getDrawable(id, getAppCompatActivity().getTheme());
/*
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getHost() != null) {
                return getResources().getDrawable(id, getAppCompatActivity().getTheme());
            }
        } else {
            return ContextCompat.getDrawable(getAppCompatActivity(), id);
//            return getResources().getDrawable(id);
        }
        return ContextCompat.getDrawable(getAppCompatActivity(), id);
*/
    }

    public int getColor(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getHost() != null) {
                return getResources().getColor(id, getAppCompatActivity().getTheme());
            }
        } else {
            return ContextCompat.getColor(getAppCompatActivity(), id);
//            return getResources().getDrawable(id);
        }
        return ContextCompat.getColor(getAppCompatActivity(), id);
    }

    public void setCompoundDrawablesRelative(
            TextView textView, Drawable s, Drawable t, Drawable e, Drawable b) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setCompoundDrawablesRelative(s, t, e, b);
        }
    }

    public void setImageDrawable(View view, Bitmap bitmap) {
        if (view instanceof ImageView) {
            BitmapDrawable drawable = getBitmapDrawable(bitmap);
            if (drawable != null) {
                drawable.setBounds(new Rect(0, 0, 100, 100));
                ((ImageView) view).setImageDrawable(drawable);
            }
        }
    }

    public BitmapDrawable getBitmapDrawable(Bitmap bitmap) {
        BitmapDrawable drawable;
        if (getHost() != null) {
            drawable = new BitmapDrawable(getResources(), bitmap);
        } else {
            drawable = new BitmapDrawable(bitmap);
        }
        return drawable;
    }

    public enum VIEW_MODE {
        ITEM, DETAIL
    }

}
