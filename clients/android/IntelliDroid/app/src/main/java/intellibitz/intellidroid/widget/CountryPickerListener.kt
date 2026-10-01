package intellibitz.intellidroid.widget

import intellibitz.intellidroid.bean.Country

fun interface CountryPickerListener {
    fun onSelectCountry(country: Country)
}
