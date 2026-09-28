package intellibitz.intellidroid.data

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.text.util.Rfc822Token
import android.util.Log
import intellibitz.intellidroid.bean.BaseItemComparator
import java.util.ArrayList
import java.util.HashSet
import java.util.Stack
import java.util.concurrent.ConcurrentLinkedQueue

open class MessageItem : BaseItem {

    var from: String? = ""
    var to: String? = ""
    var cc: String? = ""
    var bcc: String? = ""
    var fromUid: String? = null
    var toUid: String? = null
    var chatId: String? = null
    var toChatUid: String? = null
    var subject: String? = null
    var defaultFolder: Int = 0
    var folderCode: String? = null
    var contactItem: ContactItem? = null
    var attachments: MutableSet<MessageItem> = HashSet()
    var docOwnerEmail: String? = null
    var docSenderEmail: String? = null
    var docSender: String? = null
    var fromEmail: String? = null
    var fromName: String? = null
    var messageDirection: String? = null
    var messageType: String? = null
    var text: String? = null
    var fullText: String? = null
    var html: String? = null
    var messageAttachId: String? = null
    var isReadyToSend: Boolean = false
    var toType: String? = null
    var pendingDocs: Int = 0
    var hasAttachments: Int = 0
    var read: Int = 0
    var delivered: Int = 0
    var locked: Int = 0
    var isBroadcast: Boolean = false
    var isNoText: Boolean = false
    var isFlagged: Boolean = false
    var msgRef: String? = null
    var chatMsgRef: String? = null
    var flags: String? = null
    var photoFileUri: Uri? = null
    var videoFileUri: Uri? = null
    var audioFileUri: Uri? = null
    var sharedText: String? = null
    var sharedUri: Uri? = null
    var sharedUris: ArrayList<Uri> = ArrayList()
    var sharedDeviceContactItem: ContactItem? = null
    var unreadCount: Int = 0
    var latestMessageText: String? = null
    var latestMessageTimestamp: Long = 0
    var isTyping: Boolean = false
    var typingText: String? = null
    var messages: MutableSet<MessageItem> = HashSet()
    var messageItemStack: Stack<MessageItem> = Stack()
    var messageItemConcurrentLinkedQueue: ConcurrentLinkedQueue<MessageItem> =
        ConcurrentLinkedQueue()

    var description: String? = null
    var msgAttachID: String? = null
    var partID: String? = null
    var subType: String? = null
    var encoding: String? = null
    var language: String? = null
    var size: Int = 0
    var md5: String? = null
    var downloadURL: String? = null

    constructor() : super()

    constructor(id: String?) : this(id, id, null)

    constructor(id: String?, name: String?, type: String?) : super(id, name, type) {
        this.description = name
    }

    protected constructor(`in`: Parcel) : super(`in`) {
        msgAttachID = `in`.readString()
        partID = `in`.readString()
        subType = `in`.readString()
        encoding = `in`.readString()
        language = `in`.readString()
        md5 = `in`.readString()
        downloadURL = `in`.readString()
        description = `in`.readString()
        size = `in`.readInt()
        isReadyToSend = `in`.readByte().toInt() != 0
        fromEmail = `in`.readString()
        fromName = `in`.readString()
        messageDirection = `in`.readString()
        messageType = `in`.readString()
        text = `in`.readString()
        fullText = `in`.readString()
        html = `in`.readString()
        messageAttachId = `in`.readString()
        toType = `in`.readString()
        docOwnerEmail = `in`.readString()
        fromUid = `in`.readString()
        toUid = `in`.readString()
        chatId = `in`.readString()
        toChatUid = `in`.readString()
        docSenderEmail = `in`.readString()
        docSender = `in`.readString()
        pendingDocs = `in`.readInt()
        hasAttachments = `in`.readInt()
        read = `in`.readInt()
        delivered = `in`.readInt()
        locked = `in`.readInt()
        isBroadcast = `in`.readByte().toInt() != 0
        isNoText = `in`.readByte().toInt() != 0
        isFlagged = `in`.readByte().toInt() != 0
        subject = `in`.readString()
        msgRef = `in`.readString()
        chatMsgRef = `in`.readString()
        from = `in`.readString()
        to = `in`.readString()
        cc = `in`.readString()
        bcc = `in`.readString()
        flags = `in`.readString()
        contactItem = `in`.readParcelable(javaClass.classLoader)
        val attSize = `in`.readInt()
        for (i in 0 until attSize) {
            val item: MessageItem? = `in`.readParcelable(javaClass.classLoader)
            if (item != null) attachments.add(item)
        }
        isTyping = `in`.readByte().toInt() != 0
        typingText = `in`.readString()
        latestMessageText = `in`.readString()
        latestMessageTimestamp = `in`.readLong()
        unreadCount = `in`.readInt()
        defaultFolder = `in`.readInt()
        folderCode = `in`.readString()
        photoFileUri = `in`.readParcelable(javaClass.classLoader)
        videoFileUri = `in`.readParcelable(javaClass.classLoader)
        audioFileUri = `in`.readParcelable(javaClass.classLoader)
        sharedText = `in`.readString()
    }

