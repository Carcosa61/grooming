package com.petgrooming.manager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.petgrooming.manager.ui.feature.bookings.BookingFormScreen
import com.petgrooming.manager.ui.feature.bookings.BookingsScreen
import com.petgrooming.manager.ui.feature.calendar.CalendarScreen
import com.petgrooming.manager.ui.feature.dashboard.DashboardScreen
import com.petgrooming.manager.ui.feature.owners.OwnerFormScreen
import com.petgrooming.manager.ui.feature.pets.PetFormScreen
import com.petgrooming.manager.ui.feature.pets.PetsScreen
import com.petgrooming.manager.ui.feature.settings.SettingsScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val BOOKINGS = "bookings"
    const val BOOKING_DETAIL = "booking/{bookingId}"
    const val BOOKING_CREATE = "booking/create"
    const val BOOKING_EDIT = "booking/{bookingId}/edit"
    const val PETS = "pets"
    const val PET_DETAIL = "pet/{petId}"
    const val PET_CREATE = "pet/create"
    const val PET_EDIT = "pet/{petId}/edit"
    const val OWNER_CREATE = "owner/create"
    const val OWNER_EDIT = "owner/{ownerId}/edit"
    const val CALENDAR = "calendar"
    const val SETTINGS = "settings"

    fun bookingDetail(bookingId: Long) = "booking/$bookingId"
    fun bookingEdit(bookingId: Long) = "booking/$bookingId/edit"
    fun petDetail(petId: Long) = "pet/$petId"
    fun petEdit(petId: Long) = "pet/$petId/edit"
    fun ownerEdit(ownerId: Long) = "owner/$ownerId/edit"
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
                onNavigateToPets = { navController.navigate(Routes.PETS) },
                onNavigateToBookingEdit = { bookingId ->
                    navController.navigate(Routes.bookingEdit(bookingId))
                },
                onNavigateToCreateBooking = { navController.navigate(Routes.BOOKING_CREATE) }
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
            // TODO: BookingDetailScreen - for now redirect to edit
            BookingFormScreen(
                onNavigateBack = { navController.popBackStack() },
                onBookingSaved = { navController.popBackStack() },
                onAddPet = { navController.navigate(Routes.PET_CREATE) }
            )
        }

        composable(Routes.BOOKING_CREATE) {
            BookingFormScreen(
                onNavigateBack = { navController.popBackStack() },
                onBookingSaved = { navController.popBackStack() },
                onAddPet = { navController.navigate(Routes.PET_CREATE) }
            )
        }

        composable(
            route = Routes.BOOKING_EDIT,
            arguments = listOf(navArgument("bookingId") { type = NavType.LongType })
        ) {
            BookingFormScreen(
                onNavigateBack = { navController.popBackStack() },
                onBookingSaved = { navController.popBackStack() },
                onAddPet = { navController.navigate(Routes.PET_CREATE) }
            )
        }

        composable(Routes.PETS) {
            PetsScreen(
                onNavigateToPetDetail = { petId ->
                    navController.navigate(Routes.petEdit(petId))
                },
                onNavigateToCreatePet = { navController.navigate(Routes.PET_CREATE) }
            )
        }

        composable(
            route = Routes.PET_DETAIL,
            arguments = listOf(navArgument("petId") { type = NavType.LongType })
        ) {
            PetFormScreen(
                onNavigateBack = { navController.popBackStack() },
                onPetSaved = { navController.popBackStack() },
                onAddOwner = { navController.navigate(Routes.OWNER_CREATE) }
            )
        }

        composable(Routes.PET_CREATE) {
            PetFormScreen(
                onNavigateBack = { navController.popBackStack() },
                onPetSaved = { navController.popBackStack() },
                onAddOwner = { navController.navigate(Routes.OWNER_CREATE) }
            )
        }

        composable(
            route = Routes.PET_EDIT,
            arguments = listOf(navArgument("petId") { type = NavType.LongType })
        ) {
            PetFormScreen(
                onNavigateBack = { navController.popBackStack() },
                onPetSaved = { navController.popBackStack() },
                onAddOwner = { navController.navigate(Routes.OWNER_CREATE) }
            )
        }

        composable(Routes.OWNER_CREATE) {
            OwnerFormScreen(
                onNavigateBack = { navController.popBackStack() },
                onOwnerSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.OWNER_EDIT,
            arguments = listOf(navArgument("ownerId") { type = NavType.LongType })
        ) {
            OwnerFormScreen(
                onNavigateBack = { navController.popBackStack() },
                onOwnerSaved = { navController.popBackStack() }
            )
        }

        composable(Routes.CALENDAR) {
            CalendarScreen(
                onNavigateToDate = { date ->
                    navController.navigate(Routes.BOOKINGS)
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
