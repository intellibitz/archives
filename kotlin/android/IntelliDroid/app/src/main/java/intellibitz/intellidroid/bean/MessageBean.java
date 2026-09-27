package intellibitz.intellidroid.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 */
public class MessageBean
//        extends
//        MessageItem
        implements BaseBean,
        Parcelable {

    public static final Creator<MessageBean> CREATOR = new Creator<MessageBean>() {
        @Override
        public MessageBean createFromParcel(Parcel in) {
            return new MessageBean(in);
        }

        @Override
        public MessageBean[] newArray(int size) {
            return new MessageBean[size];
        }
    };
    private long contactThreadId;
    private String contactThreadDataId;
    private String contactThreadName;
    private String contactThreadPic;
    private String contactThreadType;
    private long _id;
    private String id;
    private String docType;
    private String name;
    private String rev;
    private String type;
    private String toType;
    private String docOwnerEmail;
    private String docOwner;
    private String docSenderEmail;
    private String fromUid;
    private String toUid;
    private String toChatUid;
    private String chatId;
    private String docSender;
    private int pendingDocs;
    private int unreadCount;
    private int hasAttachments;
    private String subject;
    private String latestMessageText;
    private long latestMessageTimestamp;
    private int read;
    private int delivered;
    private String from;
    private String to;
    private String cc;
    private String bcc;
    private long timestamp;
    private String dateTime;
    private String profilePic;
    private boolean typing = false;
    private boolean flagged = false;
    private String typingText;

    public MessageBean() {
        super();
    }

    protected MessageBean(Parcel in) {
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(int flagged) {
        this.flagged = flagged > 0;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public String getTypingText() {
        return typingText;
    }

    public void setTypingText(String typingText) {
        this.typingText = typingText;
    }

    public long getContactThreadId() {
        return contactThreadId;
    }

    public void setContactThreadId(long contactThreadId) {
        this.contactThreadId = contactThreadId;
    }

    public String getContactThreadDataId() {
        return contactThreadDataId;
    }

    public void setContactThreadDataId(String contactThreadDataId) {
        this.contactThreadDataId = contactThreadDataId;
    }

    public String getContactThreadName() {
        return contactThreadName;
    }

    public void setContactThreadName(String contactThreadName) {
        this.contactThreadName = contactThreadName;
    }

    public String getContactThreadPic() {
        return contactThreadPic;
    }

    public void setContactThreadPic(String contactThreadPic) {
        this.contactThreadPic = contactThreadPic;
    }

    public String getContactThreadType() {
        return contactThreadType;
    }

    public void setContactThreadType(String contactThreadType) {
        this.contactThreadType = contactThreadType;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDataId() {
        return id;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRev() {
        return rev;
    }

    public void setRev(String rev) {
        this.rev = rev;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getToType() {
        return toType;
    }

    public void setToType(String toType) {
        this.toType = toType;
    }

    public String getDocOwnerEmail() {
        return docOwnerEmail;
    }

    public void setDocOwnerEmail(String docOwnerEmail) {
        this.docOwnerEmail = docOwnerEmail;
    }

    public String getDocOwner() {
        return docOwner;
    }

    public void setDocOwner(String docOwner) {
        this.docOwner = docOwner;
    }

    public String getDocSenderEmail() {
        return docSenderEmail;
    }

    public void setDocSenderEmail(String docSenderEmail) {
        this.docSenderEmail = docSenderEmail;
    }

    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

    public String getToUid() {
        return toUid;
    }

    public void setToUid(String toUid) {
        this.toUid = toUid;
    }

    public String getToChatUid() {
        return toChatUid;
    }

    public void setToChatUid(String toChatUid) {
        this.toChatUid = toChatUid;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getDocSender() {
        return docSender;
    }

    public void setDocSender(String docSender) {
        this.docSender = docSender;
    }

    public int getPendingDocs() {
        return pendingDocs;
    }

    public void setPendingDocs(int pendingDocs) {
        this.pendingDocs = pendingDocs;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public boolean getHasAttachments() {
        return 0 != hasAttachments;
    }

    public void setHasAttachments(int hasAttachments) {
        this.hasAttachments = hasAttachments;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getLatestMessageText() {
        return latestMessageText;
    }

    public void setLatestMessageText(String latestMessageText) {
        this.latestMessageText = latestMessageText;
    }

    public long getLatestMessageTimestamp() {
        return latestMessageTimestamp;
    }

    public void setLatestMessageTimestamp(long latestMessageTimestamp) {
        this.latestMessageTimestamp = latestMessageTimestamp;
    }

    public int getRead() {
        return read;
    }

    public int getDelivered() {
        return delivered;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
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

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public boolean isGroupChat() {
        return ("GROUP".equalsIgnoreCase(getToType()));
    }

    public boolean isChat() {
        return "CHAT".equalsIgnoreCase(getType());
    }

    public boolean isEmail() {
        return "EMAIL".equalsIgnoreCase(getType());
    }

    public boolean isRead() {
        return 0 != read;
    }

    public void setRead(int read) {
        this.read = read;
    }

    public boolean isDelivered() {
        return 0 != delivered;
    }

    public void setDelivered(int delivered) {
        this.delivered = delivered;
    }

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public static class MessageBeanComparator extends
            BaseItemComparator<MessageBean> {

        public MessageBeanComparator(SORT_MODE sortMode) {
            super(sortMode);
        }

    }

}
