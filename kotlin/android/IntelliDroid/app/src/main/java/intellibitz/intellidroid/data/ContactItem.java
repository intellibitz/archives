package intellibitz.intellidroid.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import intellibitz.intellidroid.bean.BaseItemComparator;
import intellibitz.intellidroid.bean.Company;
import intellibitz.intellidroid.bean.Country;
import intellibitz.intellidroid.util.MainApplicationSingleton;
import intellibitz.intellidroid.bean.BaseItemComparator;
import intellibitz.intellidroid.bean.Company;
import intellibitz.intellidroid.bean.Country;
import intellibitz.intellidroid.util.MainApplicationSingleton;

import intellibitz.intellidroid.bean.BaseItemComparator;
import intellibitz.intellidroid.bean.Company;
import intellibitz.intellidroid.bean.Country;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 */
public class ContactItem extends
        BaseItem implements
        Parcelable {
    public static final String TAG = "ContactItem";
    public static final String USER_CONTACT = "UserItem";
    public static final String EMAIL_CONTACT = "EmailItem";
    public static final String COMPANY_CONTACT = "CompanyItem";
    public static final String DEVICE_CONTACT = "DeviceContactItem";
    public static final String WORK_CONTACT = "WorkContactItem";
    public static final String INTELLIBITZ_CONTACT = "IntellibitzContactItem";
    public static final Creator<ContactItem> CREATOR = new Creator<ContactItem>() {
        @Override
        public ContactItem createFromParcel(Parcel in) {
            return new ContactItem(in);
        }

        @Override
        public ContactItem[] newArray(int size) {
            return new ContactItem[size];
        }
    };
    //    requires explicit true for new group logic.. safe to be false by default
    protected boolean includeTypeEquals = false;
    protected boolean newGroup = false;
    protected boolean pinned;
    protected int version;
    protected int isIntellibitzContact = 0;
    protected int isWorkContact = 0;
    protected String selectedId;
    protected transient JSONArray mobiles = new JSONArray();
    protected transient JSONArray emails = new JSONArray();
    protected HashSet<ContactItem> intellibitzContacts = new HashSet<>();
    protected HashSet<ContactItem> contactItems = new HashSet<>();
    //    the information(tags or milestones) for this group
    protected HashSet<BaseItem> infoItems = new HashSet<>();
    //    display only
    protected HashSet<ContactItem> selectedContacts = new HashSet<>();
    //    GCM
    private String gcmToken;
    private int gcmTokenSentToCloud = 0;
    private String token;
    private String device;
    private String otp;
    private String mobile;
    private String signupEmail;
    private Country country;
    private Company company;
    private String companyCode;
    private String countryCode;
    private String countryName;
    private String pwd;
    private HashSet<String> devices = new HashSet<>();
    private HashSet<String> companies = new HashSet<>();
    //    display only
    private int accountExists = 0;
    private String emailURL;

    public ContactItem() {
        super();
    }

    public ContactItem(String id) {
        this(id, id, id);
    }

    public ContactItem(String id, String name, String email) {
        this(id, email, name, email);
    }

    public ContactItem(String id, String email, String name, String type) {
        super(id, name, type);
        setIntellibitzId(id);
        setEmail(email);
        setTypeId(email);
    }

    public ContactItem(ContactItem intellibitzContactItem) {
        setDataId(intellibitzContactItem.getIntellibitzId());
        setIntellibitzId(intellibitzContactItem.getIntellibitzId());
        setTypeId(intellibitzContactItem.getTypeId());
        setName(intellibitzContactItem.getName());
        setProfilePic(intellibitzContactItem.getProfilePic());
        setDevice(intellibitzContactItem.isDevice());
        setCloud(intellibitzContactItem.isCloud());
        setAnonymous(intellibitzContactItem.isAnonymous());
        setGroup(intellibitzContactItem.isGroup());
        setEmailItem(intellibitzContactItem.isEmailItem());
    }

    //    Parcelable implementation
    protected ContactItem(Parcel in) {
        super(in);
        token = in.readString();
        device = in.readString();
        otp = in.readString();
        mobile = in.readString();
        signupEmail = in.readString();
        pwd = in.readString();
        companyCode = in.readString();
        companyName = in.readString();
        countryCode = in.readString();
        countryName = in.readString();
        emailURL = in.readString();
        accountExists = in.readInt();
        gcmToken = in.readString();
        gcmTokenSentToCloud = in.readInt();
        company = in.readParcelable(getClass().getClassLoader());
        country = in.readParcelable(getClass().getClassLoader());
        version = in.readInt();
        selectedId = in.readString();
        newGroup = in.readByte() != 0;     //newGroup == true if byte != 0
        pinned = in.readByte() != 0;     //pinned == true if byte != 0
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            ContactItem item = in.readParcelable(getClass().getClassLoader());
            intellibitzContacts.add(item);
        }
        size = in.readInt();
        for (int i = 0; i < size; i++) {
            ContactItem item = in.readParcelable(getClass().getClassLoader());
            contactItems.add(item);
        }
        size = in.readInt();
        for (int i = 0; i < size; i++) {
            BaseItem item = in.readParcelable(getClass().getClassLoader());
            infoItems.add(item);
        }
        size = in.readInt();
        for (int i = 0; i < size; i++) {
            ContactItem item = in.readParcelable(getClass().getClassLoader());
            selectedContacts.add(item);
        }
        try {
            emails = new JSONArray(in.readString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            mobiles = new JSONArray(in.readString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getGcmToken() {
        return gcmToken;
    }

    public void setGcmToken(String gcmToken) {
        this.gcmToken = gcmToken;
    }

    public boolean isGcmTokenSentToCloud() {
        return gcmTokenSentToCloud != 0;
    }

    /*
    public ArrayList<DeviceContactItem> getContacts2() {
        if (null == contacts2) {
            contacts2 = new ArrayList<>(0);
        }
        if (contacts != null) {
            contacts2.clear();
            contacts2.addAll(contacts);
        }
        return contacts2;
    }

    public void setContacts2(ArrayList<DeviceContactItem> contacts2) {
        this.contacts2 = contacts2;
    }

    public void setEmails2(ArrayList<ContactItem> emails2) {
        this.emails2 = emails2;
    }
*/

    public void setGcmTokenSentToCloud(boolean gcmTokenSentToCloud) {
        this.gcmTokenSentToCloud = gcmTokenSentToCloud ? 1 : 0;
    }

    public void setDevices(HashSet<String> devices) {
        this.devices = devices;
    }

    public void setCompanies(HashSet<String> companies) {
        this.companies = companies;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getSignupEmail() {
        return signupEmail;
    }

    public void setSignupEmail(String signupEmail) {
        this.signupEmail = signupEmail;
    }

    public Set<String> getDevices() {
        return devices;
    }

    public void setDevices(Set<String> devices) {
        this.devices.clear();
        this.devices.addAll(devices);
    }

    public Set<String> getCompanies() {
        return companies;
    }

    public void setCompanies(Set<String> companies) {
        this.companies.clear();
        this.companies.addAll(companies);
    }

    public String getEmailURL() {
        return emailURL;
    }

/*
    public ArrayList<ContactItem> getEmails2() {
        emails2.clear();
        emails2.addAll(emails);
        return emails2;
    }
*/

    public void setEmailURL(String emailURL) {
        this.emailURL = emailURL;
    }

    public String getEmail() {
        if (null == email) {
            Iterator<ContactItem> iterator = contactItems.iterator();
            if (iterator.hasNext()) {
                ContactItem next = iterator.next();
                if (next != null)
                    return next.getEmail();
            }
        }
        return email;
    }

    public int getAccountExists() {
        return accountExists;
    }

    public void setAccountExists(int accountExists) {
        this.accountExists = accountExists;
    }

    public boolean removeEmail(long id) {
        ContactItem[] contactItems = this.contactItems.toArray(new ContactItem[0]);
        for (ContactItem contactItem : contactItems) {
            if (contactItem.get_id() == id) {
                this.contactItems.remove(contactItem);
            }
        }
        return false;
    }

    public boolean removeEmail(String email) {
        ContactItem[] contactItems = this.contactItems.toArray(new ContactItem[0]);
        for (ContactItem contactItem : contactItems) {
            if (contactItem.getEmail().equals(email)) {
                this.contactItems.remove(contactItem);
            }
        }
        return false;
    }

    public long getEmailId(String item) {
        ContactItem email = getEmail(item);
        if (email != null) {
            return email.get_id();
        }
        return 0;
    }

    public void addEmail(String email, String code) {
        ContactItem userEmailItem = new ContactItem(email, email, email);
        userEmailItem.setEmailCode(code);
        this.contactItems.add(userEmailItem);
    }

    public void addEmail(ContactItem item) {
        this.contactItems.add(item);
    }

    public ContactItem getEmail(String item) {
        for (ContactItem email : contactItems) {
            if (email.getEmail().equals(item)) {
                return email;
            }
        }
        return null;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getIsIntellibitzContact() {
        return isIntellibitzContact;
    }

    public void setIsIntellibitzContact(int isIntellibitzContact) {
        this.isIntellibitzContact = isIntellibitzContact;
    }

    public int getIsWorkContact() {
        return isWorkContact;
    }

    public void setIsWorkContact(int isWorkContact) {
        this.isWorkContact = isWorkContact;
    }

    public JSONArray getMobiles() {
        return mobiles;
    }

    public void setMobiles(JSONArray mobiles) {
        this.mobiles = mobiles;
    }

    public void setMobiles(String mobiles) {
        if (null == mobiles || 0 == mobiles.length()) {
            this.mobiles = new JSONArray();
            return;
        }
        try {
            this.mobiles = new JSONArray(mobiles);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setMobiles(Collection<String> mobiles) {
        this.mobiles = MainApplicationSingleton.createsJSONArrayFromCollection(mobiles);
    }

    public JSONArray getEmails() {
        return emails;
    }

    public void setEmails(JSONArray emails) {
        this.emails = emails;
    }

    public void setEmails(String emails) {
        if (null == emails || 0 == emails.length()) {
            this.emails = new JSONArray();
            return;
        }
        try {
            this.emails = new JSONArray(emails);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

/*
    public ArrayList<MobileItem> getMobiles2() {
        return mobiles2;
    }

    public void setMobiles2(ArrayList<MobileItem> mobiles2) {
        this.mobiles2 = mobiles2;
    }

    public ArrayList<EmailItem> getEmails2() {
        return emails2;
    }

    public void setEmails2(ArrayList<EmailItem> emails2) {
        this.emails2 = emails2;
    }

    public ArrayList<IntellibitzContactItem> getContacts2() {
        return contacts2;
    }

    public void setContacts2(ArrayList<IntellibitzContactItem> contacts2) {
        this.contacts2 = contacts2;
    }
*/

    public void setEmails(Collection<String> emails) {
        this.emails = MainApplicationSingleton.createsJSONArrayFromCollection(emails);
    }

    public HashSet<ContactItem> getIntellibitzContacts() {
        return intellibitzContacts;
    }

    public void setIntellibitzContacts(HashSet<ContactItem> intellibitzContacts) {
        this.intellibitzContacts = intellibitzContacts;
    }

    public String getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(String selectedId) {
        this.selectedId = selectedId;
    }

    public ContactItem addContact(ContactItem contactItem) {
        this.contactItems.add(contactItem);
        return contactItem;
    }

    public boolean isNewGroup() {
        return newGroup;
    }

    public void setNewGroup(boolean newGroup) {
        this.newGroup = newGroup;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public HashSet<BaseItem> getInfoItems() {
        return infoItems;
    }

    public BaseItem getLatestInfo() {
        if (null == infoItems || infoItems.isEmpty()) return null;
        return infoItems.iterator().next();
    }

    public boolean isEmpty() {
        return null == contactItems || contactItems.isEmpty();
    }

    public HashSet<ContactItem> getContactItems() {
        return contactItems;
    }

    public void setContactItems(Collection<ContactItem> contactItems) {
        if (null == contactItems) {
            this.contactItems.clear();
            return;
        }
        this.contactItems.addAll(contactItems);
    }

    public ContactItem getContactItem(String id) {
        if (null == contactItems || contactItems.isEmpty())
            return null;
        for (ContactItem item : contactItems) {
            if (item.getDataId().equals(id))
                return item;
        }
        return null;
    }

    public String[] getContactsAsArray() {
        String[] ids = new String[contactItems.size()];
        int i = 0;
        for (ContactItem item : contactItems) {
            String s = item.getIntellibitzId();
            ids[i++] = null == s ? "" : s;
        }
        return ids;
    }

    public JSONArray getContactsAsJsonObjectArray() {
        JSONArray jsonArray = new JSONArray();
        if (null == contactItems || contactItems.isEmpty()) return jsonArray;
        for (ContactItem item : contactItems) {
            String uid = item.getIntellibitzId();
            if (!TextUtils.isEmpty(uid)) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("uid", uid);
                    jsonObject.put("type", "user");
                    jsonArray.put(jsonObject);
                } catch (JSONException e) {
//                    e.printStackTrace();
                    Log.e(TAG, TAG + e.getMessage());
                }
            }
        }
        return jsonArray;
    }

    public String[] getContactsNameAsArray() {
        String[] ids = new String[contactItems.size()];
        int i = 0;
        for (ContactItem item : contactItems) {
            String s = item.getName();
            ids[i++] = null == s ? "" : s;
        }
        return ids;
    }

    public HashSet<ContactItem> getSelectedContacts() {
        return selectedContacts;
    }

    public void setSelectedContacts(HashSet<ContactItem> selectedContacts) {
        this.selectedContacts.clear();
        if (null == selectedContacts) return;
        this.selectedContacts.addAll(selectedContacts);
    }

    public void mergeSelectedContacts() {
//            merges selected contactItems into group contactItems and clears selected contactItems
        for (ContactItem selected : selectedContacts) {
            if (!contactItems.contains(selected)) {
                contactItems.add(selected);
            }
        }
        selectedContacts.clear();
    }

    public void setIncludeTypeEquals(boolean includeTypeEquals) {
        this.includeTypeEquals = includeTypeEquals;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Object copy = null;
        try {
//            copy super members
            copy = super.clone();
//            deep copies collections
            ContactItem contactItemClone = (ContactItem) copy;
            Set<ContactItem> items = contactItemClone.getContactItems();
            if (items != null) {
                Set<ContactItem> ecopy1 = new HashSet<>(items.size());
                for (ContactItem item : items) {
                    ecopy1.add((ContactItem) item.clone());
                }
                contactItemClone.setContactItems(ecopy1);
            }
        } catch (CloneNotSupportedException ignored) {
            Log.e(TAG, ignored.getMessage());
        }
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContactItem that = (ContactItem) o;
// the db id.. will not available for display presentation objects
//        if (_id != baseItem._id) return false;
        if (!TextUtils.isEmpty(intellibitzId)) {
            if (includeTypeEquals) {
                return (type + intellibitzId).equals(that.type + that.intellibitzId);
            }
            return intellibitzId.equals(that.intellibitzId);
        } else if (deviceContactId > 0) {
            return deviceContactId == that.deviceContactId;
        } else if (!TextUtils.isEmpty(dataId)) {
            if (includeTypeEquals) {
                return (type + dataId).equals(that.type + that.dataId);
            }
            return dataId.equals(that.dataId);
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
// the db id.. will not available for display presentation objects
//        int result = (int) (_id ^ (_id >>> 32));
        if (!TextUtils.isEmpty(intellibitzId)) {
            if (includeTypeEquals) {
                return (type + intellibitzId).hashCode();
            }
            return intellibitzId.hashCode();
        } else if (deviceContactId > 0) {
            return (int) (deviceContactId ^ (deviceContactId >>> 32));
        } else if (!TextUtils.isEmpty(dataId)) {
            if (includeTypeEquals) {
                return (type + dataId).hashCode();
            }
            return 31 * dataId.hashCode();
        }
        return super.hashCode();
    }

    @Override
    public String toString() {
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
                ", newGroup=" + newGroup +
                '}' + super.toString();
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.defaultWriteObject();
        out.writeObject(getMobiles().toString());
        out.writeObject(getEmails().toString());
/*
        JSONArray mobiles = this.getMobiles();
        if (null == mobiles) {
            out.writeInt(0);
        } else {
            out.writeInt(mobiles.length());
            out.writeObject(mobiles.toString());
        }
        JSONArray emails = this.getEmails();
        if (null == emails) {
            out.writeInt(0);
        } else {
            out.writeInt(emails.length());
            out.writeObject(emails.toString());
        }
*/
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        setMobiles((String) in.readObject());
        setEmails((String) in.readObject());
/*
        int sz = in.readInt();
        if (sz > 0){
            setMobiles((String) in.readObject());
        }
        sz = in.readInt();
        if (sz > 0){
            setEmails((String) in.readObject());
        }
*/
    }

    private void readObjectNoData()
            throws ObjectStreamException {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(token);
        dest.writeString(device);
        dest.writeString(otp);
        dest.writeString(mobile);
        dest.writeString(signupEmail);
        dest.writeString(pwd);
        dest.writeString(companyCode);
        dest.writeString(companyName);
        dest.writeString(countryCode);
        dest.writeString(countryName);
        dest.writeString(emailURL);
        dest.writeInt(accountExists);
        dest.writeString(gcmToken);
        dest.writeInt(gcmTokenSentToCloud);
        dest.writeParcelable(company, 0);
        dest.writeParcelable(country, 0);
        dest.writeInt(version);
        dest.writeString(selectedId);
        dest.writeByte((byte) (newGroup ? 1 : 0));     //if newGroup == true, byte == 1
        dest.writeByte((byte) (pinned ? 1 : 0));     //if pinned == true, byte == 1
        dest.writeInt(intellibitzContacts.size());
        for (ContactItem item : intellibitzContacts) {
            dest.writeParcelable(item, flags);
        }
        dest.writeInt(contactItems.size());
        for (ContactItem item : contactItems) {
            dest.writeParcelable(item, flags);
        }
        dest.writeInt(infoItems.size());
        for (BaseItem item : infoItems) {
            dest.writeParcelable(item, flags);
        }
        dest.writeInt(selectedContacts.size());
        for (ContactItem item : selectedContacts) {
            dest.writeParcelable(item, flags);
        }
        dest.writeString(emails.toString());
        dest.writeString(mobiles.toString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static class ContactItemComparator<T extends ContactItem> extends
            BaseItemComparator<T> {

        public ContactItemComparator() {
            super();
        }

        public ContactItemComparator(SORT_MODE sortMode) {
            super(sortMode);
        }

        @Override
        public int compare(T lhs, T rhs) {
            return super.compare(lhs, rhs);
        }
    }

}
