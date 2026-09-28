package intellibitz.intellidroid.company

import intellibitz.intellidroid.bean.Company

interface CompanyPickerListener {
    fun onSelectCompany(company: Company?)
}
