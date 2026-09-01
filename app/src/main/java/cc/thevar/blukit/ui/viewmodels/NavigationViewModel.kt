package cc.thevar.blukit.ui.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavKey
import cc.thevar.blukit.domain.model.Group
import cc.thevar.blukit.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NavigationViewModel : ViewModel() {
    val backStack = mutableStateListOf<NavKey>(Route.Nearby)

    private val _currentRoute = MutableStateFlow<Route>(Route.Nearby)
    val currentRoute: StateFlow<Route> = _currentRoute.asStateFlow()

    private fun updateCurrentRoute() {
        (backStack.lastOrNull() as? Route)?.let {
            _currentRoute.value = it
        }
    }

    fun navigate(route: Route, resetStack: Boolean = false) {
        if (resetStack) {
            backStack.clear()
        }
        backStack.add(route)
        _currentRoute.value = route
    }

    fun popBackStack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
            updateCurrentRoute()
        }
    }

    fun navigateToCrumb(index: Int) {
        if (index < backStack.size) {
            while (backStack.size > index + 1) {
                backStack.removeAt(backStack.size - 1)
            }
            updateCurrentRoute()
        }
    }

    fun getBreadcrumbTrail(sessionGroups: List<Group>, focusedSourceId: String?, scannedDevices: List<cc.thevar.blukit.domain.model.Source>): List<String> {
        val trail = mutableListOf<String>()
        backStack.filterIsInstance<Route>().forEach { route ->
            when (route) {
                is Route.Nearby -> trail.add("NEARBY")
                is Route.GroupField -> {
                    val group = sessionGroups.find { it.id == route.roomId }
                    if (group != null) {
                        group.parentId?.let { pid ->
                            val parent = sessionGroups.find { it.id == pid }
                            val parentName = parent?.name ?: "HOME"
                            if (trail.lastOrNull() != parentName) trail.add(parentName)
                        }
                        trail.add(group.name)
                    } else {
                        trail.add("GROUP")
                    }
                }
                else -> trail.add("CHAT")
            }
        }
        
        if (focusedSourceId != null && _currentRoute.value is Route.GroupField) {
            val device = scannedDevices.find { (it.persistentId == focusedSourceId) || (it.id == focusedSourceId) }
            trail.add(device?.name ?: "Source")
        }
        return trail.distinct()
    }
}
