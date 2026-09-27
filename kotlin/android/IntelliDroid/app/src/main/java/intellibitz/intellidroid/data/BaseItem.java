package intellibitz.intellidroid.data;

import android.os.Parcel;
import android.os.Parcelable;

import intellibitz.intellidroid.bean.BaseBean;
import intellibitz.intellidroid.bean.BaseBean;

import java.io.Serializable;

import androidx.annotation.NonNull;
import intellibitz.intellidroid.bean.BaseBean;

/**
 * A base item representing a piece of content in intellibitz.
 */
public class BaseItem implements
        Serializable,
        Cloneable,
        Parcelable,
        BaseBean,
        Comparable<BaseItem> {

    public static final String THREAD = "THREAD";
    public static final String MSG = "MSG";
    public static final String CHAT = "CHAT";
    public static final String EMAIL = "EMAIL";
    public static final String USER = "USER";
    public static final String GROUP = "GROUP";
    public static final String NEST = "NEST";
    public static final String STACK = "STACK";
    public static final String DRAFT = "DRAFT";
    public static final String SCHEDULE = "SCHEDULE";
    public static final Creator<BaseItem> CREATOR = new Creator<BaseItem>() {
        @Override
        public BaseItem createFromParcel(Parcel in) {
            return new BaseItem(in);
        }

        @Override
        public BaseItem[] newArray(int size) {
            return new BaseItem[size];
        }
    };
    //    domain data dataId
    protected String dataId;
    /**
     * chat contacts will have a valid intellibitz id, emailItem contacts to set this with id
     */
    protected String intellibitzId;
    //    the local contact id in the device
//    the primary key in all respects, since the device is the master
    protected long deviceContactId = 0;
    protected String type;
    protected String user;
    protected String mailbox;
    protected int seqNo;
    //    database dataId
    protected long _id;
    //    an item can be threaded (grouped)
    protected String threadId;
    //    the ref id for the thread parent
    protected String threadIdRef;
    protected String dataRev;
    protected String threadIdParts;
    protected String groupId;
    protected String groupIdRef;
    protected boolean group = false;
    protected boolean emailItem = false;
    protected boolean anonymous = false;
    protected boolean device = false;
    protected boolean cloud = false;
    protected String firstName;
    protected String lastName;
    protected String displayName;
    //
    protected String companyName;
    protected String companyId;
    protected String email;
    protected String emailCode = null;
    protected int active = 0;
    //    device ids
    protected String deviceId;
    protected String deviceName;
    protected String deviceRef;
    protected String name;
    protected String status;
    protected String profilePic;
    protected String cloudPic;
    protected String typeId;
    protected String docOwner;
    protected String docType;
    protected String baseType;
    //    cloud date time in long
    protected long timestamp;
    //    sql datetime
    protected String dateTime;

    public BaseItem() {
        super();
    }

    public BaseItem(String dataId) {
        this(dataId, null);
    }

    public BaseItem(String dataId, String name) {
        this(dataId, name, null);
    }

    public BaseItem(String dataId, String name, String type) {
        this.dataId = dataId;
        this.name = name;
        this.type = type;
    }

    public BaseItem(String dataId, String threadIdRef, String name, String type) {
        this.dataId = dataId;
        this.name = name;
        this.type = type;
        this.threadIdRef = threadIdRef;
    }

    protected BaseItem(Parcel in) {
        _id = in.readLong();
        threadId = in.readString();
        threadIdRef = in.readString();
        threadIdParts = in.readString();
        groupId = in.readString();
        groupIdRef = in.readString();
        dataId = in.readString();
        typeId = in.readString();
        dataRev = in.readString();
        intellibitzId = in.readString();
        group = in.readByte() != 0;     //== true if byte != 0
        emailItem = in.readByte() != 0;     //== true if byte != 0
        anonymous = in.readByte() != 0;     //== true if byte != 0
        device = in.readByte() != 0;     //== true if byte != 0
        cloud = in.readByte() != 0;     //== true if byte != 0
//        mobileItem = in.readParcelable(MobileItem.class.getClassLoader());
//        emailItem = in.readParcelable(EmailItem.class.getClassLoader());
//        deviceContactItem = in.readParcelable(DeviceContactItem.class.getClassLoader());
        user = in.readString();
        mailbox = in.readString();
        seqNo = in.readInt();
        firstName = in.readString();
        lastName = in.readString();
        displayName = in.readString();
        email = in.readString();
        emailCode = in.readString();
        companyName = in.readString();
        companyId = in.readString();
        active = in.readInt();
        deviceContactId = in.readLong();
        deviceId = in.readString();
        deviceName = in.readString();
        deviceRef = in.readString();
        name = in.readString();
        type = in.readString();
        docType = in.readString();
        baseType = in.readString();
        docOwner = in.readString();
        status = in.readString();
        profilePic = in.readString();
        cloudPic = in.readString();
        timestamp = in.readLong();
        dateTime = in.readString();
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMailbox() {
        return mailbox;
    }

    public void setMailbox(String mailbox) {
        this.mailbox = mailbox;
    }

    public int getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getThreadIdRef() {
        return threadIdRef;
    }

    public void setThreadIdRef(String threadIdRef) {
        this.threadIdRef = threadIdRef;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getThreadIdParts() {
        return threadIdParts;
    }

    public void setThreadIdParts(String threadIdParts) {
        this.threadIdParts = threadIdParts;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupIdRef() {
        return groupIdRef;
    }

    public void setGroupIdRef(String groupIdRef) {
        this.groupIdRef = groupIdRef;
    }

    public String getIntellibitzId() {
        return intellibitzId;
    }

    public void setIntellibitzId(String intellibitzId) {
        this.intellibitzId = intellibitzId;
    }

    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public void setGroup(int group) {
        this.group = (group > 0);
    }

    public boolean isEmailItem() {
        return emailItem;
    }

    public void setEmailItem(boolean emailItem) {
        this.emailItem = emailItem;
    }

    public void setEmailItem(int email) {
        this.emailItem = (email > 0);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailCode() {
        return emailCode;
    }

    public void setEmailCode(String emailCode) {
        this.emailCode = emailCode;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public void setAnonymous(int anonymous) {
        this.anonymous = anonymous > 0;
    }

    public boolean isDevice() {
        return device;
    }

    public void setDevice(boolean device) {
        this.device = device;
    }

    public void setDevice(int device) {
        this.device = device > 0;
    }

    public boolean isCloud() {
        return cloud;
    }

    public void setCloud(boolean cloud) {
        this.cloud = cloud;
    }

    public void setCloud(int cloud) {
        this.cloud = cloud > 0;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getDeviceContactId() {
        return deviceContactId;
    }

    public void setDeviceContactId(long deviceContactId) {
        this.deviceContactId = deviceContactId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceRef() {
        return deviceRef;
    }

    public void setDeviceRef(String deviceRef) {
        this.deviceRef = deviceRef;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDocOwner() {
        return docOwner;
    }

    public void setDocOwner(String docOwner) {
        this.docOwner = docOwner;
    }

    public String getBaseType() {
        return baseType;
    }

    public void setBaseType(String baseType) {
        this.baseType = baseType;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getDataRev() {
        return dataRev;
    }

    public void setDataRev(String dataRev) {
        this.dataRev = dataRev;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getCloudPic() {
        return cloudPic;
    }

    public void setCloudPic(String cloudPic) {
        this.cloudPic = cloudPic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseItem infoItem = ((BaseItem) o);
        if (threadIdRef != null)
            return threadIdRef.equals(infoItem.threadIdRef);
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        if (threadIdRef != null)
            return threadIdRef.hashCode();
        return super.hashCode();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "BaseItem{" +
                "_id=" + _id +
                ", threadId='" + threadId + '\'' +
                ", threadIdRef='" + threadIdRef + '\'' +
                ", threadIdParts='" + threadIdParts + '\'' +
                ", groupId='" + groupId + '\'' +
                ", groupIdRef='" + groupIdRef + '\'' +
                ", dataId='" + dataId + '\'' +
                ", dataRev='" + dataRev + '\'' +
                ", deviceId" + deviceId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceRef='" + deviceRef + '\'' +
                ", user='" + user + '\'' +
                ", mailbox='" + mailbox + '\'' +
                ", seqNo='" + seqNo + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", emailCode='" + emailCode + '\'' +
                ", email=" + email +
                ", active=" + active +
                ", profilePic=" + profilePic +
                ", cloudPic=" + cloudPic +
                ", timestamp=" + timestamp +
                ", dateTime='" + dateTime + '\'' +
                '}';
    }

    @Override
    public int compareTo(@NonNull BaseItem another) {
        if (this._id == another._id)
            return 0;
        if (this._id > another._id)
            return 1;
        return -1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeString(threadId);
        dest.writeString(threadIdRef);
        dest.writeString(threadIdParts);
        dest.writeString(groupId);
        dest.writeString(groupIdRef);
        dest.writeString(dataId);
        dest.writeString(typeId);
        dest.writeString(dataRev);
        dest.writeString(intellibitzId);
        dest.writeByte((byte) (group ? 1 : 0));     //if true, byte == 1
        dest.writeByte((byte) (emailItem ? 1 : 0));     //if true, byte == 1
        dest.writeByte((byte) (anonymous ? 1 : 0));     //if true, byte == 1
        dest.writeByte((byte) (device ? 1 : 0));     //if true, byte == 1
        dest.writeByte((byte) (cloud ? 1 : 0));     //if true, byte == 1
//        dest.writeParcelable(mobileItem, flags);
//        dest.writeParcelable(emailItem, flags);
//        dest.writeParcelable(deviceContactItem, flags);
        dest.writeString(user);
        dest.writeString(mailbox);
        dest.writeInt(seqNo);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(displayName);
        dest.writeString(email);
        dest.writeString(emailCode);
        dest.writeString(companyName);
        dest.writeString(companyId);
        dest.writeInt(active);
        dest.writeLong(deviceContactId);
        dest.writeString(deviceId);
        dest.writeString(deviceName);
        dest.writeString(deviceRef);
        dest.writeString(name);
        dest.writeString(type);
        dest.writeString(docType);
        dest.writeString(baseType);
        dest.writeString(docOwner);
        dest.writeString(status);
        dest.writeString(profilePic);
        dest.writeString(cloudPic);
        dest.writeLong(timestamp);
        dest.writeString(dateTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
