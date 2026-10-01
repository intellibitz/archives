package intellibitz.intellidroid.db

interface MessagesContactsJoinColumns : IntellibitzItemColumns {
    companion object {
        const val KEY_MSGTHREAD_ID = "msgthread_id"
        const val KEY_CONTACTTHREAD_ID = "contactthread_id"
    }
}
