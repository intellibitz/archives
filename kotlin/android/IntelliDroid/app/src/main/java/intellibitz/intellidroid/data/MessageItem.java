package intellibitz.intellidroid.data;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.util.Rfc822Token;
import android.util.Log;

import intellibitz.intellidroid.bean.BaseItemComparator;
import intellibitz.intellidroid.bean.BaseItemComparator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 */
public class MessageItem extends
        BaseItem implements
        Parcelable {
    public static final String TAG = "MessageItem";
    public static final String EMAIL_MESSAGE = "EmailMessageItem";
    public static final String ATTACHMENT_MESSAGE = "AttachmentItem";
    public static final String SCHEDULE_MESSAGE = "ScheduleItem";
    public static final String FEED_MESSAGE = "FeedItem";
    public static final Creator<MessageItem> CREATOR = new Creator<MessageItem>() {
        @Override
        public MessageItem createFromParcel(Parcel in) {
            return new MessageItem(in);
        }

        @Override
        public MessageItem[] newArray(int size) {
            return new MessageItem[size];
        }
    };
    protected String from = "";
    protected String to = "";
    protected String cc = "";
    protected String bcc = "";
    protected String fromUid;
    protected String toUid;
    protected String chatId;
    protected String toChatUid;
    protected String subject;
    protected int defaultFolder = 0;
    protected String folderCode;
    protected ContactItem contactItem;
    //    the new message attachments
    protected Set<MessageItem> attachments = new HashSet<>();
    //    the recipient of this message
    protected String docOwnerEmail;
    //    the sender, not available in cloud domain.. but easy to access
    protected String docSenderEmail;
    protected String docSender;
    protected String fromEmail;
    protected String fromName;
    protected String messageDirection;
    protected String messageType;
    protected String text;
    protected String fullText;
    protected String html;
    protected String messageAttachId;
    protected boolean readyToSend = false;
    protected String toType;
    protected int pendingDocs = 0;
    protected int hasAttachments = 0;
    protected int read = 0;
    protected int delivered = 0;
    protected int locked = 0;
    protected boolean broadcast;
    protected boolean noText;
    protected boolean flagged;
    protected String msgRef;
    protected String chatMsgRef;
    protected String flags;
    //    for camera to take photo, to add to attachments
    protected Uri photoFileUri = null;
    protected Uri videoFileUri = null;
    protected Uri audioFileUri = null;
    //    for shared content coming from the device
    protected String sharedText = null;
    protected Uri sharedUri = null;
    protected ArrayList<Uri> sharedUris = new ArrayList<>();
    protected ContactItem sharedDeviceContactItem;
    protected int unreadCount = 0;
    protected String latestMessageText = null;
    protected long latestMessageTimestamp = 0;
    protected boolean typing = false;
    protected String typingText;
    //    contained object - used for presentation displays - relations persisted by individual content
//    all messages in this thread
    protected Set<MessageItem> messages = new HashSet<>();
    //    protected List<MessageItem> messages2 = new ArrayList<>();
    //    the last messageItemStack.. or the fresh new messageItemStack that will be created
    protected Stack<MessageItem> messageItemStack = new Stack<>();
    protected ConcurrentLinkedQueue<MessageItem> messageItemConcurrentLinkedQueue =
            new ConcurrentLinkedQueue<>();
    private String description;
    private String msgAttachID;
    private String partID;
    private String subType;
    private String encoding;
    private String language;
    private int size;
    private String md5;
    private String downloadURL;

    public MessageItem() {
        super();
    }

    public MessageItem(String id) {
        this(id, id, null);
    }

    public MessageItem(String id, String name, String type) {
        super(id, name, type);
        this.description = name;
    }

    protected MessageItem(Parcel in) {
        super(in);
        msgAttachID = in.readString();
        partID = in.readString();
        subType = in.readString();
        encoding = in.readString();
        language = in.readString();
        md5 = in.readString();
        downloadURL = in.readString();
        description = in.readString();
        size = in.readInt();
        readyToSend = in.readByte() != 0;     // == true if byte != 0
        fromEmail = in.readString();
        fromName = in.readString();
        messageDirection = in.readString();
        messageType = in.readString();
        text = in.readString();
        fullText = in.readString();
        html = in.readString();
        messageAttachId = in.readString();
        toType = in.readString();
        docOwnerEmail = in.readString();
        fromUid = in.readString();
        toUid = in.readString();
        chatId = in.readString();
        toChatUid = in.readString();
        docSenderEmail = in.readString();
        docSender = in.readString();
        pendingDocs = in.readInt();
        hasAttachments = in.readInt();
        read = in.readInt();
        delivered = in.readInt();
        locked = in.readInt();
        broadcast = in.readByte() != 0;     //== true if byte != 0
        noText = in.readByte() != 0;     //== true if byte != 0
        flagged = in.readByte() != 0;     //== true if byte != 0
        subject = in.readString();
        msgRef = in.readString();
        chatMsgRef = in.readString();
        from = in.readString();
        to = in.readString();
        cc = in.readString();
        bcc = in.readString();
        flags = in.readString();
        contactItem = in.readParcelable(getClass().getClassLoader());
/*
        in.readTypedList(emails2, EmailItem.CREATOR);
        emails.clear();
        emails.addAll(emails2);
        in.readTypedList(attachments2, MessageItem.CREATOR);
        attachments.clear();
        attachments.addAll(attachments2);
*/
/*
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            EmailItem item = in.readParcelable(getClass().getClassLoader());
            emails.add(item);
        }
*/
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            MessageItem item = in.readParcelable(getClass().getClassLoader());
            attachments.add(item);
        }
        typing = in.readByte() != 0;     //typing == true if byte != 0
        typingText = in.readString();
        latestMessageText = in.readString();
        latestMessageTimestamp = in.readLong();
        unreadCount = in.readInt();
        defaultFolder = in.readInt();
        folderCode = in.readString();
        photoFileUri = in.readParcelable(getClass().getClassLoader());
        videoFileUri = in.readParcelable(getClass().getClassLoader());
        audioFileUri = in.readParcelable(getClass().getClassLoader());
        sharedText = in.readString();
        sharedUri = in.readParcelable(getClass().getClassLoader());
        sharedDeviceContactItem = in.readParcelable(getClass().getClassLoader());
        in.readTypedList(sharedUris, Uri.CREATOR);
        boolean isMessageInStack = in.readByte() != 0;     //typing == true if byte != 0
        if (isMessageInStack)
            pushMessageInStack((MessageItem) in.readParcelable(getClass().getClassLoader()));
        size = in.readInt();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                MessageItem item = in.readParcelable(getClass().getClassLoader());
                messages.add(item);
            }
        }
        size = in.readInt();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                MessageItem item = in.readParcelable(getClass().getClassLoader());
                messageItemConcurrentLinkedQueue.add(item);
            }
        }
    }

    public static String getSingleAddress(String from, String to, String cc,
                                          String userEmail, String userName) {
        String result = getSingleAddress(from, to, cc, userEmail);
        if (result != null && userName != null &&
                userName.length() > 0 && result.contains(userName))
            result = result.replace(userName, " me");
        return result;
    }

    public static String getSingleAddress(String from, String to, String cc, String userEmail) {
        String result = getSingleAddress(from, to, cc);
        if (result != null && userEmail != null &&
                userEmail.length() > 0 && result.contains(userEmail))
            result = result.replace(userEmail, " me");
        return result;
    }

    public static String getSingleAddress(String from, String to, String cc) {
        String result = from;
        if (to != null && to.length() > 0) {
            if (null == result || "".equals(result)) {
                result = to;
            } else {
                result += "," + to;
            }
        }
        if (cc != null && cc.length() > 0) {
            if (null == result || "".equals(result)) {
                result = cc;
            } else {
                result += "," + cc;
            }
        }
        return result;
    }

    public static void setMessageThreadEmailAddress(MessageItem messageItem,
                                                    String to, String cc,
                                                    String bcc) {
        ContactItem contactThreadItem = messageItem.getContactItem();
        if (null == contactThreadItem) {
            contactThreadItem = new ContactItem();
            messageItem.setContactItem(contactThreadItem);
        }
        if (to != null) {
            messageItem.setTo(to);
            ContactItem contactItem = new ContactItem();
            contactItem.setDataId(to);
            contactItem.setTypeId(to);
            contactItem.setIntellibitzId(to);
            contactItem.setName(to);
            contactItem.setType("to");
            contactThreadItem.addContact(contactItem);
        }
        if (cc != null) {
            messageItem.setCc(cc);
            ContactItem contactItem = new ContactItem();
            contactItem.setDataId(cc);
            contactItem.setTypeId(cc);
            contactItem.setIntellibitzId(cc);
            contactItem.setName(cc);
            contactItem.setType("cc");
            contactThreadItem.addContact(contactItem);
        }
        if (bcc != null) {
            messageItem.setBcc(bcc);
            ContactItem contactItem = new ContactItem();
            contactItem.setDataId(bcc);
            contactItem.setTypeId(bcc);
            contactItem.setIntellibitzId(bcc);
            contactItem.setName(bcc);
            contactItem.setType("bcc");
            contactThreadItem.addContact(contactItem);
        }
    }

    public static void setMessageThreadEmailAddress(MessageItem messageItem,
                                                    Rfc822Token[] to, Rfc822Token[] cc,
                                                    Rfc822Token[] bcc) {
        String sto = "";
        String scc = "";
        String sbcc = "";

        int i = 1;
        if (to != null && to.length > 0) {
            for (Rfc822Token s : to) {
                String address = s.getAddress();
                setMessageThreadEmailAddress(messageItem, address, null, null);
                sto += address;
                if (++i < to.length) {
                    sto += ",";
                }
            }
        }
        i = 1;
        if (cc != null && cc.length > 0) {
            for (Rfc822Token s : cc) {
                String address = s.getAddress();
                setMessageThreadEmailAddress(messageItem, null, address, null);
                scc += address;
                if (++i < cc.length) {
                    scc += ",";
                }
            }
        }
        i = 1;
        if (bcc != null && bcc.length > 0) {
            for (Rfc822Token s : bcc) {
                String address = s.getAddress();
                setMessageThreadEmailAddress(messageItem, null, null, address);
                sbcc += address;
                if (++i < bcc.length) {
                    sbcc += ",";
                }
            }
        }
        messageItem.setTo(sto);
        messageItem.setCc(scc);
        messageItem.setBcc(sbcc);
    }

    public String getMessageAttachId() {
        return messageAttachId;
    }

    public void setMessageAttachId(String messageAttachId) {
        this.messageAttachId = messageAttachId;
    }

    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getMessageDirection() {
        return messageDirection;
    }

    public void setMessageDirection(String messageDirection) {
        this.messageDirection = messageDirection;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void appendText(String text) {
        if (null == this.text)
            this.text = text;
        else
            this.text += text;
    }

    public void addAttachments(Set<MessageItem> attachments) {
        this.attachments.clear();
        this.attachments.addAll(attachments);
    }

    public MessageItem addAttachment(MessageItem item) {
        attachments.remove(item);
        attachments.add(item);
        return this;
    }

    public MessageItem addAttachment(String id) {
        MessageItem item = new MessageItem(id);
        addAttachment(item);
        return item;
    }

    public MessageItem getAttachment(String id) {
        if (null == attachments || attachments.isEmpty())
            return null;
        for (MessageItem item : attachments) {
            if (item.getDataId().equals(id))
                return item;
        }
        return null;
    }

    public boolean isReadyToSend() {
        return readyToSend;
    }

    public void setReadyToSend(boolean readyToSend) {
        this.readyToSend = readyToSend;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getToType() {
        return toType;
    }

    public void setToType(String toType) {
        this.toType = toType;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getToChatUid() {
        return toChatUid;
    }

    public void setToChatUid(String toChatUid) {
        this.toChatUid = toChatUid;
    }

    public String getMsgRef() {
        return msgRef;
    }

    public void setMsgRef(String msgRef) {
        this.msgRef = msgRef;
    }

    public String getChatMsgRef() {
        return chatMsgRef;
    }

    public void setChatMsgRef(String chatMsgRef) {
        this.chatMsgRef = chatMsgRef;
    }

    public boolean isGroupChat() {
        return (GROUP.equalsIgnoreCase(getToType()));
    }

    public boolean isChat() {
        return CHAT.equalsIgnoreCase(getType());
    }

    public boolean isEmailItem() {
        return EMAIL.equalsIgnoreCase(getType());
    }

    public boolean isDraft() {
        return DRAFT.equals(getDocType());
    }

    public void setDraft() {
        setDocType(DRAFT);
    }

    public boolean isRead() {
        return 0 != read;
    }

    public boolean getRead() {
        return isRead();
    }

    public void setRead(boolean read) {
        this.read = read ? 1 : 0;
    }

    public void setRead(int read) {
        this.read = read;
    }

    public boolean isLocked() {
        return 0 != locked;
    }

    public int getLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked ? 1 : 0;
    }

    public void setLocked(int locked) {
        this.locked = locked;
    }

    public boolean isDelivered() {
        return 0 != delivered;
    }

    public boolean getDelivered() {
        return isDelivered();
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered ? 1 : 0;
    }

    public void setDelivered(int delivered) {
        this.delivered = delivered;
    }

    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }

    public boolean getHasAttachments() {
        return 0 != hasAttachments;
    }

    public void setHasAttachments(int hasAttachments) {
        this.hasAttachments = hasAttachments;
    }

    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public void setBroadcast(int broadcast) {
        this.broadcast = broadcast > 0;
    }

    public void setNoText(boolean noText) {
        this.noText = noText;
    }

    public boolean isNoText() {
        return noText;
    }

    public void setNoText(int noText) {
        this.noText = noText > 0;
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    public Set<MessageItem> getAttachments() {
        return attachments;
    }

    public void setAttachments(Set<MessageItem> attachments) {
        this.attachments = attachments;
    }

    public String getDocSender() {
        return docSender;
    }

    public void setDocSender(String docSender) {
        this.docSender = docSender;
    }

    public String getDocSenderEmail() {
        return docSenderEmail;
    }

    public void setDocSenderEmail(String sender) {
        this.docSenderEmail = sender;
    }

    public String getDocOwnerEmail() {
        return docOwnerEmail;
    }

    public void setDocOwnerEmail(String docOwnerEmail) {
        this.docOwnerEmail = docOwnerEmail;
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

    public int getPendingDocs() {
        return pendingDocs;
    }

    public void setPendingDocs(int pendingDocs) {
        this.pendingDocs = pendingDocs;
    }

    public String getToUid() {
        return toUid;
    }

    public void setToUid(String toUid) {
        this.toUid = toUid;
    }

    public ContactItem getContactItem() {
        return contactItem;
    }

    public void setContactItem(ContactItem contactItem) {
        this.contactItem = contactItem;
        if (contactItem != null) {
            if (contactItem.isGroup()) {
                setToType(GROUP);
            }
        }
    }

    public boolean isNest() {
        return NEST.equals(getDocType());
    }

    public boolean isStack() {
        return STACK.equals(getDocType());
    }

    public void setNest() {
        setBaseType(NEST);
        setDocType(NEST);
    }

    public void setStack() {
        setBaseType(STACK);
        setDocType(STACK);
    }

    public ContactItem getSharedDeviceContactItem() {
        return sharedDeviceContactItem;
    }

    public void setSharedDeviceContactItem(ContactItem sharedDeviceContactItem) {
        this.sharedDeviceContactItem = sharedDeviceContactItem;
    }

    public String getSharedText() {
        return sharedText;
    }

    public void setSharedText(String sharedText) {
        this.sharedText = sharedText;
    }

    public Uri getSharedUri() {
        return sharedUri;
    }

    public void setSharedUri(Uri sharedUri) {
        this.sharedUri = sharedUri;
    }

    public ArrayList<Uri> getSharedUris() {
        return sharedUris;
    }

    public void setSharedUris(ArrayList<Uri> sharedUris) {
        this.sharedUris = sharedUris;
    }

    public Uri getAudioFileUri() {
        return audioFileUri;
    }

    public void setAudioFileUri(Uri audioFileUri) {
        this.audioFileUri = audioFileUri;
    }

    public Uri getPhotoFileUri() {
        return photoFileUri;
    }

    public void setPhotoFileUri(Uri photoFileUri) {
        this.photoFileUri = photoFileUri;
    }

    public Uri getVideoFileUri() {
        return videoFileUri;
    }

    /*
    public List<MessageItem> getMessages2() {
*/
/*
        if (null == messages2) {
            messages2 = new ArrayList<>(0);
        }
        if (messages != null) {
            messages2.clear();
            messages2.addAll(messages);
        }
*//*

        return messages2;
    }

    public void setMessages2(List<MessageItem> messages2) {
        this.messages2 = messages2;
    }

*/

    public void setVideoFileUri(Uri videoFileUri) {
        this.videoFileUri = videoFileUri;
    }

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }

    public String getTypingText() {
        return typingText;
    }

    public void setTypingText(String typingText) {
        this.typingText = typingText;
    }

    public String getLatestMessageText() {
/*
        if (isTyping()){
            return latestMessageText + " is typing..";
        }
*/
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

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public boolean flagStackMessageToSend() {
        MessageItem messageItem = peekMessageInStack();
        if (messageItem != null) {
            messageItem.setReadyToSend(true);
            return true;
        }
        return false;
    }

    public void pushMessageInStack(MessageItem messageItem) {
//        the stack will hold only 1 latest item
        messageItemStack.clear();
        this.messageItemStack.push(messageItem);
    }

    public MessageItem peekMessageInStack() {
        if (!messageItemStack.isEmpty())
            return this.messageItemStack.peek();
        return null;
    }

    public MessageItem popMessage() {
        if (!messageItemStack.isEmpty()) {
//        the stack will hold only 1 latest item
            MessageItem item = messageItemStack.pop();
            messageItemStack.clear();
            return item;
        }
        return null;
    }

    public ConcurrentLinkedQueue<MessageItem> getMessageItemConcurrentLinkedQueue() {
        return messageItemConcurrentLinkedQueue;
    }

    public Set<MessageItem> getMessages() {
        return messages;
    }

    public void setMessages(Set<MessageItem> messages) {
        this.messages = messages;
/*
        messages2.clear();
        messages2.addAll(messages);
*/
    }

    public MessageItem addMessage(MessageItem messageItem) throws CloneNotSupportedException {
        if (null == messageItem) return null;
//        removes, previous entry.. so as to sync with stack
        removeMessage(messageItem);
        messages.add(messageItem);
//        messages2.add(messageItem);
//        the latest message to be pushed in
        pushMessageInStack((MessageItem) messageItem.clone());
        return messageItem;
    }

    public boolean removeMessage(MessageItem messageItem) {
        if (null == messageItem) return false;
        boolean flag = messages.remove(messageItem);
//        messages2.remove(messageItem);
        return flag;
    }

    public MessageItem addMessage(String id) throws CloneNotSupportedException {
        MessageItem messageItem = new MessageItem();
        messageItem.setDataId(id);
//        sets the messageItemStack text, mostly used by the presentation layer
//        messageItem.setText(id);
        return addMessage(messageItem);
    }

    public MessageItem addMessage(String id, String chatId, String msgRef) throws CloneNotSupportedException {
        MessageItem messageItem = new MessageItem();
        messageItem.setDataId(id);
        messageItem.setChatId(chatId);
        messageItem.setChatMsgRef(msgRef);
//        sets the messageItemStack text, mostly used by the presentation layer
//        messageItem.setText(id);
        return addMessage(messageItem);
    }

    public MessageItem getMessage(String id) {
        if (null == messages || messages.isEmpty())
            return null;
        for (MessageItem item : messages) {
            if (item.getDataId().equals(id))
                return item;
        }
        return null;
    }

    public MessageItem getMessageByClientMsgRef(String id) {
        if (null == id || null == messages || messages.isEmpty())
            return null;
        for (MessageItem item : messages) {
            if (id.equals(item.getChatMsgRef()))
                return item;
        }
        return null;
    }

    public String[] getMessageIds() {
        if (null == messages || messages.isEmpty())
            return null;
        String[] ids = new String[messages.size()];
        int i = 0;
        for (MessageItem item : messages) {
            ids[i++] = item.getDataId();
        }
        return ids;
    }

    public String[] getUnreadMessageIds() {
        if (null == messages || messages.isEmpty())
            return null;
        String[] ids = new String[messages.size()];
        int i = 0;
        for (MessageItem item : messages) {
            if (!item.isRead())
                ids[i++] = item.getDataId();
        }
        return ids;
    }

    public MessageItem getAttachment(long id) {
        for (MessageItem messageItem : messages) {
            Set<MessageItem> attachmentItems = messageItem.getAttachments();
            for (MessageItem attachmentItem : attachmentItems) {
                if (id == attachmentItem.get_id()) {
                    return attachmentItem;
                }
            }
        }
        return null;
    }

    public int getDefaultFolder() {
        return defaultFolder;
    }

    public void setDefaultFolder(boolean defaultFolder) {
        this.defaultFolder = defaultFolder ? 1 : 0;
    }

    public void setDefaultFolder(int defaultFolder) {
        this.defaultFolder = defaultFolder;
    }

    public String getFolderCode() {
        return folderCode;
    }

    public void setFolderCode(String folderCode) {
        this.folderCode = folderCode;
    }

    public Object cloneShal() {
        Object copy = null;
        try {
            copy = super.clone();
        } catch (CloneNotSupportedException ignored) {
            Log.e(TAG, ignored.getMessage());
        }
        return copy;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Object object = null;
        try {
//            copy super members
            object = super.clone();
//            deep copies collections
            MessageItem messageItemClone = (MessageItem) object;
// // TODO: 12-07-2016
//            is this object clone redundant
            ContactItem contactItem = messageItemClone.getContactItem();
            if (contactItem != null) {
                ContactItem contactItemClone = (ContactItem) contactItem.clone();
                messageItemClone.setContactItem(contactItemClone);
            }
            Set<MessageItem> items = messageItemClone.getMessages();
            if (items != null) {
                Set<MessageItem> ecopy1 = new HashSet<>(items.size());
                for (MessageItem item : items) {
                    ecopy1.add((MessageItem) item.clone());
                }
                messageItemClone.setMessages(ecopy1);
            }
            Set<MessageItem> attachmentItems = messageItemClone.getAttachments();
            if (attachmentItems != null) {
                Set<MessageItem> acopy1 = new HashSet<>(attachmentItems.size());
                for (MessageItem item : attachmentItems) {
                    acopy1.add((MessageItem) item.clone());
                }
                messageItemClone.setAttachments(acopy1);
            }
        } catch (CloneNotSupportedException ignored) {
            Log.e(TAG, ignored.getMessage());
        }
        return object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseItem baseItem = (BaseItem) o;
// the db id.. will not available for display presentation objects
//        if (_id != baseItem._id) return false;
        String dataId = this.getDataId();
        String chatMsgRef = this.getChatMsgRef();
        if (dataId.equals(this.getChatId()) && !TextUtils.isEmpty(chatMsgRef)) {
//            new msg
            MessageItem messageItem = (MessageItem) baseItem;
            return chatMsgRef.equals(messageItem.getChatMsgRef());
        }
        return dataId.equals(baseItem.getDataId());

    }

    @Override
    public int hashCode() {
// the db id.. will not available for display presentation objects
//        int result = (int) (_id ^ (_id >>> 32));
        String dataId = this.getDataId();
        int hash = 31 * dataId.hashCode();
        String chatMsgRef = this.getChatMsgRef();
        if (dataId.equals(this.getChatId()) && !TextUtils.isEmpty(chatMsgRef)) {
//            new msg
            hash = 31 * chatMsgRef.hashCode();

        }
        return hash;
    }

    @Override
    public String toString() {
        return "MessageItem{" +
                "messages=" + messages +
                "fromEmail='" + fromEmail + '\'' +
                ", fromName='" + fromName + '\'' +
                ", messageDirection='" + messageDirection + '\'' +
                ", messageType='" + messageType + '\'' +
                ", text='" + text + '\'' +
                ", html='" + html + '\'' +
                ", fromUid='" + fromUid + '\'' +
                ", toUid='" + getToUid() + '\'' +
                ", messageAttachId='" + messageAttachId + '\'' +
                ", messageItemStack=" + messageItemStack +
                ", latestMessageText='" + latestMessageText + '\'' +
                ", unreadCount=" + unreadCount +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", cc='" + cc + '\'' +
                ", bcc='" + bcc + '\'' +
                ", toType='" + toType + '\'' +
                ", docOwnerEmail='" + docOwnerEmail + '\'' +
                ", docSenderEmail='" + docSenderEmail + '\'' +
                ", docSender='" + docSender + '\'' +
                ", hasAttachments=" + hasAttachments +
                ", subject='" + subject + '\'' +
//                ", emails=" + emails +
                ", attachments=" + attachments +
                "description='" + description + '\'' +
                ", partID='" + partID + '\'' +
                ", subType='" + subType + '\'' +
                ", encoding='" + encoding + '\'' +
                ", language='" + language + '\'' +
                ", size=" + size +
                ", md5='" + md5 + '\'' +
                ", downloadURL='" + downloadURL + '\'' +
//                ", emails2=" + emails2 +
//                ", attachments2=" + attachments2 +
                '}' + super.toString();
    }

    public MessageItem invoke() {
        if (null == contactItem || contactItem.isEmpty()) return this;
        HashSet<ContactItem> contactItems = contactItem.getContactItems();
        for (ContactItem emailItem : contactItems) {
            String email = emailItem.getTypeId();
            if ("from".equals(emailItem.getType()) && !from.equals(email)) {
/*
                if (!"".equals(to) && !to.contains(email)) {
                    to += ",";
                }
                if (!to.contains(email)) {
                    to += email;
                }
*/
            } else if ("to".equals(emailItem.getType())) {
                if (!TextUtils.isEmpty(to) && !to.contains(email)) {
                    to += ",";
                }
                if (!to.contains(email)) {
                    to += email;
                }
            } else if ("cc".equals(emailItem.getType())) {
                if (!TextUtils.isEmpty(cc) && !cc.contains(email)) {
                    cc += ",";
                }
                if (!cc.contains(email)) {
                    cc += email;
                }
            } else if ("bcc".equals(emailItem.getType())) {
                if (!TextUtils.isEmpty(bcc) && !bcc.contains(email)) {
                    bcc += ",";
                }
                if (!bcc.contains(email)) {
                    bcc += email;
                }
            }
        }
        return this;
    }

    /**
     * composes msg thread contact, from the collection of emails aka email group
     *
     * @return ContactItem the email group contact
     */
    public ContactItem compose() {
        if (null == contactItem || contactItem.isEmpty())
            return contactItem;
// // TODO: 30-06-2016
        /*
        HashSet<ContactItem> contactItems = contactItem.getContactItems();
        Collection<EmailItem> items = getEmails();
        if (items != null && !items.isEmpty()) {
            ContactItem contactItem = new ContactItem();
            contactItem.setDataId(getChatId());
            contactItem.setGroupId(getChatId());
            contactItem.setDataRev(getDataRev());
            contactItem.setType(getType());
            contactItem.setDocOwner(getDocOwner());
            contactItem.setName(getName());
            contactItem.setProfilePic(getProfilePic());
            contactItem.setGroup(true);
            contactItem.setEmailItem(true);
            contactItem.setTimestamp(getTimestamp());
            EmailItem[] emailItems = items.toArray(new EmailItem[0]);
            for (EmailItem emailItem : emailItems) {
                IntellibitzContactItem intellibitzContactItem = new IntellibitzContactItem(emailItem);
                intellibitzContactItem.setEmailItem(true);
                ContactItem msgContactItem = new ContactItem(intellibitzContactItem);
                contactItem.addContact(msgContactItem);
            }
            setContactItem(contactItem);
        }
*/
        return getContactItem();
    }

    public ContactItem compose(Rfc822Token[] to, Rfc822Token[] cc,
                               Rfc822Token[] bcc) {
        if (null == contactItem) contactItem = new ContactItem();
        String sto = "";
        String scc = "";
        String sbcc = "";

        int i = 1;
        if (to != null && to.length > 0) {
            for (Rfc822Token s : to) {
                String address = s.getAddress();
                if (address != null && !address.isEmpty()) {
                    ContactItem contactItem = new ContactItem();
                    contactItem.setDataId(address);
                    contactItem.setTypeId(address);
                    contactItem.setIntellibitzId(address);
                    contactItem.setName(address);
                    contactItem.setType("to");
                    this.contactItem.addContact(contactItem);
                    sto += address;
                    if (++i < to.length) {
                        sto += ",";
                    }
                }
            }
        }
        i = 1;
        if (cc != null && cc.length > 0) {
            for (Rfc822Token s : cc) {
                String address = s.getAddress();
                if (address != null && !address.isEmpty()) {
                    ContactItem contactItem = new ContactItem();
                    contactItem.setDataId(address);
                    contactItem.setTypeId(address);
                    contactItem.setIntellibitzId(address);
                    contactItem.setName(address);
                    contactItem.setType("cc");
                    this.contactItem.addContact(contactItem);
                    scc += address;
                    if (++i < cc.length) {
                        scc += ",";
                    }
                }
            }
        }
        i = 1;
        if (bcc != null && bcc.length > 0) {
            for (Rfc822Token s : bcc) {
                String address = s.getAddress();
                if (address != null && !address.isEmpty()) {
                    ContactItem contactItem = new ContactItem();
                    contactItem.setDataId(address);
                    contactItem.setTypeId(address);
                    contactItem.setIntellibitzId(address);
                    contactItem.setName(address);
                    contactItem.setType("bcc");
                    this.contactItem.addContact(contactItem);
                    sbcc += address;
                    if (++i < bcc.length) {
                        sbcc += ",";
                    }
                }
            }
        }
        setTo(sto);
        setCc(scc);
        setBcc(sbcc);
        return compose();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(msgAttachID);
        dest.writeString(partID);
        dest.writeString(subType);
        dest.writeString(encoding);
        dest.writeString(language);
        dest.writeString(md5);
        dest.writeString(downloadURL);
        dest.writeString(description);
        dest.writeInt(size);
        dest.writeByte((byte) (readyToSend ? 1 : 0));     //if == true, byte == 1
        dest.writeString(fromEmail);
        dest.writeString(fromName);
        dest.writeString(messageDirection);
        dest.writeString(messageType);
        dest.writeString(text);
        dest.writeString(fullText);
        dest.writeString(html);
        dest.writeString(messageAttachId);
        dest.writeString(toType);
        dest.writeString(docOwnerEmail);
        dest.writeString(fromUid);
        dest.writeString(toUid);
        dest.writeString(chatId);
        dest.writeString(toChatUid);
        dest.writeString(docSenderEmail);
        dest.writeString(docSender);
        dest.writeInt(pendingDocs);
        dest.writeInt(hasAttachments);
        dest.writeInt(read);
        dest.writeInt(delivered);
        dest.writeInt(locked);
        dest.writeByte((byte) (broadcast ? 1 : 0));     //if true, byte == 1
        dest.writeByte((byte) (noText ? 1 : 0));     //if true, byte == 1
        dest.writeByte((byte) (flagged ? 1 : 0));     //if true, byte == 1
        dest.writeString(subject);
        dest.writeString(msgRef);
        dest.writeString(chatMsgRef);
        dest.writeString(from);
        dest.writeString(to);
        dest.writeString(cc);
        dest.writeString(bcc);
        dest.writeString(this.flags);
        dest.writeParcelable(contactItem, flags);
/*
        emails2.clear();
        emails2.addAll(emails);
        dest.writeTypedList(emails2);
        attachments2.clear();
        attachments2.addAll(attachments);
        dest.writeTypedList(attachments2);
*/
/*
        dest.writeInt(emails.size());
        for (EmailItem item : emails) {
            dest.writeParcelable(item, flags);
        }
*/
        dest.writeInt(attachments.size());
        for (MessageItem item : attachments) {
            dest.writeParcelable(item, flags);
        }
        dest.writeByte((byte) (typing ? 1 : 0));     //if typing == true, byte == 1
        dest.writeString(typingText);
        dest.writeString(latestMessageText);
        dest.writeLong(latestMessageTimestamp);
        dest.writeInt(unreadCount);
        dest.writeInt(defaultFolder);
        dest.writeString(folderCode);
        dest.writeParcelable(photoFileUri, flags);
        dest.writeParcelable(videoFileUri, flags);
        dest.writeParcelable(audioFileUri, flags);
        dest.writeString(sharedText);
        dest.writeParcelable(sharedUri, flags);
        dest.writeParcelable(sharedDeviceContactItem, flags);
        dest.writeTypedList(sharedUris);
        dest.writeByte((byte) (null == messageItemStack || messageItemStack.isEmpty() ? 1 : 0));     //if typing == true, byte == 1
        if (messageItemStack != null && messageItemStack.isEmpty())
            dest.writeParcelable(peekMessageInStack(), flags);
        dest.writeInt(messages.size());
        for (MessageItem item : messages) {
            dest.writeParcelable(item, flags);
        }
        dest.writeInt(messageItemConcurrentLinkedQueue.size());
        for (MessageItem item : messageItemConcurrentLinkedQueue) {
            dest.writeParcelable(item, flags);
        }

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getMsgAttachID() {
        return msgAttachID;
    }

    public void setMsgAttachID(String msgAttachID) {
        this.msgAttachID = msgAttachID;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getPartID() {
        return partID;
    }

    public void setPartID(String partID) {
        this.partID = partID;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static class MessageItemComparator<T extends MessageItem> extends
            BaseItemComparator<T> {

        public MessageItemComparator() {
            super();
        }

        public MessageItemComparator(SORT_MODE sortMode) {
            super(sortMode);
        }

        @Override
        public int compare(T lhs, T rhs) {
            return super.compare(lhs, rhs);
        }
    }

}
