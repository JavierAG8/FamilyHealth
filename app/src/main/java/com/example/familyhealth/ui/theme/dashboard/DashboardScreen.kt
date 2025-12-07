package com.example.familyhealth.ui.theme.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenMedication: () -> Unit,
    onOpenAppointments: () -> Unit,
    onOpenSymptoms: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenFamily: () -> Unit,
    onOpenRecommendations: () -> Unit,
    onLogout: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "FamilyHealth",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.primary,
                    titleContentColor = colorScheme.onPrimary,
                    navigationIconContentColor = colorScheme.onPrimary,
                    actionIconContentColor = colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Mi espacio de salud",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Accede a tu ficha, tratamientos, citas y síntomas en un solo lugar.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onBackground.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                ),
                onClick = onOpenProfile
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Perfil",
                        modifier = Modifier.size(40.dp),
                        tint = colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mi ficha de salud",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Datos personales, patologías y hábitos.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    TextButton(
                        onClick = onOpenProfile,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = colorScheme.onPrimary
                        )
                    ) {
                        Text("Ver ficha")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Gestión diaria",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.primary
                )
            )
            Spacer(Modifier.height(12.dp))

            DashboardCard(
                title = "Medicaciones",
                description = "Gestiona tus tratamientos y pastillas.",
                icon = Icons.Filled.MedicalServices,
                containerColor = colorScheme.primaryContainer,
                iconTint = colorScheme.primary,
                onClick = onOpenMedication
            )

            Spacer(Modifier.height(12.dp))

            DashboardCard(
                title = "Citas médicas",
                description = "Consulta, añade y organiza tus próximas visitas.",
                icon = Icons.Filled.CalendarToday,
                containerColor = colorScheme.primaryContainer,
                iconTint = colorScheme.primary,
                onClick = onOpenAppointments
            )

            Spacer(Modifier.height(12.dp))

            DashboardCard(
                title = "Síntomas",
                description = "Registra cómo te sientes día a día.",
                icon = Icons.Filled.Favorite,
                containerColor = colorScheme.primaryContainer,
                iconTint = colorScheme.primary,
                onClick = onOpenSymptoms
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Red de apoyo",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.primary
                )
            )
            Spacer(Modifier.height(12.dp))

            DashboardCard(
                title = "Familia",
                description = "Gestiona quién puede ver tu información de salud.",
                icon = Icons.Filled.FamilyRestroom,
                containerColor = colorScheme.primaryContainer,
                iconTint = colorScheme.primary,
                onClick = onOpenFamily
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Recomendaciones",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.primary
                )
            )
            Spacer(Modifier.height(12.dp))

            DashboardCard(
                title = "Recomendaciones para el paciente",
                description = "Consejos personalizados y generales sobre hábitos de vida saludables.",
                icon = Icons.Filled.Favorite,
                containerColor = colorScheme.primaryContainer,
                iconTint = colorScheme.primary,
                onClick = onOpenRecommendations
            )

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Cerrar sesión"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar sesión")
                }
            }
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = iconTint
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
