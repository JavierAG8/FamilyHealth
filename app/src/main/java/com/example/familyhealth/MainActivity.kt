package com.example.familyhealth

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.familyhealth.ui.theme.auth.LoginScreen
import com.example.familyhealth.ui.theme.auth.RegisterScreen
import com.example.familyhealth.ui.theme.dashboard.DashboardScreen
import com.example.familyhealth.ui.theme.medication.MedicationListScreen
import com.example.familyhealth.ui.theme.navigation.NavRoutes
import com.example.familyhealth.viewmodel.AuthViewModel
import com.example.familyhealth.ui.theme.appointments.AppointmentsListScreen
import com.example.familyhealth.ui.theme.symptom.SymptomScreen
import com.example.familyhealth.ui.theme.profile.ProfileScreen
import com.example.familyhealth.ui.theme.family.FamilyScreen
import com.example.familyhealth.ui.theme.FamilyHealthTheme
import androidx.compose.ui.platform.LocalContext
import com.example.familyhealth.viewmodel.AuthViewModelFactory
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.familyhealth.ui.theme.splash.SplashScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()
        createNotificationChannel()

        setContent {
            FamilyHealthTheme {
                val navController = rememberNavController()

                val context = LocalContext.current
                val authVm: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(context.applicationContext)
                )

                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.Splash.route
                ) {

                    composable(NavRoutes.Splash.route) {
                        SplashScreen(
                            onFinished = {
                                navController.navigate(NavRoutes.Login.route) {
                                    popUpTo(NavRoutes.Splash.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(NavRoutes.Login.route) {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate(NavRoutes.Dashboard.route) {
                                    popUpTo(NavRoutes.Login.route) { inclusive = true }
                                }
                            },
                            onGoToRegister = {
                                navController.navigate(NavRoutes.Register.route)
                            },
                            vm = authVm
                        )
                    }

                    composable("healthTips") {
                        com.example.familyhealth.ui.theme.healthtips.HealthTipsScreen(navController)
                    }

                    composable(NavRoutes.Register.route) {
                        RegisterScreen(
                            onRegisterSuccess = {
                                navController.navigate(NavRoutes.Dashboard.route) {
                                    popUpTo(NavRoutes.Login.route) { inclusive = true }
                                }
                            },
                            vm = authVm
                        )
                    }

                    composable(NavRoutes.Dashboard.route) {
                        DashboardScreen(
                            onOpenMedication = {
                                navController.navigate(NavRoutes.MedicationList.route)
                            },
                            onOpenAppointments = {
                                navController.navigate(NavRoutes.Appointments.route)
                            },
                            onOpenSymptoms = {
                                navController.navigate(NavRoutes.Symptoms.route)
                            },
                            onOpenProfile = {
                                navController.navigate(NavRoutes.Profile.route)
                            },
                            onOpenFamily = {
                                navController.navigate(NavRoutes.Family.route)
                            },
                            onOpenRecommendations = {
                                navController.navigate("healthTips")
                            },
                            onLogout = {
                                authVm.logout()
                                navController.navigate(NavRoutes.Login.route) {
                                    popUpTo(NavRoutes.Dashboard.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(NavRoutes.MedicationList.route) {
                        MedicationListScreen()
                    }

                    composable(NavRoutes.Appointments.route) {
                        AppointmentsListScreen()
                    }

                    composable(NavRoutes.Symptoms.route) {
                        SymptomScreen()
                    }

                    composable(NavRoutes.Profile.route) {
                        ProfileScreen()
                    }

                    composable(NavRoutes.Family.route) {
                        FamilyScreen()
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    2001
                )
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "familyhealth_channel"
            val name = "Recordatorios FamilyHealth"
            val descriptionText = "Recordatorios de medicación y citas"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)

            notificationManager.createNotificationChannel(channel)
        }
    }
}
