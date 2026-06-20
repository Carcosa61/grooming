package com.petgrooming.manager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.petgrooming.manager.ui.feature.bookings.BookingsScreen
import com.petgrooming.manager.ui.feature.calendar.CalendarScreen
import com.petgrooming.manager.ui.feature.dashboard.DashboardScreen
import com.petgrooming.manager.ui.feature.pets.PetsScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val BOOKINGS = "bookings"
    const val BOOKING_DETAIL = "booking/{bookingId}"
    const val BOOKING_CREATE = "booking/create"
    const val PETS = "pets"
    const val PET_DETAIL = "pet/{petId}"
    const val PET_CREATE = "pet/create"
    const val CALENDAR = "calendar"
    const val SETTINGS = "settings"

    fun bookingDetail(bookingId: Long) = "booking/$bookingId"
    fun petDetail(petId: Long) = "pet/$petId"
}

@Composable
fun PetGroomingNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD,
        modifier = modifier
    ) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigateToBookings = { navController.navigate(Routes.BOOKINGS) },
                onNavigateToPets = { navController.navigate(Routes.PETS) }
            )
        }

        composable(Routes.BOOKINGS) {
            BookingsScreen(
                onNavigateToBookingDetail = { bookingId ->
                    navController.navigate(Routes.bookingDetail(bookingId))
                },
                onNavigateToCreateBooking = { navController.navigate(Routes.BOOKING_CREATE) }
            )
        }

        composable(
            route = Routes.BOOKING_DETAIL,
            arguments = listOf(navArgument("bookingId") { type = NavType.LongType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getLong("bookingId") ?: return@composable
            // TODO: BookingDetailScreen(bookingId, navController)
        }

        composable(Routes.BOOKING_CREATE) {
            // TODO: BookingFormScreen(navController)
        }

        composable(Routes.PETS) {
            PetsScreen(
                onNavigateToPetDetail = { petId ->
                    navController.navigate(Routes.petDetail(petId))
                },
                onNavigateToCreatePet = { navController.navigate(Routes.PET_CREATE) }
            )
        }

        composable(
            route = Routes.PET_DETAIL,
            arguments = listOf(navArgument("petId") { type = NavType.LongType })
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getLong("petId") ?: return@composable
            // TODO: PetDetailScreen(petId, navController)
        }

        composable(Routes.PET_CREATE) {
            // TODO: PetFormScreen(navController)
        }

        composable(Routes.CALENDAR) {
            CalendarScreen(
                onNavigateToDate = { date ->
                    // Navigate to bookings for selected date
                    navController.navigate(Routes.BOOKINGS)
                }
            )
        }

        composable(Routes.SETTINGS) {
            // TODO: SettingsScreen(navController)
        }
    }
}
