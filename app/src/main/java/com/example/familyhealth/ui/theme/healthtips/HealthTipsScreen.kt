package com.example.familyhealth.ui.theme.healthtips

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite // ❤️ ICONO
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.familyhealth.viewmodel.HealthTipsViewModel
import com.example.familyhealth.viewmodel.HealthTip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthTipsScreen(
    navController: NavController? = null,
    vm: HealthTipsViewModel = viewModel()
) {
    val dailyTip by vm.dailyTip
    val generalTips by vm.generalTips
    val personalizedTips by vm.personalizedTips

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hábitos Saludables") },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Atrás")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Text(
                    text = "Recomendación del día",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (dailyTip != null) {
                    TipCard(tip = dailyTip!!, highlighted = true)
                } else {
                    Text(text = "No hay recomendación disponible en este momento.")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp
                )
            }

            if (personalizedTips.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Recomendaciones personalizadas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(personalizedTips) { tip ->
                    TipCard(tip = tip)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    // ➖ DIVIDER después de las personalizadas
                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Recomendaciones generales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(generalTips) { tip ->
                TipCard(tip = tip)
            }
        }
    }
}


@Composable
private fun TipCard(
    tip: HealthTip,
    highlighted: Boolean = false
) {
    val containerColor =
        if (highlighted) MaterialTheme.colorScheme.tertiary
        else MaterialTheme.colorScheme.primaryContainer

    val titleColor =
        if (highlighted) MaterialTheme.colorScheme.onTertiary
        else MaterialTheme.colorScheme.onPrimaryContainer

    val bodyColor =
        if (highlighted) MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.9f)
        else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)

    val categoryColor =
        if (highlighted) MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.7f)
        else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {

        if (highlighted) {

            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Recomendación destacada",
                    tint = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tip.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tip.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = bodyColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tip.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = categoryColor
                    )
                }
            }
        } else {

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tip.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = bodyColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tip.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = categoryColor
                )
            }
        }
    }
}
