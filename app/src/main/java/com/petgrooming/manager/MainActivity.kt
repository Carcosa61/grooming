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
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
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
        route = "dashboard",
        icon = Icons.Default.Dashboard,
        labelResId = R.string.nav_dashboard
    )
    data object Bookings : BottomNavItem(
        route = "bookings",
        icon = Icons.Default.EventNote,
        labelResId = R.string.nav_bookings
    )
    data object Pets : BottomNavItem(
        route = "pets",
        icon = Icons.Default.Pets,
        labelResId = R.string.nav_pets
    )
    data object Calendar : BottomNavItem(
        route = "calendar",
        icon = Icons.Default.CalendarMonth,
        labelResId = R.string.nav_calendar
    )
}

@Composable
fun MainScreen() {
    val navItems = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Bookings,
        BottomNavItem.Pets,
        BottomNavItem.Calendar
    )

    var selectedIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(stringResource(item.labelResId)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Content will be replaced with NavHost when navigation is set up
        when (navItems[selectedIndex]) {
            BottomNavItem.Dashboard -> PlaceholderScreen(
                title = stringResource(R.string.nav_dashboard),
                modifier = Modifier.padding(innerPadding)
            )
            BottomNavItem.Bookings -> PlaceholderScreen(
                title = stringResource(R.string.nav_bookings),
                modifier = Modifier.padding(innerPadding)
            )
            BottomNavItem.Pets -> PlaceholderScreen(
                title = stringResource(R.string.nav_pets),
                modifier = Modifier.padding(innerPadding)
            )
            BottomNavItem.Calendar -> PlaceholderScreen(
                title = stringResource(R.string.nav_calendar),
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )
    }
}
