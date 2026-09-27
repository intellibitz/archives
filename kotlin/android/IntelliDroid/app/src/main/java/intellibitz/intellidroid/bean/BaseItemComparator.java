package intellibitz.intellidroid.bean;

import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import java.util.Comparator;

/**
 */
public class BaseItemComparator<T extends BaseBean> implements
        Comparator<T> {

    private SORT_MODE sortMode = SORT_MODE.ASC;

    public BaseItemComparator() {
        super();
    }

    public BaseItemComparator(SORT_MODE sortMode) {
        super();
        this.sortMode = sortMode;
    }

    @Override
    public int compare(T lhs, T rhs) {
        if (SORT_MODE.ASC == sortMode) {
            if (lhs.getTimestamp() == rhs.getTimestamp())
                return 0;
            if (lhs.getTimestamp() > rhs.getTimestamp())
                return 1;
            return -1;
        } else if (SORT_MODE.DESC == sortMode) {
            if (lhs.getTimestamp() == rhs.getTimestamp())
                return 0;
            if (lhs.getTimestamp() > rhs.getTimestamp())
                return -1;
            return 1;
        } else if (SORT_MODE.ASC_DT == sortMode) {
            if (MainApplicationSingleton.getDateTimeMillisISO(lhs.getDateTime()) ==
                    MainApplicationSingleton.getDateTimeMillisISO(rhs.getDateTime()))
                return 0;
            if (MainApplicationSingleton.getDateTimeMillisISO(lhs.getDateTime()) >
                    MainApplicationSingleton.getDateTimeMillisISO(rhs.getDateTime()))
                return -1;
            return 1;
        } else if (SORT_MODE.DESC_DT == sortMode) {
            if (MainApplicationSingleton.getDateTimeMillisISO(lhs.getDateTime()) ==
                    MainApplicationSingleton.getDateTimeMillisISO(rhs.getDateTime()))
                return 0;
            if (MainApplicationSingleton.getDateTimeMillisISO(lhs.getDateTime()) >
                    MainApplicationSingleton.getDateTimeMillisISO(rhs.getDateTime()))
                return -1;
            return 1;
        }
        return 0;
    }


    public enum SORT_MODE {
        ASC, DESC, ASC_DT, DESC_DT
    }
}
