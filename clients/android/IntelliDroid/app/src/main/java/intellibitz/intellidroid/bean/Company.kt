package intellibitz.intellidroid.bean

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class Company : Comparable<Company>, Serializable, Parcelable {

    var typeCode: String? = null
    var type: String? = null

    constructor() : super()

    constructor(type: String?, typeCode: String?) : super() {
        this.type = type
        this.typeCode = typeCode
    }

    protected constructor(`in`: Parcel) {
        type = `in`.readString()
        typeCode = `in`.readString()
    }

    override fun hashCode(): Int {
        return typeCode?.hashCode() ?: 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val company = other as Company
        return typeCode == company.typeCode
    }

    override fun toString(): String {
        return type ?: ""
    }

    override fun compareTo(other: Company): Int {
        val thisType = type ?: ""
        val otherType = other.type ?: ""
        return thisType.compareTo(otherType)
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(type)
        dest.writeString(typeCode)
    }

    companion object CREATOR : Parcelable.Creator<Company> {
        override fun createFromParcel(`in`: Parcel): Company {
            return Company(`in`)
        }

        override fun newArray(size: Int): Array<Company?> {
            return arrayOfNulls(size)
        }
    }
}
