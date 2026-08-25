package cc.thevar.blukit.ui.viewmodels

import androidx.lifecycle.ViewModel
import cc.thevar.blukit.data.power.SupremePowerManager
import cc.thevar.blukit.domain.power.SupremePowerReport
import kotlinx.coroutines.flow.StateFlow

class SupremePowerViewModel(
    private val supremePowerManager: SupremePowerManager,
) : ViewModel() {
    val report: StateFlow<SupremePowerReport> = supremePowerManager.report

    fun updateLocation(location: android.location.Location) {
        supremePowerManager.updateLocation(location)
    }
}
