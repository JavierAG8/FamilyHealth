package com.example.familyhealth.ui.theme.family

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.familyhealth.viewmodel.FamilyMember
import com.example.familyhealth.viewmodel.FamilyRole
import com.example.familyhealth.viewmodel.FamilyViewModel
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen() {
    val vm: FamilyViewModel = viewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    var showDialog by remember { mutableStateOf(false) }
    var memberToEdit by remember { mutableStateOf<FamilyMember?>(null) }

    LaunchedEffect(Unit) {
        vm.loadFamily()
    }

    val headerColor = colors.primary
    val memberCardColor = colors.primaryContainer

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Familiares y seguimiento",
                        color = colors.onPrimary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = headerColor
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    memberToEdit = null
                    showDialog = true
                },
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            ) {
                Text("+")
            }
        }
    ) { innerPadding ->

        Column(
            Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = headerColor,
                    contentColor = colors.onPrimary
                )
            ) {
                Row(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.FamilyRestroom,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = colors.onPrimary
                    )
                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Red de apoyo",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Añade familiares, cuidadores o profesionales de confianza.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            uiState.error?.let {
                Text(
                    text = "Error: $it",
                    color = colors.error
                )
            }

            if (uiState.saved) {
                Text(
                    text = "Familia actualizada correctamente.",
                    color = colors.primary
                )
                LaunchedEffect(true) { vm.clearSavedFlag() }
            }

            val family = uiState.members.filter { it.role == FamilyRole.FAMILY }
            val caregivers = uiState.members.filter { it.role == FamilyRole.CAREGIVER }
            val professionals = uiState.members.filter { it.role == FamilyRole.PROFESSIONAL }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (family.isEmpty() && caregivers.isEmpty() && professionals.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = colors.surfaceVariant
                            )
                        ) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Todavía no has añadido a nadie.",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    "Pulsa el botón + para añadir un familiar, cuidador o profesional.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                } else {
                    if (family.isNotEmpty()) {
                        item { SectionHeader("Familia") }
                        items(family) { m ->
                            FamilyMemberItem(
                                member = m,
                                containerColor = memberCardColor,
                                onClick = {
                                    memberToEdit = m
                                    showDialog = true
                                },
                                onDelete = { vm.deleteFamilyMember(m) }
                            )
                        }
                    }

                    if (caregivers.isNotEmpty()) {
                        item { SectionHeader("Cuidadores") }
                        items(caregivers) { m ->
                            FamilyMemberItem(
                                member = m,
                                containerColor = memberCardColor,
                                onClick = {
                                    memberToEdit = m
                                    showDialog = true
                                },
                                onDelete = { vm.deleteFamilyMember(m) }
                            )
                        }
                    }

                    if (professionals.isNotEmpty()) {
                        item { SectionHeader("Profesionales sanitarios") }
                        items(professionals) { m ->
                            FamilyMemberItem(
                                member = m,
                                containerColor = memberCardColor,
                                onClick = {
                                    memberToEdit = m
                                    showDialog = true
                                },
                                onDelete = { vm.deleteFamilyMember(m) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddOrEditFamilyDialog(
            initial = memberToEdit,
            onDismiss = {
                showDialog = false
                memberToEdit = null
            },
            onConfirm = { email, alias, role ->
                val editing = memberToEdit
                if (editing == null) {
                    vm.addFamilyMember(email, alias, role)
                } else {
                    vm.updateFamilyMember(
                        editing.copy(email = email, alias = alias, role = role)
                    )
                }
                showDialog = false
                memberToEdit = null
            }
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    )
    HorizontalDivider(
        modifier = Modifier.padding(bottom = 4.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    )
}

@Composable
private fun FamilyMemberItem(
    member: FamilyMember,
    containerColor: Color,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = colors.onPrimaryContainer
        ),
        onClick = onClick
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    color = colors.secondaryContainer,
                    contentColor = colors.onSecondaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            initialsFor(member),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        member.alias.ifBlank { member.email },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        member.email,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        when (member.role) {
                            FamilyRole.FAMILY -> "Familiar"
                            FamilyRole.CAREGIVER -> "Cuidador/a"
                            FamilyRole.PROFESSIONAL -> "Profesional sanitario"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.primary
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDelete) {
                    Text("Eliminar")
                }
            }
        }
    }
}

private fun initialsFor(member: FamilyMember): String {
    val base = member.alias.ifBlank { member.email.substringBefore("@") }
    val parts = base.trim().split(" ")
    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
        parts.isNotEmpty() -> parts[0].take(2).uppercase()
        else -> "FH"
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddOrEditFamilyDialog(
    initial: FamilyMember?,
    onDismiss: () -> Unit,
    onConfirm: (email: String, alias: String, role: FamilyRole) -> Unit
) {
    var email by remember { mutableStateOf(initial?.email ?: "") }
    var alias by remember { mutableStateOf(initial?.alias ?: "") }
    var role by remember { mutableStateOf(initial?.role ?: FamilyRole.FAMILY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Añadir miembro" else "Editar miembro") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") }
                )
                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    label = { Text("Alias (madre, hijo, cuidador...)") }
                )
                Text(
                    "Rol:",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoleChip("Familiar", FamilyRole.FAMILY, role) { role = it }
                    RoleChip("Cuidador", FamilyRole.CAREGIVER, role) { role = it }
                    RoleChip("Profesional", FamilyRole.PROFESSIONAL, role) { role = it }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(email, alias, role) },
                enabled = email.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun RoleChip(
    label: String,
    role: FamilyRole,
    selectedRole: FamilyRole,
    onSelected: (FamilyRole) -> Unit
) {
    FilterChip(
        selected = selectedRole == role,
        onClick = { onSelected(role) },
        label = { Text(label) }
    )
}
