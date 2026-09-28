package intellibitz.intellidroid.listener

interface ClutterMessageListener : ClutterListener {
    fun onEmailMessageTyping(text: String?)
    fun onEmailMessageTypingStopped(text: String?)
}
