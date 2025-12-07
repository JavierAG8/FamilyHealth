package com.example.familyhealth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PatientProfile(
    val name: String = "",
    val age: Int? = null,
    val gender: String = "",
    val weightKg: Float? = null,
    val heightCm: Float? = null,
    val bloodType: String = "",
    val conditions: String = "",
    val allergies: String = "",
    val notes: String = "",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val referenceCenter: String = ""
)

data class ProfileUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val profile: PatientProfile = PatientProfile()
)

class ProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _ui = MutableStateFlow(ProfileUiState())
    val ui: StateFlow<ProfileUiState> = _ui

    init {
        loadProfile()
    }

    fun loadProfile() {
        val user = auth.currentUser
        if (user == null) {
            _ui.value = _ui.value.copy(
                loading = false,
                error = "No hay usuario autenticado."
            )
            return
        }

        _ui.value = _ui.value.copy(loading = true, error = null, message = null)

        // Leemos de: users/{uid}/profile/basic
        db.collection("users")
            .document(user.uid)
            .collection("profile")
            .document("basic")
            .get()
            .addOnSuccessListener { doc ->
                val p = if (doc != null && doc.exists()) {
                    PatientProfile(
                        name = doc.getString("name") ?: "",
                        age = doc.getLong("age")?.toInt(),
                        gender = doc.getString("gender") ?: "",
                        weightKg = doc.getDouble("weightKg")?.toFloat(),
                        heightCm = doc.getDouble("heightCm")?.toFloat(),
                        bloodType = doc.getString("bloodType") ?: "",
                        conditions = doc.getString("conditions") ?: "",
                        allergies = doc.getString("allergies") ?: "",
                        notes = doc.getString("notes") ?: "",
                        emergencyContactName = doc.getString("emergencyContactName") ?: "",
                        emergencyContactPhone = doc.getString("emergencyContactPhone") ?: "",
                        referenceCenter = doc.getString("referenceCenter") ?: ""
                    )
                } else {
                    PatientProfile()
                }

                _ui.value = _ui.value.copy(
                    loading = false,
                    profile = p,
                    error = null
                )
            }
            .addOnFailureListener { e ->
                _ui.value = _ui.value.copy(
                    loading = false,
                    error = e.message ?: "Error al cargar el perfil."
                )
            }
    }

    fun saveProfile(
        name: String,
        age: String,
        gender: String,
        weight: String,
        height: String,
        bloodType: String,
        conditions: String,
        allergies: String,
        notes: String,
        emergencyContactName: String,
        emergencyContactPhone: String,
        referenceCenter: String
    ) {
        val user = auth.currentUser
        if (user == null) {
            _ui.value = _ui.value.copy(error = "No hay usuario autenticado.")
            return
        }

        val ageInt = age.toIntOrNull()
        val weightFloat = weight.replace(",", ".").toFloatOrNull()
        val heightFloat = height.replace(",", ".").toFloatOrNull()

        val profile = PatientProfile(
            name = name.trim(),
            age = ageInt,
            gender = gender.trim(),
            weightKg = weightFloat,
            heightCm = heightFloat,
            bloodType = bloodType.trim().uppercase(),
            conditions = conditions.trim(),
            allergies = allergies.trim(),
            notes = notes.trim(),
            emergencyContactName = emergencyContactName.trim(),
            emergencyContactPhone = emergencyContactPhone.trim(),
            referenceCenter = referenceCenter.trim()
        )

        _ui.value = _ui.value.copy(saving = true, error = null, message = null)

        val data = mapOf(
            "name" to profile.name,
            "age" to profile.age,
            "gender" to profile.gender,
            "weightKg" to profile.weightKg,
            "heightCm" to profile.heightCm,
            "bloodType" to profile.bloodType,
            "conditions" to profile.conditions,
            "allergies" to profile.allergies,
            "notes" to profile.notes,
            "emergencyContactName" to profile.emergencyContactName,
            "emergencyContactPhone" to profile.emergencyContactPhone,
            "referenceCenter" to profile.referenceCenter
        )

        viewModelScope.launch {
            db.collection("users")
                .document(user.uid)
                .collection("profile")
                .document("basic")
                .set(data)
                .addOnSuccessListener {
                    _ui.value = _ui.value.copy(
                        saving = false,
                        profile = profile,
                        message = "Perfil guardado correctamente."
                    )
                }
                .addOnFailureListener { e ->
                    _ui.value = _ui.value.copy(
                        saving = false,
                        error = e.message ?: "Error al guardar el perfil."
                    )
                }
        }
    }

    fun consumeMessage() {
        _ui.value = _ui.value.copy(message = null)
    }
}
