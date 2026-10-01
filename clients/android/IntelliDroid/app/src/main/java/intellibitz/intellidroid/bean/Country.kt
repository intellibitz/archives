package intellibitz.intellidroid.bean

import android.os.Parcel
import android.os.Parcelable
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.io.Serializable
import java.util.Locale

class Country : Comparable<Country>, Serializable, Parcelable {

    var name: String? = null
    var isoCode: String? = null
    var dialCode: String? = null

    constructor() : super()

    constructor(isoCode: String) {
        val upperIso = isoCode.uppercase()
        this.isoCode = upperIso
        this.name = Locale("", upperIso).displayCountry
        this.dialCode = PhoneNumberUtil.getInstance().getCountryCodeForRegion(upperIso).toString()
    }

    protected constructor(`in`: Parcel) {
        name = `in`.readString()
        isoCode = `in`.readString()
        dialCode = `in`.readString()
    }

    override fun toString(): String {
        return name ?: ""
    }

    override fun compareTo(other: Country): Int {
        val thisName = name ?: ""
        val otherName = other.name ?: ""
        return thisName.compareTo(otherName)
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(isoCode)
        dest.writeString(dialCode)
    }

    companion object CREATOR : Parcelable.Creator<Country> {
        override fun createFromParcel(`in`: Parcel): Country {
            return Country(`in`)
        }

        override fun newArray(size: Int): Array<Country?> {
            return arrayOfNulls(size)
        }
    }
}
