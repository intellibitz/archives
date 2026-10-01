package intellibitz.intellidroid.data

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import intellibitz.intellidroid.bean.BaseItemComparator
import intellibitz.intellidroid.bean.Company
import intellibitz.intellidroid.bean.Country
import intellibitz.intellidroid.util.MainApplicationSingleton
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.ObjectStreamException
import java.util.ArrayList
import java.util.HashSet

open class ContactItem : BaseItem {

    var isIncludeTypeEquals: Boolean = false
    var isNewGroup: Boolean = false
    var isPinned: Boolean = false
    var version: Int = 0
    var isIntellibitzContact: Int = 0
    var isWorkContact: Int = 0
    var selectedId: String? = null
    @Transient
    var mobiles: JSONArray = JSONArray()
    @Transient
    var emails: JSONArray = JSONArray()
    var intellibitzContacts: HashSet<ContactItem> = HashSet()
    var contactItems: HashSet<ContactItem> = HashSet()
    var infoItems: HashSet<BaseItem> = HashSet()
    var selectedContacts: HashSet<ContactItem> = HashSet()

    var gcmToken: String? = null
    var isGcmTokenSentToCloud: Int = 0
    var token: String? = null
    var device: String? = null
    var otp: String? = null
    var mobile: String? = null
    var signupEmail: String? = null
    var country: Country? = null
    var company: Company? = null
    var companyCode: String? = null
    var countryCode: String? = null
    var countryName: String? = null
    var pwd: String? = null
    var devices: HashSet<String> = HashSet()
    var companies: HashSet<String> = HashSet()
    var accountExists: Int = 0
    var emailURL: String? = null

    constructor() : super()

    constructor(id: String?) : this(id, id, id)

    constructor(id: String?, name: String?, email: String?) : this(id, email, name, email)

    constructor(id: String?, email: String?, name: String?, type: String?) : super(id, name, type) {
        intellibitzId = id
        this.email = email
        typeId = email
    }

    constructor(intellibitzContactItem: ContactItem) {
        dataId = intellibitzContactItem.intellibitzId
        intellibitzId = intellibitzContactItem.intellibitzId
        typeId = intellibitzContactItem.typeId
        name = intellibitzContactItem.name
        profilePic = intellibitzContactItem.profilePic
        isDevice = intellibitzContactItem.isDevice
        isCloud = intellibitzContactItem.isCloud
        isAnonymous = intellibitzContactItem.isAnonymous
        isGroup = intellibitzContactItem.isGroup
        isEmailItem = intellibitzContactItem.isEmailItem
    }

