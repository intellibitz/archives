package cc.thevar.blukit.ui.viewmodels

import cc.thevar.blukit.ui.navigation.Route
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationViewModelTest {

    @Test
    fun `initial state is Nearby`() {
        val viewModel = NavigationViewModel()
        assertEquals(Route.Nearby, viewModel.currentRoute.value)
        assertEquals(1, viewModel.backStack.size)
        assertEquals(Route.Nearby, viewModel.backStack.first())
    }

    @Test
    fun `navigate to Timeline updates state`() {
        val viewModel = NavigationViewModel()
        viewModel.navigate(Route.Timeline)
        assertEquals(Route.Timeline, viewModel.currentRoute.value)
        assertEquals(2, viewModel.backStack.size)
        assertEquals(Route.Timeline, viewModel.backStack.last())
    }

    @Test
    fun `popBackStack updates state`() {
        val viewModel = NavigationViewModel()
        viewModel.navigate(Route.Timeline)
        viewModel.popBackStack()
        assertEquals(Route.Nearby, viewModel.currentRoute.value)
        assertEquals(1, viewModel.backStack.size)
    }

    @Test
    fun `navigateToCrumb resets stack`() {
        val viewModel = NavigationViewModel()
        viewModel.navigate(Route.Timeline)
        viewModel.navigate(Route.LiveFeed)
        assertEquals(3, viewModel.backStack.size)
        
        viewModel.navigateToCrumb(0) // Back to Nearby
        assertEquals(1, viewModel.backStack.size)
        assertEquals(Route.Nearby, viewModel.currentRoute.value)
    }
}
