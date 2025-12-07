package com.example.familyhealth.ui.theme.navigation

sealed class NavRoutes(val route: String) {
    object Splash : NavRoutes("splash")
    data object Login : NavRoutes("login")
    data object Register : NavRoutes("register")
    data object Dashboard : NavRoutes("dashboard")
    data object MedicationList : NavRoutes("medication_list")
    data object Appointments : NavRoutes("appointments")
    data object Symptoms : NavRoutes("symptoms")
    object Profile : NavRoutes("profile")
    object Family : NavRoutes("family")
}
