

package intellibitz.intellidroid.widget.advrecyclerview.utils;

import android.view.ViewGroup;

import intellibitz.intellidroid.widget.advrecyclerview.expandable.ExpandableItemAdapter;
import intellibitz.intellidroid.widget.advrecyclerview.expandable.ExpandableItemAdapter;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import intellibitz.intellidroid.widget.advrecyclerview.expandable.ExpandableItemAdapter;

public abstract class AbstractExpandableItemAdapter<GVH extends RecyclerView.ViewHolder, CVH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements ExpandableItemAdapter<GVH, CVH> {

    /**
     * This method will not be called.
     * Override {@link #onCreateGroupViewHolder(ViewGroup, int)} and
     * {@link #onCreateChildViewHolder(ViewGroup, int)} instead.
     *
     * @param parent   not used
     * @param viewType not used
     * @return null
     */
    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    /**
     * This method will not be called.
     * Override {@link #getGroupId(int)} and {@link #getChildId(int, int)} instead.
     *
     * @param position not used
     * @return {@link RecyclerView#NO_ID}
     */
    @Override
    public final long getItemId(int position) {
        return RecyclerView.NO_ID;
    }

    /**
     * This method will not be called.
     * Override {@link #getGroupItemViewType(int)} and {@link #getChildItemViewType(int, int)} instead.
     *
     * @param position not used
     * @return 0
     */
    @Override
    public final int getItemViewType(int position) {
        return 0;
    }

    /**
     * This method will not be called.
     * Override {@link #getGroupCount()} and {@link #getChildCount(int)} instead.
     *
     * @return 0
     */
    @Override
    public final int getItemCount() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGroupItemViewType(int groupPosition) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getChildItemViewType(int groupPosition, int childPosition) {
        return 0;
    }

    /**
     * This method will not be called.
     * Override {@link #onBindGroupViewHolder(RecyclerView.ViewHolder, int, int)} ()} and
     * {@link #onBindChildViewHolder(RecyclerView.ViewHolder, int, int, int)} instead.
     *
     * @param holder   not used
     * @param position not used
     */
    @Override
    public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindGroupViewHolder(GVH holder, int groupPosition, int viewType, List<Object> payloads) {
        onBindGroupViewHolder(holder, groupPosition, viewType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindChildViewHolder(CVH holder, int groupPosition, int childPosition, int viewType, List<Object> payloads) {
        onBindChildViewHolder(holder, groupPosition, childPosition, viewType);
    }

    /**
     * Override this method if need to customize the behavior.
     * {@inheritDoc}
     */
    @Override
    public boolean onHookGroupExpand(int groupPosition, boolean fromUser) {
        return true;
    }

    /**
     * Override this method if need to customize the behavior.
     * {@inheritDoc}
     */
    @Override
    public boolean onHookGroupCollapse(int groupPosition, boolean fromUser) {
        return true;
    }
}