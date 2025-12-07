@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.familyhealth.ui.components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AppTopBar(title: String) {
    CenterAlignedTopAppBar(title = { Text(title) })
}
