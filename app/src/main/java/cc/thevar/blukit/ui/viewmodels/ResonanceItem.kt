package cc.thevar.blukit.ui.viewmodels

import cc.thevar.blukit.domain.model.Echo
import cc.thevar.blukit.domain.model.Source

data class ResonanceItem(
    val source: Source,
    val latestEcho: Echo? = null
)
