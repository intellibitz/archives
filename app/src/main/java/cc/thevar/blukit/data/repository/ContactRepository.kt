package cc.thevar.blukit.data.repository

import cc.thevar.blukit.data.local.PulseStore
import cc.thevar.blukit.data.local.entities.ContactEntity
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val pulseStore: PulseStore) {
    val allContacts: Flow<List<ContactEntity>> = pulseStore.getAllContacts()

    suspend fun getContact(id: String): ContactEntity? = pulseStore.getContact(id)

    suspend fun saveContact(contact: ContactEntity) {
        pulseStore.insertContact(contact)
    }

    suspend fun deleteContact(contact: ContactEntity) {
        pulseStore.deleteContact(contact.id)
    }

    suspend fun clearAll() {
        pulseStore.deleteAllContacts()
    }
}
