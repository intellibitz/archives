package intellibitz.intellidroid.widget.advrecyclerview.event

interface RecyclerViewEventDistributorListener {
    fun onAddedToEventDistributor(distributor: BaseRecyclerViewEventDistributor<*>?)
    fun onRemovedFromEventDistributor(distributor: BaseRecyclerViewEventDistributor<*>?)
}
