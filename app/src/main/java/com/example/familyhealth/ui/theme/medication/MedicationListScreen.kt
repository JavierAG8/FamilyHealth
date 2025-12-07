package com.example.familyhealth.ui.theme.medication

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.familyhealth.data.local.MedicationEntity
import com.example.familyhealth.data.local.SymptomEntity
import com.example.familyhealth.utils.MedicationPdfExporter
import com.example.familyhealth.utils.AllDataPdfExporter
import com.example.familyhealth.viewmodel.MedicationViewModel
import com.example.familyhealth.viewmodel.ProfileViewModel
import com.example.familyhealth.viewmodel.SymptomViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen() {
    val vm: MedicationViewModel = viewModel()
    val meds = vm.medications.collectAsStateWithLifecycle().value

    val profileVm: ProfileViewModel = viewModel()
    val profileUi = profileVm.ui.collectAsStateWithLifecycle().value

    val symptomVm: SymptomViewModel = viewModel()
    val symptoms: List<SymptomEntity> = symptomVm.symptoms.collectAsStateWithLifecycle().value

    var showDialog by remember { mutableStateOf(false) }
    var medToEdit by remember { mutableStateOf<MedicationEntity?>(null) }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme

    val medsPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            val ok = MedicationPdfExporter.export(context, uri, meds)
            scope.launch {
                snackbarHostState.showSnackbar(
                    if (ok) "PDF de medicación exportado correctamente."
                    else "Error al exportar PDF de medicación."
                )
            }
        }
    }

    val fullPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            val ok = AllDataPdfExporter.export(
                context = context,
                uri = uri,
                profile = profileUi.profile,
                medications = meds,
                symptoms = symptoms
            )
            scope.launch {
                snackbarHostState.showSnackbar(
                    if (ok) "Informe clínico completo exportado correctamente."
                    else "Error al exportar informe completo."
                )
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mis Medicaciones",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.primary,
                    titleContentColor = colorScheme.onPrimary,
                    navigationIconContentColor = colorScheme.onPrimary,
                    actionIconContentColor = colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    medToEdit = null   // modo NUEVO
                    showDialog = true
                },
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            ) {
                Text("+")
            }
        }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Encabezado / resumen
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.MedicalServices,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Tratamientos activos",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "Gestiona tu medicación diaria.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (meds.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        AssistChip(
                            onClick = { vm.syncFromCloud() },
                            label = { Text("Sync") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Sync,
                                    contentDescription = "Sincronizar"
                                )
                            }
                        )
                        AssistChip(
                            onClick = {
                                val now = System.currentTimeMillis()
                                val suggestedName =
                                    "medicacion_${DateFormat.format("yyyyMMdd_HHmm", now)}.pdf"
                                medsPdfLauncher.launch(suggestedName)
                            },
                            label = { Text("Medicación PDF") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.PictureAsPdf,
                                    contentDescription = "Exportar PDF medicación"
                                )
                            }
                        )
                        AssistChip(
                            onClick = {
                                val now = System.currentTimeMillis()
                                val suggestedName =
                                    "informe_completo_${DateFormat.format("yyyyMMdd_HHmm", now)}.pdf"
                                fullPdfLauncher.launch(suggestedName)
                            },
                            label = { Text("Informe completo") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.PictureAsPdf,
                                    contentDescription = "Exportar informe completo"
                                )
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (meds.isEmpty()) {
                // Estado vacío
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer
                    )
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Aún no tienes medicaciones guardadas.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "Pulsa el botón + para añadir tu primer tratamiento, " +
                                    "o sincroniza con la nube si ya tenías datos.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = { vm.syncFromCloud() }) {
                            Icon(
                                imageVector = Icons.Filled.Sync,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Sincronizar desde la nube")
                        }
                    }
                }
            } else {
                // Lista de medicaciones
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(meds) { m ->
                        val hasReminder = vm.hasAnyReminderConfigured(m)
                        MedicationItem(
                            med = m,
                            hasReminder = hasReminder,
                            onToggle = { vm.toggleTaken(m) },
                            onEdit = {
                                medToEdit = m
                                showDialog = true
                            },
                            onDelete = { vm.deleteMedication(m) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddOrEditMedicationDialog(
            initial = medToEdit,
            onDismiss = {
                showDialog = false
                medToEdit = null
            },
            onConfirm = { name, dosage, frequency, startDate, endDate ->
                val editing = medToEdit
                if (editing == null) {
                    // NUEVA
                    vm.addMedication(name, dosage, frequency, startDate, endDate)
                } else {
                    // EDITAR
                    vm.updateMedication(
                        editing.copy(
                            name = name,
                            dosage = dosage,
                            frequency = frequency,
                            startDate = startDate,
                            endDate = endDate
                        )
                    )
                }
                showDialog = false
                medToEdit = null
            }
        )
    }
}

@Composable
private fun MedicationItem(
    med: MedicationEntity,
    hasReminder: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val takenLabel = if (med.taken) "Toma registrada" else "Pendiente"
    val colorScheme = MaterialTheme.colorScheme

    val reminderText = if (hasReminder) "Recordatorios: activos" else "Recordatorios: sin avisos"
    val reminderColor = if (hasReminder) colorScheme.tertiary else colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (med.taken) {
            CardDefaults.cardColors(
                containerColor = colorScheme.secondaryContainer,
                contentColor = colorScheme.onSecondaryContainer
            )
        } else {
            CardDefaults.cardColors(
                containerColor = colorScheme.primaryContainer,
                contentColor = colorScheme.onPrimaryContainer
            )
        }
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                med.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text("Dosis: ${med.dosage}")
            Text("Frecuencia: ${med.frequency}")
            if (med.startDate.isNotBlank() || med.endDate.isNotBlank()) {
                Text("Desde ${med.startDate} hasta ${med.endDate}")
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = reminderText,
                style = MaterialTheme.typography.labelMedium,
                color = reminderColor
            )

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElevatedButton(onClick = onToggle) {
                    Text(
                        if (med.taken) "✔ $takenLabel"
                        else "Marcar como tomada"
                    )
                }
                TextButton(onClick = onEdit) {
                    Text("Editar")
                }
                TextButton(onClick = onDelete) {
                    Text("Eliminar")
                }
            }
        }
    }
}

