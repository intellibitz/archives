package cc.thevar.blukit.data.repository

import cc.thevar.blukit.data.local.dao.ContactDao
import cc.thevar.blukit.data.local.entities.ContactEntity
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao) {
    val allContacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()

    suspend fun getContact(id: String): ContactEntity? = contactDao.getContactById(id)

    suspend fun saveContact(contact: ContactEntity) {
        contactDao.insertContact(contact)
    }

    suspend fun deleteContact(contact: ContactEntity) {
        contactDao.deleteContact(contact)
    }

    suspend fun clearAll() {
        contactDao.deleteAllContacts()
    }
}