    fun isRead(): Boolean = read != 0

    fun setRead(readVal: Boolean) {
        read = if (readVal) 1 else 0
    }

    fun isDelivered(): Boolean = delivered != 0

    fun setDelivered(deliveredVal: Boolean) {
        delivered = if (deliveredVal) 1 else 0
    }

    fun isLocked(): Boolean = locked != 0

    fun setLocked(lockedVal: Boolean) {
        locked = if (lockedVal) 1 else 0
    }

    fun peekMessageInStack(): MessageItem? {
        return if (messageItemStack.isEmpty()) null else messageItemStack.peek()
    }

    fun cloneShal(): Any? {
        return try {
            super.clone()
        } catch (ignored: CloneNotSupportedException) {
            Log.e(TAG, ignored.message ?: "")
            null
        }
    }

    @Throws(CloneNotSupportedException::class)
    override fun clone(): Any {
        val copy = super.clone() as MessageItem
        val ci = copy.contactItem
        if (ci != null) {
            copy.contactItem = ci.clone() as ContactItem
        }
        val msgs = copy.messages
        val ecopy1 = HashSet<MessageItem>(msgs.size)
        for (item in msgs) {
            ecopy1.add(item.clone() as MessageItem)
        }
        copy.messages = ecopy1

        val atts = copy.attachments
        val acopy1 = HashSet<MessageItem>(atts.size)
        for (item in atts) {
            acopy1.add(item.clone() as MessageItem)
        }
        copy.attachments = acopy1
        return copy
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val baseItem = other as BaseItem
        val currentDataId = this.dataId ?: ""
        val currentChatMsgRef = this.chatMsgRef
        if (currentDataId == this.chatId && !TextUtils.isEmpty(currentChatMsgRef)) {
            val messageItem = baseItem as MessageItem
            return currentChatMsgRef == messageItem.chatMsgRef
        }
        return currentDataId == (baseItem.dataId ?: "")
    }

    override fun hashCode(): Int {
        val currentDataId = this.dataId ?: ""
        var hash = 31 * currentDataId.hashCode()
        val currentChatMsgRef = this.chatMsgRef
        if (currentDataId == this.chatId && !TextUtils.isEmpty(currentChatMsgRef)) {
            hash = 31 * (currentChatMsgRef?.hashCode() ?: 0)
        }
        return hash
    }

