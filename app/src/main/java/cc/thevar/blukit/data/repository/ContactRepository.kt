package cc.thevar.blukit.data.repository

import cc.thevar.blukit.data.local.MessageStore
import cc.thevar.blukit.data.local.entities.ContactEntity
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val messageStore: MessageStore) {
    val allContacts: Flow<List<ContactEntity>> = messageStore.getAllContacts()

    fun deleteContact(contact: ContactEntity) {
        messageStore.deleteContact(contact.id)
    }

    fun clearAll() {
        messageStore.deleteAllContacts()
    }
}
