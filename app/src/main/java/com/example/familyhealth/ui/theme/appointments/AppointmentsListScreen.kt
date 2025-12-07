package com.example.familyhealth.ui.theme.appointments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.familyhealth.data.local.AppointmentEntity
import com.example.familyhealth.viewmodel.AppointmentViewModel
import java.util.Calendar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Sync
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.familyhealth.utils.openCalendarInsert

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsListScreen() {
    val vm: AppointmentViewModel = viewModel()
    val items = vm.appointments.collectAsStateWithLifecycle().value

    var showDialog by remember { mutableStateOf(false) }
    var appointmentToEdit by remember { mutableStateOf<AppointmentEntity?>(null) }

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Citas médicas",
                        color = colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    appointmentToEdit = null
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
            // Cabecera
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Próximas citas",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "Lleva el control de tus consultas médicas.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (items.isNotEmpty()) {
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
                }
            }

            Spacer(Modifier.height(16.dp))

            if (items.isEmpty()) {
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
                            "No hay citas aún.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "Añade tu primera cita con el botón +, " +
                                    "o recupera citas desde la nube.",
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
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items) { a ->
                        AppointmentItem(
                            a = a,
                            onEdit = {
                                appointmentToEdit = a
                                showDialog = true
                            },
                            onDelete = { vm.delete(a) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddOrEditAppointmentDialog(
            initial = appointmentToEdit,
            onDismiss = { showDialog = false },
            onConfirm = { doctor, location, startEpoch, endEpoch, notes ->
                val editing = appointmentToEdit
                if (editing == null) {
                    vm.add(doctor, location, startEpoch, endEpoch, notes)
                } else {
                    vm.update(
                        editing.copy(
                            doctor = doctor,
                            location = location,
                            start = startEpoch,
                            end = endEpoch,
                            notes = notes
                        )
                    )
                }
                showDialog = false
                appointmentToEdit = null
            }
        )
    }
}

@Composable
private fun AppointmentItem(
    a: AppointmentEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.primaryContainer,
            contentColor = colorScheme.onPrimaryContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Dr./Consulta: ${a.doctor}",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text("Lugar: ${a.location}")
            Text("Inicio: ${a.start.toHuman()}")
            Text("Fin: ${a.end.toHuman()}")
            if (a.notes.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text("Notas: ${a.notes}")
            }

            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onEdit) {
                    Text("Editar")
                }
                TextButton(onClick = onDelete) {
                    Text("Eliminar")
                }
                TextButton(
                    onClick = {
                        openCalendarInsert(
                            context = context,
                            title = "Cita con ${a.doctor}",
                            location = a.location,
                            startMillis = a.start,
                            endMillis = a.end,
                            notes = a.notes
                        )
                    }
                ) {
                    Text("Añadir a calendario")
                }
            }
        }
    }
}


private fun Long.toHuman(): String =
    DateFormat.format("dd/MM/yyyy HH:mm", this).toString()

@Composable
private fun AddOrEditAppointmentDialog(
    initial: AppointmentEntity?,
    onDismiss: () -> Unit,
    onConfirm: (doctor: String, location: String, start: Long, end: Long, notes: String) -> Unit
) {
    val context = LocalContext.current
    val baseCalendar = remember { Calendar.getInstance() }

    var doctor by remember { mutableStateOf(initial?.doctor ?: "") }
    var location by remember { mutableStateOf(initial?.location ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    var startMillis by remember { mutableStateOf<Long?>(initial?.start) }
    var endMillis by remember { mutableStateOf<Long?>(initial?.end) }

    var startLabel by remember {
        mutableStateOf(initial?.start?.toHuman() ?: "Selecciona fecha y hora de inicio")
    }
    var endLabel by remember {
        mutableStateOf(initial?.end?.toHuman() ?: "Selecciona fecha y hora de fin")
    }

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
        title = { Text(if (initial == null) "Nueva cita" else "Editar cita") },
        text = {
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = doctor, onValueChange = { doctor = it }, label = { Text("Dr./Consulta") })
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Lugar") })

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            pickDateTime(startMillis) { millis ->
                                startMillis = millis
                                startLabel = millis.toHuman()
                            }
                        }
                ) {
                    OutlinedTextField(
                        value = startLabel,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Inicio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            pickDateTime(endMillis) { millis ->
                                endMillis = millis
                                endLabel = millis.toHuman()
                            }
                        }
                ) {
                    OutlinedTextField(
                        value = endLabel,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Fin") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (opcional)") }
                )
            }
        },
        confirmButton = {
            val ok = doctor.isNotBlank() &&
                    location.isNotBlank() &&
                    startMillis != null &&
                    endMillis != null &&
                    endMillis!! > startMillis!!

            TextButton(onClick = {
                onConfirm(doctor, location, startMillis!!, endMillis!!, notes)
            }, enabled = ok) {
                Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