    override fun toString(): String {
        return "MessageItem{" +
                "messages=" + messages +
                ", fromEmail='" + fromEmail + '\'' +
                ", fromName='" + fromName + '\'' +
                ", messageDirection='" + messageDirection + '\'' +
                ", messageType='" + messageType + '\'' +
                ", text='" + text + '\'' +
                ", html='" + html + '\'' +
                ", fromUid='" + fromUid + '\'' +
                ", toUid='" + toUid + '\'' +
                ", messageAttachId='" + messageAttachId + '\'' +
                ", messageItemStack=" + messageItemStack +
                ", latestMessageText='" + latestMessageText + '\'' +
                ", unreadCount=" + unreadCount +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", cc='" + cc + '\'' +
                ", bcc='" + bcc + '\'' +
                ", toType='" + toType + '\'' +
                ", docOwnerEmail='" + docOwnerEmail + '\'' +
                ", docSenderEmail='" + docSenderEmail + '\'' +
                ", docSender='" + docSender + '\'' +
                ", hasAttachments=" + hasAttachments +
                ", subject='" + subject + '\'' +
                ", attachments=" + attachments +
                ", description='" + description + '\'' +
                ", partID='" + partID + '\'' +
                ", subType='" + subType + '\'' +
                ", encoding='" + encoding + '\'' +
                ", language='" + language + '\'' +
                ", size=" + size +
                ", md5='" + md5 + '\'' +
                ", downloadURL='" + downloadURL + '\'' +
                '}' + super.toString()
    }

    fun invoke(): MessageItem {
        val ci = contactItem ?: return this
        if (ci.isEmpty()) return this
        val contactItems = ci.contactItems
        for (emailItem in contactItems) {
            val email = emailItem.typeId
            if ("from" == emailItem.type && from != email) {
                // from logic
            } else if ("to" == emailItem.type) {
                if (!to.isNullOrEmpty() && email != null && !to!!.contains(email)) {
                    to += ","
                }
                if (email != null && !to!!.contains(email)) {
                    to += email
                }
            } else if ("cc" == emailItem.type) {
                if (!cc.isNullOrEmpty() && email != null && !cc!!.contains(email)) {
                    cc += ","
                }
                if (email != null && !cc!!.contains(email)) {
                    cc += email
                }
            } else if ("bcc" == emailItem.type) {
                if (!bcc.isNullOrEmpty() && email != null && !bcc!!.contains(email)) {
                    bcc += ","
                }
                if (email != null && !bcc!!.contains(email)) {
                    bcc += email
                }
            }
        }
        return this
    }

    fun compose(): ContactItem? {
        return contactItem
    }