    protected constructor(`in`: Parcel) : super(`in`) {
        token = `in`.readString()
        device = `in`.readString()
        otp = `in`.readString()
        mobile = `in`.readString()
        signupEmail = `in`.readString()
        pwd = `in`.readString()
        companyCode = `in`.readString()
        companyName = `in`.readString()
        countryCode = `in`.readString()
        countryName = `in`.readString()
        emailURL = `in`.readString()
        accountExists = `in`.readInt()
        gcmToken = `in`.readString()
        isGcmTokenSentToCloud = `in`.readInt()
        company = `in`.readParcelable(javaClass.classLoader)
        country = `in`.readParcelable(javaClass.classLoader)
        version = `in`.readInt()
        selectedId = `in`.readString()
        isNewGroup = `in`.readByte().toInt() != 0
        isPinned = `in`.readByte().toInt() != 0
        var size = `in`.readInt()
        for (i in 0 until size) {
            val item: ContactItem? = `in`.readParcelable(javaClass.classLoader)
            if (item != null) intellibitzContacts.add(item)
        }
        size = `in`.readInt()
        for (i in 0 until size) {
            val item: ContactItem? = `in`.readParcelable(javaClass.classLoader)
            if (item != null) contactItems.add(item)
        }
        size = `in`.readInt()
        for (i in 0 until size) {
            val item: BaseItem? = `in`.readParcelable(javaClass.classLoader)
            if (item != null) infoItems.add(item)
        }
        size = `in`.readInt()
        for (i in 0 until size) {
            val item: ContactItem? = `in`.readParcelable(javaClass.classLoader)
            if (item != null) selectedContacts.add(item)
        }
        try {
            val emailsStr = `in`.readString()
            if (emailsStr != null) emails = JSONArray(emailsStr)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        try {
            val mobilesStr = `in`.readString()
            if (mobilesStr != null) mobiles = JSONArray(mobilesStr)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun isGcmTokenSentToCloud(): Boolean = isGcmTokenSentToCloud != 0

    fun setGcmTokenSentToCloud(sent: Boolean) {
        this.isGcmTokenSentToCloud = if (sent) 1 else 0
    }

    fun setDevices(devices: Set<String>) {
        this.devices.clear()
        this.devices.addAll(devices)
    }

    fun setCompanies(companies: Set<String>) {
        this.companies.clear()
        this.companies.addAll(companies)
    }

    fun getEmailAddress(): String? {
        if (email == null) {
            val iterator = contactItems.iterator()
            if (iterator.hasNext()) {
                val next = iterator.next()
                return next.email
            }
        }
        return email
    }

    fun removeEmail(id: Long): Boolean {
        val items = contactItems.toTypedArray()
        for (contactItem in items) {
            if (contactItem._id == id) {
                contactItems.remove(contactItem)
            }
        }
        return false
    }

    fun removeEmail(emailVal: String): Boolean {
        val items = contactItems.toTypedArray()
        for (contactItem in items) {
            if (contactItem.email == emailVal) {
                contactItems.remove(contactItem)
            }
        }
        return false
    }

    fun getEmailId(item: String): Long {
        val em = getEmail(item)
        return em?._id ?: 0L
    }

    fun addEmail(emailVal: String?, code: String?) {
        val userEmailItem = ContactItem(emailVal, emailVal, emailVal).apply {
            emailCode = code
        }
        contactItems.add(userEmailItem)
    }

    fun addEmail(item: ContactItem) {
        contactItems.add(item)
    }

    fun getEmail(item: String): ContactItem? {
        for (em in contactItems) {
            if (em.email == item) {
                return em
            }
        }
        return null
    }

    fun setMobiles(mobilesStr: String?) {
        if (mobilesStr.isNullOrEmpty()) {
            this.mobiles = JSONArray()
            return
        }
        try {
            this.mobiles = JSONArray(mobilesStr)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun setMobiles(mobilesList: Collection<String>) {
        this.mobiles = MainApplicationSingleton.createsJSONArrayFromCollection(mobilesList)
    }

    fun setEmails(emailsStr: String?) {
        if (emailsStr.isNullOrEmpty()) {
            this.emails = JSONArray()
            return
        }
        try {
            this.emails = JSONArray(emailsStr)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun setEmails(emailsList: Collection<String>) {
        this.emails = MainApplicationSingleton.createsJSONArrayFromCollection(emailsList)
    }

    fun addContact(contactItem: ContactItem): ContactItem {
        contactItems.add(contactItem)
        return contactItem
    }

    val latestInfo: BaseItem?
        get() {
            if (infoItems.isEmpty()) return null
            return infoItems.iterator().next()
        }

    fun isEmpty(): Boolean = contactItems.isEmpty()

    fun setContactItems(items: Collection<ContactItem>?) {
        if (items == null) {
            contactItems.clear()
            return
        }
        contactItems.addAll(items)
    }

    fun getContactItem(id: String): ContactItem? {
        if (contactItems.isEmpty()) return null
        for (item in contactItems) {
            if (item.dataId == id) return item
        }
        return null
    }

    fun getContactsAsArray(): Array<String> {
        val ids = Array(contactItems.size) { "" }
        var i = 0
        for (item in contactItems) {
            val s = item.intellibitzId
            ids[i++] = s ?: ""
        }
        return ids
    }

    fun getContactsAsJsonObjectArray(): JSONArray {
        val jsonArray = JSONArray()
        if (contactItems.isEmpty()) return jsonArray
        for (item in contactItems) {
            val uid = item.intellibitzId
            if (!TextUtils.isEmpty(uid)) {
                try {
                    val jsonObject = JSONObject().apply {
                        put("uid", uid)
                        put("type", "user")
                    }
                    jsonArray.put(jsonObject)
                } catch (e: JSONException) {
                    Log.e(TAG, TAG + e.message)
                }
            }
        }
        return jsonArray
    }

    fun getContactsNameAsArray(): Array<String> {
        val ids = Array(contactItems.size) { "" }
        var i = 0
        for (item in contactItems) {
            val s = item.name
            ids[i++] = s ?: ""
        }
        return ids
    }

    fun setSelectedContacts(selectedContacts: HashSet<ContactItem>?) {
        this.selectedContacts.clear()
        if (selectedContacts != null) {
            this.selectedContacts.addAll(selectedContacts)
        }
    }

    fun mergeSelectedContacts() {
        for (selected in selectedContacts) {
            if (!contactItems.contains(selected)) {
                contactItems.add(selected)
            }
        }
        selectedContacts.clear()
    }

    @Throws(CloneNotSupportedException::class)
    override fun clone(): Any {
        val copy = super.clone() as ContactItem
        val items = copy.contactItems
        val ecopy1 = HashSet<ContactItem>(items.size)
        for (item in items) {
            ecopy1.add(item.clone() as ContactItem)
        }
        copy.contactItems = ecopy1
        return copy
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ContactItem
        if (!TextUtils.isEmpty(intellibitzId)) {
            if (isIncludeTypeEquals) {
                return (type + intellibitzId) == (that.type + that.intellibitzId)
            }
            return intellibitzId == that.intellibitzId
        } else if (deviceContactId > 0) {
            return deviceContactId == that.deviceContactId
        } else if (!TextUtils.isEmpty(dataId)) {
            if (isIncludeTypeEquals) {
                return (type + dataId) == (that.type + that.dataId)
            }
            return dataId == that.dataId
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        if (!TextUtils.isEmpty(intellibitzId)) {
            if (isIncludeTypeEquals) {
                return (type + intellibitzId).hashCode()
            }
            return intellibitzId.hashCode()
        } else if (deviceContactId > 0) {
            return (deviceContactId xor (deviceContactId ushr 32)).toInt()
        } else if (!TextUtils.isEmpty(dataId)) {
            if (isIncludeTypeEquals) {
                return (type + dataId).hashCode()
            }
            return 31 * (dataId?.hashCode() ?: 0)
        }
        return super.hashCode()
    }

    override fun toString(): String {
        return "ContactItem{" +
                "token='" + token + '\'' +
                ", device='" + device + '\'' +
                ", otp='" + otp + '\'' +
                ", mobile='" + mobile + '\'' +
                ", country=" + country +
                ", countryCode='" + countryCode + '\'' +
                ", pwd='" + pwd + '\'' +
                ", emails=" + contactItems +
                ", devices=" + devices +
                ", companies=" + companies +
                ", countryName='" + countryName + '\'' +
                ", accountExists=" + accountExists +
                ", email='" + email + '\'' +
                ", emailURL='" + emailURL + '\'' +
                ", newGroup=" + isNewGroup +
                '}' + super.toString()
    }

    @Throws(IOException::class)
    private fun writeObject(out: java.io.ObjectOutputStream) {
        out.defaultWriteObject()
        out.writeObject(mobiles.toString())
        out.writeObject(emails.toString())
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(`in`: java.io.ObjectInputStream) {
        `in`.defaultReadObject()
        setMobiles(`in`.readObject() as? String)
        setEmails(`in`.readObject() as? String)
    }

    @Throws(ObjectStreamException::class)
    private fun readObjectNoData() {}

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeString(token)
        dest.writeString(device)
        dest.writeString(otp)
        dest.writeString(mobile)
        dest.writeString(signupEmail)
        dest.writeString(pwd)
        dest.writeString(companyCode)
        dest.writeString(companyName)
        dest.writeString(countryCode)
        dest.writeString(countryName)
        dest.writeString(emailURL)
        dest.writeInt(accountExists)
        dest.writeString(gcmToken)
        dest.writeInt(isGcmTokenSentToCloud)
        dest.writeParcelable(company, 0)
        dest.writeParcelable(country, 0)
        dest.writeInt(version)
        dest.writeString(selectedId)
        dest.writeByte((if (isNewGroup) 1 else 0).toByte())
        dest.writeByte((if (isPinned) 1 else 0).toByte())
        dest.writeInt(intellibitzContacts.size)
        for (item in intellibitzContacts) {
            dest.writeParcelable(item, flags)
        }
        dest.writeInt(contactItems.size)
        for (item in contactItems) {
            dest.writeParcelable(item, flags)
        }
        dest.writeInt(infoItems.size)
        for (item in infoItems) {
            dest.writeParcelable(item, flags)
        }
        dest.writeInt(selectedContacts.size)
        for (item in selectedContacts) {
            dest.writeParcelable(item, flags)
        }
        dest.writeString(emails.toString())
        dest.writeString(mobiles.toString())
    }

    override fun describeContents(): Int = 0

    open class ContactItemComparator<T : ContactItem> : BaseItemComparator<T> {
        constructor() : super()
        constructor(sortMode: SORT_MODE) : super(sortMode)
        override fun compare(lhs: T, rhs: T): Int = super.compare(lhs, rhs)
    }

    companion object {
        const val TAG = "ContactItem"
        const val USER_CONTACT = "UserItem"
        const val EMAIL_CONTACT = "EmailItem"
        const val COMPANY_CONTACT = "CompanyItem"
        const val DEVICE_CONTACT = "DeviceContactItem"
        const val WORK_CONTACT = "WorkContactItem"
        const val INTELLIBITZ_CONTACT = "IntellibitzContactItem"

        @JvmField
        val CREATOR: Parcelable.Creator<ContactItem> = object : Parcelable.Creator<ContactItem> {
            override fun createFromParcel(`in`: Parcel): ContactItem = ContactItem(`in`)
            override fun newArray(size: Int): Array<ContactItem?> = arrayOfNulls(size)
        }
    }
}
