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
    // Single authoritative backstack
    val backStack = mutableStateListOf<NavKey>()

    private val _currentRoute = MutableStateFlow<Route?>(null)
    val currentRoute: StateFlow<Route?> = _currentRoute.asStateFlow()

    fun initBackStack(initialRoute: Route) {
        if (backStack.isEmpty()) {
            backStack.add(initialRoute)
            _currentRoute.value = initialRoute
        }
    }

    private fun updateCurrentRoute() {
        _currentRoute.value = backStack.lastOrNull() as? Route
    }

    fun navigate(route: Route, resetStack: Boolean = false) {
        if (resetStack) {
            backStack.clear()
        }
        backStack.add(route)
        updateCurrentRoute()
    }

    fun popBackStack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
            updateCurrentRoute()
        }
    }

    fun navigateToCrumb(index: Int) {
        if (index >= 0 && index < backStack.size) {
            while (backStack.size > index + 1) {
                backStack.removeAt(backStack.size - 1)
            }
            updateCurrentRoute()
        }
    }

    fun getBreadcrumbTrail(sessionGroups: List<Group>, scannedDevices: List<cc.thevar.blukit.domain.model.Source>): List<String> {
        val trail = mutableListOf<String>()
        backStack.filterIsInstance<Route>().forEach { route ->
            when (route) {
                is Route.Onboarding -> trail.add("IDENTITY")
                is Route.Nearby -> trail.add("NEARBY")
                is Route.Timeline -> trail.add("HISTORY")
                is Route.LiveFeed -> trail.add("LIVE")
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
                is Route.MessageField -> {
                    trail.add("CHAT")
                }
            }
        }
        return trail.distinct()
    }
}
