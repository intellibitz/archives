package intellibitz.intellidroid.bean

import intellibitz.intellidroid.util.MainApplicationSingleton
import java.util.Comparator

open class BaseItemComparator<T : BaseBean> : Comparator<T> {

    private var sortMode = SORT_MODE.ASC

    constructor() : super()

    constructor(sortMode: SORT_MODE) : super() {
        this.sortMode = sortMode
    }

    override fun compare(lhs: T, rhs: T): Int {
        when (sortMode) {
            SORT_MODE.ASC -> {
                if (lhs.getTimestamp() == rhs.getTimestamp()) return 0
                return if (lhs.getTimestamp() > rhs.getTimestamp()) 1 else -1
            }
            SORT_MODE.DESC -> {
                if (lhs.getTimestamp() == rhs.getTimestamp()) return 0
                return if (lhs.getTimestamp() > rhs.getTimestamp()) -1 else 1
            }
            SORT_MODE.ASC_DT -> {
                val lMillis = MainApplicationSingleton.getDateTimeMillisISO(lhs.getDateTime())
                val rMillis = MainApplicationSingleton.getDateTimeMillisISO(rhs.getDateTime())
                if (lMillis == rMillis) return 0
                return if (lMillis > rMillis) -1 else 1
            }
            SORT_MODE.DESC_DT -> {
                val lMillis = MainApplicationSingleton.getDateTimeMillisISO(lhs.getDateTime())
                val rMillis = MainApplicationSingleton.getDateTimeMillisISO(rhs.getDateTime())
                if (lMillis == rMillis) return 0
                return if (lMillis > rMillis) -1 else 1
            }
        }
    }

    enum class SORT_MODE {
        ASC, DESC, ASC_DT, DESC_DT
    }
}
