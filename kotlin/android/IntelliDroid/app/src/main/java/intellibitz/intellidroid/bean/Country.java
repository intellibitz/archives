package intellibitz.intellidroid.bean;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.io.Serializable;
import java.util.Locale;

public class Country implements Comparable<Country>, Serializable, Parcelable {

    public static final Creator<Country> CREATOR = new Creator<Country>() {
        @Override
        public Country createFromParcel(Parcel in) {
            return new Country(in);
        }

        @Override
        public Country[] newArray(int size) {
            return new Country[size];
        }
    };
    private String name;
    private String isoCode;
    private String dialCode;

    public Country() {
        super();
    }

    public Country(String isoCode) {
        setIsoCode(isoCode.toUpperCase());
        setName(new Locale("", getIsoCode()).getDisplayCountry());
        setDialCode(String.valueOf(PhoneNumberUtil.getInstance().getCountryCodeForRegion(getIsoCode())));
    }

    protected Country(Parcel in) {
        name = in.readString();
        isoCode = in.readString();
        dialCode = in.readString();
    }

    public String getDialCode() {
        return dialCode;
    }

    public void setDialCode(String dialCode) {
        this.dialCode = dialCode;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
//        return name + " (+" + dialCode + ")";
        return name;
    }

    @Override
    public int compareTo(@NonNull Country another) {
        return this.name.compareTo(another.name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(isoCode);
        dest.writeString(dialCode);
    }
}