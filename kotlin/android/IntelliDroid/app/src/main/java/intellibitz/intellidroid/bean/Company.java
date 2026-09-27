package intellibitz.intellidroid.bean;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.io.Serializable;

public class Company implements
        Comparable<Company>, Serializable, Parcelable {

    public static final Creator<Company> CREATOR = new Creator<Company>() {
        @Override
        public Company createFromParcel(Parcel in) {
            return new Company(in);
        }

        @Override
        public Company[] newArray(int size) {
            return new Company[size];
        }
    };
    private String typeCode;
    private String type;

    public Company() {
        super();
    }

    public Company(String type, String typeCode) {
        super();
        this.type = type;
        this.typeCode = typeCode;
    }

    protected Company(Parcel in) {
        type = in.readString();
        typeCode = in.readString();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    @Override
    public int hashCode() {
        return typeCode.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Company company = (Company) o;

        return typeCode.equals(company.typeCode);

    }

    @Override
    public String toString() {
//        return type + " (+" + type + ")";
        return type;
    }

    @Override
    public int compareTo(@NonNull Company another) {
        return this.type.compareTo(another.type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(typeCode);
    }
}