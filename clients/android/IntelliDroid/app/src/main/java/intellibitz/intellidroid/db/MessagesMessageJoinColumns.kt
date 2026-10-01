package intellibitz.intellidroid.db

interface MessagesMessageJoinColumns : IntellibitzItemColumns {
    companion object {
        const val KEY_MESSAGE_ID = "msg_id"
        const val KEY_MSG_THREAD_ID = "msg_thread_id"
    }
}
