package intellibitz.intellidroid.db

interface MessageAttachmentJoinColumns : IntellibitzItemColumns {
    companion object {
        const val KEY_ATTACHMENT_ID = "attachment_id"
        const val KEY_MESSAGE_ID = "msg_id"
    }
}
