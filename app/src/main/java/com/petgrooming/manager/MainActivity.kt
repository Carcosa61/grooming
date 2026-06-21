package com.petgrooming.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.petgrooming.manager.ui.navigation.PetGroomingNavHost
import com.petgrooming.manager.ui.navigation.Routes
import com.petgrooming.manager.ui.theme.PetGroomingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetGroomingTheme {
                MainScreen()
            }
        }
    }
}

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val labelResId: Int
) {
    data object Dashboard : BottomNavItem(
        route = Routes.DASHBOARD,
        icon = Icons.Default.Dashboard,
        labelResId = R.string.nav_dashboard
    )
    data object Bookings : BottomNavItem(
        route = Routes.BOOKINGS,
        icon = Icons.Default.EventNote,
        labelResId = R.string.nav_bookings
    )
    data object Pets : BottomNavItem(
        route = Routes.PETS,
        icon = Icons.Default.Pets,
        labelResId = R.string.nav_pets
    )
    data object Calendar : BottomNavItem(
        route = Routes.CALENDAR,
        icon = Icons.Default.CalendarMonth,
        labelResId = R.string.nav_calendar
    )
    data object Settings : BottomNavItem(
        route = Routes.SETTINGS,
        icon = Icons.Default.Settings,
        labelResId = R.string.nav_settings
    )
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navItems = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Bookings,
        BottomNavItem.Pets,
        BottomNavItem.Calendar,
        BottomNavItem.Settings
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            NavigationBar {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route?.substringBefore("?") == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { 
                            Text(
                                text = stringResource(item.labelResId),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            ) 
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        PetGroomingNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