    fun compose(
        to: Array<Rfc822Token>?,
        cc: Array<Rfc822Token>?,
        bcc: Array<Rfc822Token>?
    ): ContactItem? {
        if (contactItem == null) contactItem = ContactItem()
        var sto = ""
        var scc = ""
        var sbcc = ""

        if (!to.isNullOrEmpty()) {
            for (i in to.indices) {
                val address = to[i].address
                if (!address.isNullOrEmpty()) {
                    val ci = ContactItem().apply {
                        dataId = address
                        typeId = address
                        intellibitzId = address
                        name = address
                        type = "to"
                    }
                    contactItem?.addContact(ci)
                    sto += address
                    if (i < to.size - 1) {
                        sto += ","
                    }
                }
            }
        }

        if (!cc.isNullOrEmpty()) {
            for (i in cc.indices) {
                val address = cc[i].address
                if (!address.isNullOrEmpty()) {
                    val ci = ContactItem().apply {
                        dataId = address
                        typeId = address
                        intellibitzId = address
                        name = address
                        type = "cc"
                    }
                    contactItem?.addContact(ci)
                    scc += address
                    if (i < cc.size - 1) {
                        scc += ","
                    }
                }
            }
        }

        if (!bcc.isNullOrEmpty()) {
            for (i in bcc.indices) {
                val address = bcc[i].address
                if (!address.isNullOrEmpty()) {
                    val ci = ContactItem().apply {
                        dataId = address
                        typeId = address
                        intellibitzId = address
                        name = address
                        type = "bcc"
                    }
                    contactItem?.addContact(ci)
                    sbcc += address
                    if (i < bcc.size - 1) {
                        sbcc += ","
                    }
                }
            }
        }

        this.to = sto
        this.cc = scc
        this.bcc = sbcc
        return compose()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeString(msgAttachID)
        dest.writeString(partID)
        dest.writeString(subType)
        dest.writeString(encoding)
        dest.writeString(language)
        dest.writeString(md5)
        dest.writeString(downloadURL)
        dest.writeString(description)
        dest.writeInt(size)
        dest.writeByte((if (isReadyToSend) 1 else 0).toByte())
        dest.writeString(fromEmail)
        dest.writeString(fromName)
        dest.writeString(messageDirection)
        dest.writeString(messageType)
        dest.writeString(text)
        dest.writeString(fullText)
        dest.writeString(html)
        dest.writeString(messageAttachId)
        dest.writeString(toType)
        dest.writeString(docOwnerEmail)
        dest.writeString(fromUid)
        dest.writeString(toUid)
        dest.writeString(chatId)
        dest.writeString(toChatUid)
        dest.writeString(docSenderEmail)
        dest.writeString(docSender)
        dest.writeInt(pendingDocs)
        dest.writeInt(hasAttachments)
        dest.writeInt(read)
        dest.writeInt(delivered)
        dest.writeInt(locked)
        dest.writeByte((if (isBroadcast) 1 else 0).toByte())
        dest.writeByte((if (isNoText) 1 else 0).toByte())
        dest.writeByte((if (isFlagged) 1 else 0).toByte())
        dest.writeString(subject)
        dest.writeString(msgRef)
        dest.writeString(chatMsgRef)
        dest.writeString(from)
        dest.writeString(to)
        dest.writeString(cc)
        dest.writeString(bcc)
        dest.writeString(this.flags)
        dest.writeParcelable(contactItem, flags)
        dest.writeInt(attachments.size)
        for (item in attachments) {
            dest.writeParcelable(item, flags)
        }
        dest.writeByte((if (isTyping) 1 else 0).toByte())
        dest.writeString(typingText)
        dest.writeString(latestMessageText)
        dest.writeLong(latestMessageTimestamp)
        dest.writeInt(unreadCount)
        dest.writeInt(defaultFolder)
        dest.writeString(folderCode)
        dest.writeParcelable(photoFileUri, flags)
        dest.writeParcelable(videoFileUri, flags)
        dest.writeParcelable(audioFileUri, flags)
        dest.writeString(sharedText)
        dest.writeParcelable(sharedUri, flags)
        dest.writeParcelable(sharedDeviceContactItem, flags)
        dest.writeTypedList(sharedUris)
        dest.writeByte((if (messageItemStack.isEmpty()) 1 else 0).toByte())
        if (messageItemStack.isNotEmpty()) {
            dest.writeParcelable(peekMessageInStack(), flags)
        }
        dest.writeInt(messages.size)
        for (item in messages) {
            dest.writeParcelable(item, flags)
        }
        dest.writeInt(messageItemConcurrentLinkedQueue.size)
        for (item in messageItemConcurrentLinkedQueue) {
            dest.writeParcelable(item, flags)
        }
    }

    override fun describeContents(): Int = 0

    open class MessageItemComparator<T : MessageItem> : BaseItemComparator<T> {
        constructor() : super()
        constructor(sortMode: SORT_MODE) : super(sortMode)
        override fun compare(lhs: T, rhs: T): Int = super.compare(lhs, rhs)
    }

    companion object {
        const val TAG = "MessageItem"
        const val EMAIL_MESSAGE = "EmailMessageItem"
        const val ATTACHMENT_MESSAGE = "AttachmentItem"
        const val SCHEDULE_MESSAGE = "ScheduleItem"
        const val FEED_MESSAGE = "FeedItem"

        @JvmField
        val CREATOR: Parcelable.Creator<MessageItem> = object : Parcelable.Creator<MessageItem> {
            override fun createFromParcel(`in`: Parcel): MessageItem = MessageItem(`in`)
            override fun newArray(size: Int): Array<MessageItem?> = arrayOfNulls(size)
        }
    }
}
