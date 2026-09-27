

package intellibitz.intellidroid.widget.advrecyclerview.draggable;

import androidx.recyclerview.widget.RecyclerView;

class LeftRightEdgeEffectDecorator extends BaseEdgeEffectDecorator {
    public LeftRightEdgeEffectDecorator(RecyclerView recyclerView) {
        super(recyclerView);
    }

    @Override
    protected int getEdgeDirection(int no) {
        switch (no) {
            case 0:
                return EDGE_LEFT;
            case 1:
                return EDGE_RIGHT;
            default:
                throw new IllegalArgumentException();
        }
    }
}
