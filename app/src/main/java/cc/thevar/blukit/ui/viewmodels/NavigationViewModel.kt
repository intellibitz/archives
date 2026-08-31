package cc.thevar.blukit.ui.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import cc.thevar.blukit.domain.model.Sphere
import cc.thevar.blukit.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class NavigationViewModel : ViewModel() {
    private val _backStack = mutableStateListOf<Route>(Route.SphereField(Sphere.ID_GLOBAL))
    val backStack: List<Route> = _backStack

    private val _currentRoute = MutableStateFlow<Route>(Route.SphereField(Sphere.ID_GLOBAL))
    val currentRoute: StateFlow<Route> = _currentRoute.asStateFlow()

    fun navigate(route: Route, resetStack: Boolean = false) {
        if (resetStack) {
            _backStack.clear()
        }
        if (_backStack.lastOrNull() != route) {
            _backStack.add(route)
            _currentRoute.value = route
        }
    }

    fun popBackStack() {
        if (_backStack.size > 1) {
            _backStack.removeLast()
            _currentRoute.value = _backStack.last()
        }
    }

    fun navigateToCrumb(index: Int) {
        if (index < _backStack.size) {
            while (_backStack.size > index + 1) {
                _backStack.removeLast()
            }
            _currentRoute.value = _backStack.last()
        }
    }

    fun getBreadcrumbTrail(sessionGroups: List<Sphere>, focusedSourceId: String?, scannedDevices: List<cc.thevar.blukit.domain.model.Source>): List<String> {
        val trail = mutableListOf<String>()
        _backStack.forEach { route ->
            when (route) {
                is Route.Sensing -> trail.add("NEARBY")
                is Route.SphereField -> {
                    val sphere = sessionGroups.find { it.id == route.roomId }
                    if (sphere != null) {
                        sphere.parentId?.let { pid ->
                            val parent = sessionGroups.find { it.id == pid }
                            val parentName = parent?.name ?: "HOME"
                            if (trail.lastOrNull() != parentName) trail.add(parentName)
                        }
                        trail.add(sphere.name)
                    } else {
                        trail.add("GROUP")
                    }
                }
                else -> trail.add("CHAT")
            }
        }
        
        if (focusedSourceId != null && _currentRoute.value is Route.SphereField) {
            val device = scannedDevices.find { (it.persistentId == focusedSourceId) || (it.id == focusedSourceId) }
            trail.add(device?.name ?: "Source")
        }
        return trail.distinct()
    }
}
