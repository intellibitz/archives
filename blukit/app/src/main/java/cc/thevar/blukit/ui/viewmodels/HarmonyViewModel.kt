package cc.thevar.blukit.ui.viewmodels

import androidx.lifecycle.ViewModel
import cc.thevar.blukit.data.power.HarmonyManager
import cc.thevar.blukit.domain.power.HarmonyReport
import kotlinx.coroutines.flow.StateFlow

class HarmonyViewModel(
    private val harmonyManager: HarmonyManager,
) : ViewModel() {
    val report: StateFlow<HarmonyReport> = harmonyManager.report

    fun updateLocation(location: android.location.Location) {
        harmonyManager.updateLocation(location)
    }
}