@Composable
private fun AddOrEditMedicationDialog(
    initial: MedicationEntity?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, dosage: String, frequency: String, startDate: String, endDate: String) -> Unit
) {
    val context = LocalContext.current
    val baseCalendar = remember { Calendar.getInstance() }

    var name by remember(initial) { mutableStateOf(initial?.name ?: "") }
    var dosage by remember(initial) { mutableStateOf(initial?.dosage ?: "") }
    var frequency by remember(initial) { mutableStateOf(initial?.frequency ?: "") }

    var startMillis by remember { mutableStateOf<Long?>(null) }
    var endMillis by remember { mutableStateOf<Long?>(null) }

    var startLabel by remember(initial) {
        mutableStateOf(initial?.startDate ?: "Selecciona fecha y hora de inicio")
    }
    var endLabel by remember(initial) {
        mutableStateOf(initial?.endDate ?: "Selecciona fecha y hora de fin")
    }

    fun formatMillis(millis: Long): String =
        DateFormat.format("dd/MM/yyyy HH:mm", millis).toString()

    fun pickDateTime(initialMs: Long?, onResult: (Long) -> Unit) {
        val cal = (initialMs?.let { Calendar.getInstance().apply { timeInMillis = it } }
            ?: (baseCalendar.clone() as Calendar))

        DatePickerDialog(
            context,
            { _, y, m, d ->
                cal.set(Calendar.YEAR, y)
                cal.set(Calendar.MONTH, m)
                cal.set(Calendar.DAY_OF_MONTH, d)

                val hour = cal.get(Calendar.HOUR_OF_DAY)
                val minute = cal.get(Calendar.MINUTE)

                TimePickerDialog(
                    context,
                    { _, h, min ->
                        cal.set(Calendar.HOUR_OF_DAY, h)
                        cal.set(Calendar.MINUTE, min)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        onResult(cal.timeInMillis)
                    },
                    hour, minute, true
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nueva medicación" else "Editar medicación") },
        text = {
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                OutlinedTextField(value = dosage, onValueChange = { dosage = it }, label = { Text("Dosis") })
                OutlinedTextField(value = frequency, onValueChange = { frequency = it }, label = { Text("Frecuencia (ej: cada 8h)") })

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            pickDateTime(startMillis) { millis ->
                                startMillis = millis
                                startLabel = formatMillis(millis)
                            }
                        }
                ) {
                    OutlinedTextField(
                        value = startLabel,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Fecha/hora inicio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            pickDateTime(endMillis) { millis ->
                                endMillis = millis
                                endLabel = formatMillis(millis)
                            }
                        }
                ) {
                    OutlinedTextField(
                        value = endLabel,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Fecha/hora fin") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            val isValid =
                name.isNotBlank() &&
                        dosage.isNotBlank() &&
                        frequency.isNotBlank() &&
                        startLabel != "Selecciona fecha y hora de inicio" &&
                        endLabel != "Selecciona fecha y hora de fin"

            TextButton(onClick = {
                onConfirm(name, dosage, frequency, startLabel, endLabel)
            }, enabled = isValid) {
                Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
