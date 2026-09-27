

package intellibitz.intellidroid.widget.advrecyclerview.event;

import java.lang.ref.WeakReference;

import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewRecyclerEventDistributor extends BaseRecyclerViewEventDistributor<RecyclerView.RecyclerListener> {

    private InternalRecyclerListener mInternalRecyclerListener;

    public RecyclerViewRecyclerEventDistributor() {
        super();

        mInternalRecyclerListener = new InternalRecyclerListener(this);
    }

    @Override
    protected void onRecyclerViewAttached(RecyclerView rv) {
        super.onRecyclerViewAttached(rv);

        rv.setRecyclerListener(mInternalRecyclerListener);
    }

    @Override
    protected void onRelease() {
        super.onRelease();

        if (mInternalRecyclerListener != null) {
            mInternalRecyclerListener.release();
            mInternalRecyclerListener = null;
        }
    }

    /*package*/ void handleOnViewRecycled(RecyclerView.ViewHolder holder) {
        if (mListeners == null) {
            return;
        }

        for (RecyclerView.RecyclerListener listener : mListeners) {
            listener.onViewRecycled(holder);
        }
    }

    private static class InternalRecyclerListener implements RecyclerView.RecyclerListener {
        private final WeakReference<RecyclerViewRecyclerEventDistributor> mRefDistributor;

        public InternalRecyclerListener(RecyclerViewRecyclerEventDistributor distributor) {
            super();
            mRefDistributor = new WeakReference<>(distributor);
        }

        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            final RecyclerViewRecyclerEventDistributor distributor = mRefDistributor.get();

            if (distributor != null) {
                distributor.handleOnViewRecycled(holder);
            }
        }

        public void release() {
            mRefDistributor.clear();
        }
    }
}
