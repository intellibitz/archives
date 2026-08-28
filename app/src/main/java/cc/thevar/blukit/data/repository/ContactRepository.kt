package cc.thevar.blukit.data.repository

import cc.thevar.blukit.data.local.PulseStore
import cc.thevar.blukit.data.local.entities.ContactEntity
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val pulseStore: PulseStore) {
    val allContacts: Flow<List<ContactEntity>> = pulseStore.getAllContacts()

    fun deleteContact(contact: ContactEntity) {
        pulseStore.deleteContact(contact.id)
    }

    fun clearAll() {
        pulseStore.deleteAllContacts()
    }
}
