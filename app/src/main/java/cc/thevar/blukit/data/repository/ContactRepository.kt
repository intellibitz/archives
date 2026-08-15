package cc.thevar.blukit.data.repository

import cc.thevar.blukit.data.local.VibeStore
import cc.thevar.blukit.data.local.entities.ContactEntity
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val vibeStore: VibeStore) {
    val allContacts: Flow<List<ContactEntity>> = vibeStore.getAllContacts()

    suspend fun getContact(id: String): ContactEntity? = vibeStore.getContact(id)

    suspend fun saveContact(contact: ContactEntity) {
        vibeStore.insertContact(contact)
    }

    suspend fun deleteContact(contact: ContactEntity) {
        vibeStore.deleteContact(contact.id)
    }

    suspend fun clearAll() {
        vibeStore.deleteAllContacts()
    }
}
