package org.jdc.template.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

interface ViewModelNavBar<T : Enum<T>> {
    val navigatorFlow: StateFlow<ViewModelNavBarNavigator?>
    val selectedNavBarFlow: StateFlow<T?>

    fun navigate(route: String, popBackStack: Boolean = false)
    fun navigate(routes: List<String>)
    fun navigateWithOptions(route: String, navOptions: NavOptions)
    fun navigateWithOptions(route: String, optionsBuilder: NavOptionsBuilder.() -> Unit = {})
    fun onNavBarItemSelected(selectedItem: T, route: String? = null)
    fun navBarNavigation(route: String, reselected: Boolean)
    fun resetNavigate(viewModelNavBarNavigator: ViewModelNavBarNavigator)
}

class ViewModelNavBarImpl<T : Enum<T>>(
    startNavBarItem: T?,
    private val navBarConfig: NavBarConfig<T>? = null,
) : ViewModelNavBar<T> {
    private val _navigatorFlow = MutableStateFlow<ViewModelNavBarNavigator?>(null)
    override val navigatorFlow: StateFlow<ViewModelNavBarNavigator?> = _navigatorFlow.asStateFlow()

    private val _selectedNavBarFlow = MutableStateFlow<T?>(startNavBarItem)
    override val selectedNavBarFlow: StateFlow<T?> = _selectedNavBarFlow.asStateFlow()

    override fun navigate(route: String, popBackStack: Boolean) {
        _navigatorFlow.compareAndSet(null, if (popBackStack) ViewModelNavBarNavigator.PopAndNavigate(route) else ViewModelNavBarNavigator.Navigate(route))
    }

    override fun navigate(routes: List<String>) {
        _navigatorFlow.compareAndSet(null, ViewModelNavBarNavigator.NavigateMultiple(routes))
    }

    override fun navigateWithOptions(route: String, navOptions: NavOptions) {
        _navigatorFlow.compareAndSet(null, ViewModelNavBarNavigator.NavigateWithOptions(route, navOptions))
    }

    override fun navigateWithOptions(route: String, optionsBuilder: NavOptionsBuilder.() -> Unit) {
        _navigatorFlow.compareAndSet(null, ViewModelNavBarNavigator.NavigateWithOptions(route, navOptions(optionsBuilder)))
    }

    override fun navBarNavigation(route: String, reselected: Boolean) {
        _navigatorFlow.compareAndSet(null, ViewModelNavBarNavigator.NavBarNavigate(route, reselected))
    }

    override fun resetNavigate(viewModelNavBarNavigator: ViewModelNavBarNavigator) {
        _navigatorFlow.compareAndSet(viewModelNavBarNavigator, null)
    }

    override fun onNavBarItemSelected(selectedItem: T, route: String?) {
        val navRoute = route ?: navBarConfig?.getRouteByNavItem(selectedItem)

        if (navRoute != null) {
            val reselected = _selectedNavBarFlow.value == selectedItem
            navBarNavigation(navRoute, reselected)
            _selectedNavBarFlow.value = selectedItem
        } else {
            Timber.e("route not found for selectedItem [$selectedItem].  Make sure either the selectedItem is defined in the NavBarConfig OR the 'route' is supplied to this function")
        }
    }
}

sealed class ViewModelNavBarNavigator {
    abstract fun <T : Enum<T>> navigate(navController: NavController, viewModelNav: ViewModelNavBar<T>): Boolean

    class NavBarNavigate(private val route: String, private val reselected: Boolean) : ViewModelNavBarNavigator() {
        override fun <T : Enum<T>> navigate(navController: NavController, viewModelNav: ViewModelNavBar<T>): Boolean {
            if (reselected) {
                // clear back stack
                navController.popBackStack(route, inclusive = false)
            }

            navController.navigate(route) {
                // Avoid multiple copies of the same destination when reselecting the same item
                launchSingleTop = true
                // Restore state when reselecting a previously selected item
                restoreState = true
                // Pop up backstack to the first destination and save state. This makes going back
                // to the start destination when pressing back in any other bottom tab.
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
            }

            viewModelNav.resetNavigate(this)
            return false
        }
    }

    class Navigate(private val route: String) : ViewModelNavBarNavigator() {
        override fun <T : Enum<T>> navigate(navController: NavController, viewModelNav: ViewModelNavBar<T>): Boolean {
            navController.navigate(route)

            viewModelNav.resetNavigate(this)
            return false
        }
    }

    class NavigateMultiple(private val routes: List<String>): ViewModelNavBarNavigator() {
        override fun <T : Enum<T>> navigate(navController: NavController, viewModelNav: ViewModelNavBar<T>): Boolean {
            routes.forEach { route ->
                navController.navigate(route)
            }

            viewModelNav.resetNavigate(this)
            return false
        }
    }

    class NavigateWithOptions(private val route: String, private val navOptions: NavOptions) : ViewModelNavBarNavigator() {
        override fun <T : Enum<T>> navigate(navController: NavController, viewModelNav: ViewModelNavBar<T>): Boolean {
            navController.navigate(route, navOptions)

            viewModelNav.resetNavigate(this)
            return false
        }
    }

    class PopAndNavigate(private val route: String) : ViewModelNavBarNavigator() {
        override fun <T : Enum<T>> navigate(navController: NavController, viewModelNav: ViewModelNavBar<T>): Boolean {
            val stackPopped = navController.popBackStack()
            navController.navigate(route)

            viewModelNav.resetNavigate(this)
            return stackPopped
        }
    }
}

interface NavBarConfig<T : Enum<T>> {
    fun getRouteByNavItem(navBarItem: T): String?
}

class DefaultNavBarConfig<T : Enum<T>>(
    private val navBarItemRouteMap: Map<T, String>
): NavBarConfig<T> {
    override fun getRouteByNavItem(navBarItem: T): String? = navBarItemRouteMap[navBarItem]
}

@Composable
fun <T : Enum<T>>HandleNavBarNavigation(
    viewModelNavBar: ViewModelNavBar<T>,
    navController: NavController?,
    navigatorFlow: StateFlow<ViewModelNavBarNavigator?>
) {
    val navigator by navigatorFlow.collectAsState()

    if (navController != null) {
        navigator?.navigate(navController, viewModelNavBar)
    }
}
