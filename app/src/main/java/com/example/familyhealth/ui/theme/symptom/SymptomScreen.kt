package com.example.familyhealth.ui.theme.symptom

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
import com.example.familyhealth.data.local.SymptomEntity
import com.example.familyhealth.viewmodel.SymptomViewModel
import java.util.Calendar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Sync
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.familyhealth.ui.theme.HardPainContainer
import com.example.familyhealth.ui.theme.LowPainContainer
import com.example.familyhealth.ui.theme.OnHardPainContainer
import com.example.familyhealth.ui.theme.OnLowPainContainer
import com.example.familyhealth.ui.theme.WarningContainer
import com.example.familyhealth.ui.theme.OnWarningContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomScreen() {
    val vm: SymptomViewModel = viewModel()
    val items = vm.symptoms.collectAsStateWithLifecycle().value

    var showDialog by remember { mutableStateOf(false) }
    var symptomToEdit by remember { mutableStateOf<SymptomEntity?>(null) }

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Registro de síntomas",
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
                    symptomToEdit = null
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Evolución de síntomas",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "Anota cómo te sientes para comentar con tu médico.",
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

            if (items.isNotEmpty()) {
                val avgIntensity = remember(items) {
                    items.map { it.intensity }.average()
                }
                val last = items.first()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Resumen rápido",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("Intensidad media: ${"%.1f".format(avgIntensity)}/10")
                        Text(
                            "Último síntoma: ${last.dateTime.toHuman()} (${last.intensity}/10)"
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            if (items.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.secondaryContainer,
                        contentColor = colorScheme.onSecondaryContainer
                    )
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Aún no has registrado síntomas.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "Pulsa + para registrar cómo te sientes, o recupera datos desde la nube.",
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
                    items(items) { s ->
                        SymptomItem(
                            s = s,
                            onEdit = {
                                symptomToEdit = s
                                showDialog = true
                            },
                            onDelete = { vm.deleteSymptom(s) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddOrEditSymptomDialog(
            initial = symptomToEdit,
            onDismiss = {
                showDialog = false
                symptomToEdit = null
            },
            onConfirm = { dateTime, intensity, description, tags ->
                val editing = symptomToEdit
                if (editing == null) {
                    vm.addSymptom(dateTime, intensity, description, tags)
                } else {
                    vm.updateSymptom(
                        editing.copy(
                            dateTime = dateTime,
                            intensity = intensity,
                            description = description,
                            tags = tags
                        )
                    )
                }
                showDialog = false
                symptomToEdit = null
            }
        )
    }
}

@Composable
private fun SymptomItem(
    s: SymptomEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    val containerColor = when (s.intensity) {
        in 1..4 -> LowPainContainer
        in 5..7 -> HardPainContainer
        else -> WarningContainer
    }

    val contentColor = when (s.intensity) {
        in 1..4 -> OnLowPainContainer
        in 5..7 -> OnHardPainContainer
        else -> OnWarningContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Fecha: ${s.dateTime.toHuman()}",
                fontWeight = FontWeight.SemiBold
            )
            Text("Intensidad: ${s.intensity}/10")
            Text("Descripción: ${s.description}")
            if (s.tags.isNotBlank()) Text("Etiquetas: ${s.tags}")

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) { Text("Editar") }
                TextButton(onClick = onDelete) { Text("Eliminar") }
            }
        }
    }
}

private fun Long.toHuman(): String =
    DateFormat.format("dd/MM/yyyy HH:mm", this).toString()

@Composable
private fun AddOrEditSymptomDialog(
    initial: SymptomEntity?,
    onDismiss: () -> Unit,
    onConfirm: (dateTime: Long, intensity: Int, description: String, tags: String) -> Unit
) {
    val context = LocalContext.current
    val baseCalendar = remember { Calendar.getInstance() }

    var dateTimeMillis by remember(initial) { mutableStateOf(initial?.dateTime) }
    var dateLabel by remember(initial) {
        mutableStateOf(
            initial?.dateTime?.let { DateFormat.format("dd/MM/yyyy HH:mm", it).toString() }
                ?: "Selecciona fecha y hora"
        )
    }

    var intensity by remember(initial) { mutableStateOf(initial?.intensity?.toFloat() ?: 5f) }
    var description by remember(initial) { mutableStateOf(initial?.description ?: "") }
    var tags by remember(initial) { mutableStateOf(initial?.tags ?: "") }

    fun formatMillis(millis: Long): String =
        DateFormat.format("dd/MM/yyyy HH:mm", millis).toString()

    fun pickDateTime(initialMs: Long?, onResult: (Long) -> Unit) {
        val cal = (initialMs?.let { Calendar.getInstance().apply { timeInMillis = it } }
            ?: (baseCalendar.clone() as Calendar))

        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

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
                    hour,
                    minute,
                    true
                ).show()
            },
            year,
            month,
            day
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nuevo síntoma" else "Editar síntoma") },
        text = {
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = dateLabel,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            pickDateTime(dateTimeMillis) { millis ->
                                dateTimeMillis = millis
                                dateLabel = formatMillis(millis)
                            }
                        },
                    label = { Text("Fecha y hora") }
                )

                Text("Intensidad: ${intensity.toInt()}/10")
                Slider(
                    value = intensity,
                    onValueChange = { intensity = it },
                    valueRange = 1f..10f,
                    steps = 8
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") }
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Etiquetas (coma separadas)") }
                )
            }
        },
        confirmButton = {
            val dt = dateTimeMillis
            val isValid = dt != null && description.isNotBlank()
            TextButton(
                onClick = {
                    onConfirm(dt!!, intensity.toInt(), description, tags)
                },
                enabled = isValid
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
