package intellibitz.intellidroid.listener

interface PeopleDetailListener : PeopleListener {
    fun onPeopleTyping(text: String?)
    fun onPeopleTypingStopped(text: String?)
}
