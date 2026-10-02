package intellibitz.intellidroid.data

import android.os.Parcel
import android.os.Parcelable
import intellibitz.intellidroid.bean.BaseBean
import java.io.Serializable

open class BaseItem : Serializable, Cloneable, Parcelable, BaseBean, Comparable<BaseItem> {

    var dataId: String? = null
    var intellibitzId: String? = null
    var deviceContactId: Long = 0
    var type: String? = null
    var user: String? = null
    var mailbox: String? = null
    var seqNo: Int = 0
    var _id: Long = 0
    var threadId: String? = null
    var threadIdRef: String? = null
    var dataRev: String? = null
    var threadIdParts: String? = null
    var groupId: String? = null
    var groupIdRef: String? = null
    var isGroup: Boolean = false
    var isEmailItem: Boolean = false
    var isAnonymous: Boolean = false
    var isDevice: Boolean = false
    var isCloud: Boolean = false
    var firstName: String? = null
    var lastName: String? = null
    var displayName: String? = null
    var companyName: String? = null
    var companyId: String? = null
    var email: String? = null
    var emailCode: String? = null
    var active: Int = 0
    var deviceId: String? = null
    var deviceName: String? = null
    var deviceRef: String? = null
    var name: String? = null
    var status: String? = null
    var profilePic: String? = null
    var cloudPic: String? = null
    var typeId: String? = null
    var docOwner: String? = null
    var docType: String? = null
    var baseType: String? = null
    var timestamp: Long = 0
    var dateTime: String? = null

    constructor() : super()

    constructor(dataId: String?) : this(dataId, null)

    constructor(dataId: String?, name: String?) : this(dataId, name, null)

    constructor(dataId: String?, name: String?, type: String?) {
        this.dataId = dataId
        this.name = name
        this.type = type
    }

    constructor(dataId: String?, threadIdRef: String?, name: String?, type: String?) {
        this.dataId = dataId
        this.name = name
        this.type = type
        this.threadIdRef = threadIdRef
    }

    protected constructor(`in`: Parcel) {
        _id = `in`.readLong()
        threadId = `in`.readString()
        threadIdRef = `in`.readString()
        threadIdParts = `in`.readString()
        groupId = `in`.readString()
        groupIdRef = `in`.readString()
        dataId = `in`.readString()
        typeId = `in`.readString()
        dataRev = `in`.readString()
        intellibitzId = `in`.readString()
        isGroup = `in`.readByte().toInt() != 0
        isEmailItem = `in`.readByte().toInt() != 0
        isAnonymous = `in`.readByte().toInt() != 0
        isDevice = `in`.readByte().toInt() != 0
        isCloud = `in`.readByte().toInt() != 0
        user = `in`.readString()
        mailbox = `in`.readString()
        seqNo = `in`.readInt()
        firstName = `in`.readString()
        lastName = `in`.readString()
        displayName = `in`.readString()
        email = `in`.readString()
        emailCode = `in`.readString()
        companyName = `in`.readString()
        companyId = `in`.readString()
        active = `in`.readInt()
        deviceContactId = `in`.readLong()
        deviceId = `in`.readString()
        deviceName = `in`.readString()
        deviceRef = `in`.readString()
        name = `in`.readString()
        type = `in`.readString()
        docType = `in`.readString()
        baseType = `in`.readString()
        docOwner = `in`.readString()
        status = `in`.readString()
        profilePic = `in`.readString()
        cloudPic = `in`.readString()
        timestamp = `in`.readLong()
        dateTime = `in`.readString()
    }

    override fun getDataId(): String? = dataId

    override fun getTimestamp(): Long = timestamp

    override fun getDateTime(): String? = dateTime

    fun setGroup(group: Int) {
        this.isGroup = group > 0
    }

    fun setEmailItem(email: Int) {
        this.isEmailItem = email > 0
    }

    fun setAnonymous(anonymous: Int) {
        this.isAnonymous = anonymous > 0
    }

    fun setDevice(device: Int) {
        this.isDevice = device > 0
    }

    fun setCloud(cloud: Int) {
        this.isCloud = cloud > 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val infoItem = other as BaseItem
        if (threadIdRef != null) {
            return threadIdRef == infoItem.threadIdRef
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        if (threadIdRef != null) {
            return threadIdRef.hashCode()
        }
        return super.hashCode()
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Any {
        return super.clone()
    }

    override fun toString(): String {
        return "BaseItem{" +
                "_id=" + _id +
                ", threadId='" + threadId + '\'' +
                ", threadIdRef='" + threadIdRef + '\'' +
                ", threadIdParts='" + threadIdParts + '\'' +
                ", groupId='" + groupId + '\'' +
                ", groupIdRef='" + groupIdRef + '\'' +
                ", dataId='" + dataId + '\'' +
                ", dataRev='" + dataRev + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceRef='" + deviceRef + '\'' +
                ", user='" + user + '\'' +
                ", mailbox='" + mailbox + '\'' +
                ", seqNo=" + seqNo +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", emailCode='" + emailCode + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                ", profilePic='" + profilePic + '\'' +
                ", cloudPic='" + cloudPic + '\'' +
                ", timestamp=" + timestamp +
                ", dateTime='" + dateTime + '\'' +
                '}'
    }

    override fun compareTo(other: BaseItem): Int {
        if (this._id == other._id) return 0
        return if (this._id > other._id) 1 else -1
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(_id)
        dest.writeString(threadId)
        dest.writeString(threadIdRef)
        dest.writeString(threadIdParts)
        dest.writeString(groupId)
        dest.writeString(groupIdRef)
        dest.writeString(dataId)
        dest.writeString(typeId)
        dest.writeString(dataRev)
        dest.writeString(intellibitzId)
        dest.writeByte((if (isGroup) 1 else 0).toByte())
        dest.writeByte((if (isEmailItem) 1 else 0).toByte())
        dest.writeByte((if (isAnonymous) 1 else 0).toByte())
        dest.writeByte((if (isDevice) 1 else 0).toByte())
        dest.writeByte((if (isCloud) 1 else 0).toByte())
        dest.writeString(user)
        dest.writeString(mailbox)
        dest.writeInt(seqNo)
        dest.writeString(firstName)
        dest.writeString(lastName)
        dest.writeString(displayName)
        dest.writeString(email)
        dest.writeString(emailCode)
        dest.writeString(companyName)
        dest.writeString(companyId)
        dest.writeInt(active)
        dest.writeLong(deviceContactId)
        dest.writeString(deviceId)
        dest.writeString(deviceName)
        dest.writeString(deviceRef)
        dest.writeString(name)
        dest.writeString(type)
        dest.writeString(docType)
        dest.writeString(baseType)
        dest.writeString(docOwner)
        dest.writeString(status)
        dest.writeString(profilePic)
        dest.writeString(cloudPic)
        dest.writeLong(timestamp)
        dest.writeString(dateTime)
    }

    override fun describeContents(): Int = 0

    companion object {
        const val THREAD = "THREAD"
        const val MSG = "MSG"
        const val CHAT = "CHAT"
        const val EMAIL = "EMAIL"
        const val USER = "USER"
        const val GROUP = "GROUP"
        const val NEST = "NEST"
        const val STACK = "STACK"
        const val DRAFT = "DRAFT"
        const val SCHEDULE = "SCHEDULE"

        @JvmField
        val CREATOR: Parcelable.Creator<BaseItem> = object : Parcelable.Creator<BaseItem> {
            override fun createFromParcel(`in`: Parcel): BaseItem = BaseItem(`in`)
            override fun newArray(size: Int): Array<BaseItem?> = arrayOfNulls(size)
        }
    }
}
