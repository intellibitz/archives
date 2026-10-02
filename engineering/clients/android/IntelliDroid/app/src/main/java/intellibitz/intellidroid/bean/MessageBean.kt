package intellibitz.intellidroid.bean

import android.os.Parcel
import android.os.Parcelable

open class MessageBean : BaseBean, Parcelable {

    var contactThreadId: Long = 0
    var contactThreadDataId: String? = null
    var contactThreadName: String? = null
    var contactThreadPic: String? = null
    var contactThreadType: String? = null
    var _id: Long = 0
    var id: String? = null
    var docType: String? = null
    var name: String? = null
    var rev: String? = null
    var type: String? = null
    var toType: String? = null
    var docOwnerEmail: String? = null
    var docOwner: String? = null
    var docSenderEmail: String? = null
    var fromUid: String? = null
    var toUid: String? = null
    var toChatUid: String? = null
    var chatId: String? = null
    var docSender: String? = null
    var pendingDocs: Int = 0
    var unreadCount: Int = 0
    var hasAttachments: Int = 0
    var subject: String? = null
    var latestMessageText: String? = null
    var latestMessageTimestamp: Long = 0
    var read: Int = 0
    var delivered: Int = 0
    var from: String? = null
    var to: String? = null
    var cc: String? = null
    var bcc: String? = null
    private var timestamp: Long = 0
    var dateTime: String? = null
    var profilePic: String? = null
    var isTyping: Boolean = false
    var isFlagged: Boolean = false
    var typingText: String? = null

    constructor() : super()

    protected constructor(`in`: Parcel)

    override fun getDataId(): String? = id

    override fun getTimestamp(): Long = timestamp

    fun setTimestamp(timestamp: Long) {
        this.timestamp = timestamp
    }

    override fun getDateTime(): String? = dateTime

    fun setFlagged(flagged: Int) {
        this.isFlagged = flagged > 0
    }

    fun setFlagged(flagged: Boolean) {
        this.isFlagged = flagged
    }

    fun isGroupChat(): Boolean = "GROUP".equals(toType, ignoreCase = true)

    fun isChat(): Boolean = "CHAT".equals(type, ignoreCase = true)

    fun isEmail(): Boolean = "EMAIL".equals(type, ignoreCase = true)

    fun isRead(): Boolean = read != 0

    fun isDelivered(): Boolean = delivered != 0

    override fun writeToParcel(dest: Parcel, flags: Int) {}

    override fun describeContents(): Int = 0

    class MessageBeanComparator(sortMode: SORT_MODE) : BaseItemComparator<MessageBean>(sortMode)

    companion object CREATOR : Parcelable.Creator<MessageBean> {
        override fun createFromParcel(`in`: Parcel): MessageBean = MessageBean(`in`)
        override fun newArray(size: Int): Array<MessageBean?> = arrayOfNulls(size)
    }
}
