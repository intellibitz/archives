package intellibitz.sted.fontmap

import intellibitz.sted.util.Resources

class FontMapEntry(
    override var from: String = "",
    override var to: String = ""
) : ITransliterate.IEntry, Comparable<Any>, Cloneable {

    var beginsWith: Boolean = false
    var endsWith: Boolean = false
    
    var followedBy: String? = null
        set(value) {
            field = if (Resources.EMPTY_STRING == value) null else value
        }
        
    var precededBy: String? = null
        set(value) {
            field = if (Resources.EMPTY_STRING == value) null else value
        }
        
    var conditional: String = Resources.ENTRY_CONDITIONAL_AND
        set(value) {
            require(
                value.equals(Resources.ENTRY_CONDITIONAL_AND, ignoreCase = true) ||
                value.equals(Resources.ENTRY_CONDITIONAL_OR, ignoreCase = true) ||
                value.equals(Resources.ENTRY_CONDITIONAL_NOT, ignoreCase = true)
            ) { value }
            field = when {
                value.equals(Resources.ENTRY_CONDITIONAL_AND, ignoreCase = true) -> Resources.ENTRY_CONDITIONAL_AND
                value.equals(Resources.ENTRY_CONDITIONAL_OR, ignoreCase = true) -> Resources.ENTRY_CONDITIONAL_OR
                else -> Resources.ENTRY_CONDITIONAL_NOT
            }
        }
    
    var id: String = Resources.getId().toString()
        private set
        
    var status: Int = -1

    fun isAdded(): Boolean = Resources.ENTRY_STATUS_ADD == status
    fun isEdited(): Boolean = Resources.ENTRY_STATUS_EDIT == status
    fun isDeleted(): Boolean = Resources.ENTRY_STATUS_DELETE == status

    fun setBeginsWith(beginsWith: String) {
        this.beginsWith = beginsWith.toBoolean()
    }

    fun setEndsWith(endsWith: String) {
        this.endsWith = endsWith.toBoolean()
    }

    fun isValid(): Boolean {
        return from.isNotEmpty() && to.isNotEmpty() && from != to
    }

    fun isRulesSet(): Boolean {
        return beginsWith || endsWith || !followedBy.isNullOrEmpty() || !precededBy.isNullOrEmpty()
    }


    override fun toString(): String {
        return buildString {
            append(from)
            append(Resources.ENTRY_TOSTRING_DELIMITER)
            append(to)
            append(Resources.ENTRY_TOSTRING_DELIMITER)
            append(beginsWith)
            append(Resources.ENTRY_TOSTRING_DELIMITER)
            append(endsWith)
            append(Resources.ENTRY_TOSTRING_DELIMITER)
            append(followedBy)
            append(Resources.ENTRY_TOSTRING_DELIMITER)
            append(precededBy)
            append(Resources.ENTRY_TOSTRING_DELIMITER)
            append(conditional)
        }
    }

    override fun compareTo(other: Any): Int {
        if (other is FontMapEntry) {
            if (this == other) return 0
            if (from != other.from) return from.compareTo(other.from)
            if (to != other.to) return to.compareTo(other.to)
            if (beginsWith != other.beginsWith) return 1
            if (endsWith != other.endsWith) return 1
            if (conditional != other.conditional) return conditional.compareTo(other.conditional)
            
            val folVal = if (followedBy == null) other.followedBy == null else followedBy == other.followedBy
            if (!folVal) {
                if (followedBy != null && other.followedBy != null) {
                    return followedBy!!.compareTo(other.followedBy!!)
                }
                return 1
            }
            
            val preVal = if (precededBy == null) other.precededBy == null else precededBy == other.precededBy
            if (!preVal) {
                if (precededBy != null && other.precededBy != null) {
                    return precededBy!!.compareTo(other.precededBy!!)
                }
                return 1
            }
        }
        return -1
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FontMapEntry) return false

        if (from != other.from) return false
        if (to != other.to) return false
        if (beginsWith != other.beginsWith) return false
        if (endsWith != other.endsWith) return false
        if (conditional != other.conditional) return false
        
        val folVal = if (followedBy == null) other.followedBy == null else followedBy == other.followedBy
        if (!folVal) return false
        
        return if (precededBy == null) other.precededBy == null else precededBy == other.precededBy
    }

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 29 * result + to.hashCode()
        result = 29 * result + conditional.hashCode()
        result = 29 * result + (if (beginsWith) 1 else 0)
        result = 29 * result + (if (endsWith) 1 else 0)
        result = 29 * result + (followedBy?.hashCode() ?: 0)
        result = 29 * result + (precededBy?.hashCode() ?: 0)
        return result
    }

    public override fun clone(): Any {
        return try {
            val cloned = super.clone() as FontMapEntry
            cloned.id = id
            cloned
        } catch (e: CloneNotSupportedException) {
            e.printStackTrace()
            null!!
        }
    }
}
