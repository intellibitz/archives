package cc.thevar.blukit.ui.viewmodels

import cc.thevar.blukit.domain.model.Message
import cc.thevar.blukit.domain.model.Source

data class ConnectionItem(
    val source: Source,
    val latestMessage: Message? = null
)
