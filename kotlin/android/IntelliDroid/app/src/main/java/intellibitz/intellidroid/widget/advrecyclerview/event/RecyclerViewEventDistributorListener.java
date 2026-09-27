

package intellibitz.intellidroid.widget.advrecyclerview.event;

public interface RecyclerViewEventDistributorListener {
    void onAddedToEventDistributor(BaseRecyclerViewEventDistributor distributor);

    void onRemovedFromEventDistributor(BaseRecyclerViewEventDistributor distributor);
}
