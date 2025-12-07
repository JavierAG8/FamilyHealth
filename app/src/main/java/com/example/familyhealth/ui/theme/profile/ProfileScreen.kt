package com.example.familyhealth.ui.theme.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.familyhealth.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    vm: ProfileViewModel = viewModel()
) {
    val ui = vm.ui.collectAsState().value
    val scroll = rememberScrollState()

    var isEditing by remember { mutableStateOf(false) }

    // Campos editables (se inicializan desde ui.profile)
    var name by remember(ui.profile) { mutableStateOf(ui.profile.name) }
    var age by remember(ui.profile) { mutableStateOf(ui.profile.age?.toString() ?: "") }
    var gender by remember(ui.profile) { mutableStateOf(ui.profile.gender) }
    var weight by remember(ui.profile) { mutableStateOf(ui.profile.weightKg?.toString() ?: "") }
    var height by remember(ui.profile) { mutableStateOf(ui.profile.heightCm?.toString() ?: "") }
    var bloodType by remember(ui.profile) { mutableStateOf(ui.profile.bloodType) }
    var conditions by remember(ui.profile) { mutableStateOf(ui.profile.conditions) }
    var allergies by remember(ui.profile) { mutableStateOf(ui.profile.allergies) }
    var notes by remember(ui.profile) { mutableStateOf(ui.profile.notes) }
    var emergencyName by remember(ui.profile) { mutableStateOf(ui.profile.emergencyContactName) }
    var emergencyPhone by remember(ui.profile) { mutableStateOf(ui.profile.emergencyContactPhone) }
    var referenceCenter by remember(ui.profile) { mutableStateOf(ui.profile.referenceCenter) }

    val bmiText by remember(weight, height) {
        mutableStateOf(
            runCatching {
                val w = weight.replace(",", ".").toFloat()
                val hCm = height.replace(",", ".").toFloat()
                if (w > 0 && hCm > 0) {
                    val hM = hCm / 100f
                    val bmi = w / (hM * hM)
                    "IMC: %.1f".format(bmi)
                } else null
            }.getOrNull()
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }

    ui.message?.let { msg ->
        LaunchedEffect(msg) {
            snackbarHostState.showSnackbar(msg)
            isEditing = false
            vm.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Perfil de salud") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(scroll)
            ) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = name.ifBlank { "Paciente sin nombre" },
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = buildString {
                                if (age.isNotBlank()) append("Edad: $age años   ")
                                if (gender.isNotBlank()) append("•  $gender")
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (weight.isNotBlank() || height.isNotBlank() || bloodType.isNotBlank()) {
                            Text(
                                text = buildString {
                                    append("Peso: ${weight.ifBlank { "?" }} kg   •   ")
                                    append("Altura: ${height.ifBlank { "?" }} cm")
                                    if (bloodType.isNotBlank()) {
                                        append("   •   Grupo sanguíneo: $bloodType")
                                    }
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        bmiText?.let { bmi ->
                            Text(
                                text = bmi,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        if (ui.profile.emergencyContactName.isNotBlank() ||
                            ui.profile.emergencyContactPhone.isNotBlank()
                        ) {
                            Text(
                                text = "Contacto de emergencia: " +
                                        listOf(
                                            ui.profile.emergencyContactName.takeIf { it.isNotBlank() },
                                            ui.profile.emergencyContactPhone.takeIf { it.isNotBlank() }
                                        ).filterNotNull().joinToString(" - "),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (ui.profile.referenceCenter.isNotBlank()) {
                            Text(
                                text = "Centro de salud: ${ui.profile.referenceCenter}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Esta ficha resume tus datos médicos básicos.\n" +
                                    "Puedes mostrarla en consultas o urgencias.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                if (!isEditing) {

                    Text(
                        text = "Resumen clínico",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoRow("Patologías / diagnósticos", ui.profile.conditions)
                            InfoRow("Alergias", ui.profile.allergies)
                            InfoRow("Notas / hábitos", ui.profile.notes)
                            InfoRow("Contacto de emergencia", buildString {
                                if (ui.profile.emergencyContactName.isNotBlank())
                                    append(ui.profile.emergencyContactName)
                                if (ui.profile.emergencyContactPhone.isNotBlank()) {
                                    if (isNotEmpty()) append(" - ")
                                    append(ui.profile.emergencyContactPhone)
                                }
                            })
                            InfoRow("Centro de salud de referencia", ui.profile.referenceCenter)
                        }
                    }

                    Spacer(Modifier.height(80.dp))
                } else {

                    Text(
                        text = "Editar datos clínicos",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Nombre y apellidos") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = age,
                                onValueChange = { age = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Edad (años)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = gender,
                                onValueChange = { gender = it },
                                label = { Text("Sexo / Género") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = weight,
                                    onValueChange = { weight = it.replace(",", ".") },
                                    label = { Text("Peso (kg)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = height,
                                    onValueChange = { height = it.replace(",", ".") },
                                    label = { Text("Altura (cm)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = bloodType,
                                onValueChange = { bloodType = it.uppercase() },
                                label = { Text("Grupo sanguíneo (ej: A+, 0-, B+)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = conditions,
                                onValueChange = { conditions = it },
                                label = { Text("Patologías / diagnósticos principales") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 80.dp),
                                maxLines = 4
                            )

                            OutlinedTextField(
                                value = allergies,
                                onValueChange = { allergies = it },
                                label = { Text("Alergias (medicamentos, alimentos...)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 80.dp),
                                maxLines = 4
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Notas relevantes (hábitos, cirugías, embarazo…)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp),
                                maxLines = 6
                            )

                            OutlinedTextField(
                                value = emergencyName,
                                onValueChange = { emergencyName = it },
                                label = { Text("Contacto de emergencia (nombre)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = emergencyPhone,
                                onValueChange = { emergencyPhone = it.filter { ch -> ch.isDigit() || ch == '+' || ch == ' ' } },
                                label = { Text("Teléfono de emergencia") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = referenceCenter,
                                onValueChange = { referenceCenter = it },
                                label = { Text("Centro de salud de referencia") },
                                singleLine = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(Modifier.height(80.dp))
                }
            }

            if (!ui.loading) {
                if (!isEditing) {
                    FilledTonalButton(
                        onClick = { isEditing = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp)
                    ) {
                        Text("Editar ficha")
                    }
                } else {
                    FilledTonalButton(
                        onClick = {
                            vm.saveProfile(
                                name = name,
                                age = age,
                                gender = gender,
                                weight = weight,
                                height = height,
                                bloodType = bloodType,
                                conditions = conditions,
                                allergies = allergies,
                                notes = notes,
                                emergencyContactName = emergencyName,
                                emergencyContactPhone = emergencyPhone,
                                referenceCenter = referenceCenter
                            )
                        },
                        enabled = !ui.saving,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp)
                    ) {
                        if (ui.saving) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Guardando…")
                        } else {
                            Text("Guardar cambios")
                        }
                    }
                }
            }

            if (ui.loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text("Cargando perfil clínico…")
                    }
                }
            }
            ui.error?.let { err ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    if (value.isBlank()) return
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
