package cc.thevar.blukit.data.repository

import cc.thevar.blukit.data.local.EchoLedger
import cc.thevar.blukit.data.local.entities.ContactEntity
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val messageStore: EchoLedger) {
    val allContacts: Flow<List<ContactEntity>> = messageStore.getAllContacts()

    fun deleteContact(contact: ContactEntity) {
        messageStore.deleteContact(contact.id)
    }

    fun clearAll() {
        messageStore.deleteAllContacts()
    }
}
