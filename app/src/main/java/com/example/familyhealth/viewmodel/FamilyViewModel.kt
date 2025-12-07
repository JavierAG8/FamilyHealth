package com.example.familyhealth.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

enum class FamilyRole { FAMILY, CAREGIVER, PROFESSIONAL }
enum class AccessStatus { PENDING, ACTIVE }

data class FamilyMember(
    val id: String = "",
    val email: String = "",
    val alias: String = "",
    val role: FamilyRole = FamilyRole.FAMILY,
    val status: AccessStatus = AccessStatus.PENDING
)

data class FamilyUiState(
    val members: List<FamilyMember> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)


class FamilyViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(FamilyUiState())
    val uiState: StateFlow<FamilyUiState> = _uiState


    fun loadFamily() {
        val user = auth.currentUser ?: run {
            _uiState.update { it.copy(error = "Usuario no autenticado") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null, saved = false) }

        db.collection("users")
            .document(user.uid)
            .collection("family")
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.map { doc ->

                    val roleStr = doc.getString("role") ?: FamilyRole.FAMILY.name
                    val statusStr = doc.getString("status") ?: AccessStatus.PENDING.name

                    FamilyMember(
                        id = doc.id,
                        email = doc.getString("email") ?: "",
                        alias = doc.getString("alias") ?: "",
                        role = runCatching { FamilyRole.valueOf(roleStr) }.getOrDefault(FamilyRole.FAMILY),
                        status = runCatching { AccessStatus.valueOf(statusStr) }.getOrDefault(AccessStatus.PENDING)
                    )
                }

                _uiState.update { it.copy(members = list, isLoading = false) }
            }
            .addOnFailureListener { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
    }


    fun addFamilyMember(email: String, alias: String, role: FamilyRole) {
        val user = auth.currentUser ?: run {
            _uiState.update { it.copy(error = "Usuario no autenticado") }
            return
        }

        if (email.isBlank()) return

        _uiState.update { it.copy(isLoading = true, error = null, saved = false) }

        val data = mapOf(
            "email" to email,
            "alias" to alias,
            "role" to role.name,
            "status" to AccessStatus.PENDING.name  // SIEMPRE empieza como pendiente
        )

        db.collection("users")
            .document(user.uid)
            .collection("family")
            .add(data)
            .addOnSuccessListener {
                loadFamily()
                _uiState.update { it.copy(isLoading = false, saved = true) }
            }
            .addOnFailureListener { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
    }


    fun updateFamilyMember(member: FamilyMember) {
        val user = auth.currentUser ?: run {
            _uiState.update { it.copy(error = "Usuario no autenticado") }
            return
        }

        if (member.id.isBlank()) {
            _uiState.update { it.copy(error = "ID inválido") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null, saved = false) }

        val data = mapOf(
            "email" to member.email,
            "alias" to member.alias,
            "role" to member.role.name,
            "status" to member.status.name
        )

        db.collection("users")
            .document(user.uid)
            .collection("family")
            .document(member.id)
            .set(data)
            .addOnSuccessListener {
                loadFamily()
                _uiState.update { it.copy(isLoading = false, saved = true) }
            }
            .addOnFailureListener { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
    }


    fun deleteFamilyMember(member: FamilyMember) {
        val user = auth.currentUser ?: return

        db.collection("users")
            .document(user.uid)
            .collection("family")
            .document(member.id)
            .delete()
            .addOnSuccessListener { loadFamily() }
            .addOnFailureListener { e ->
                _uiState.update { it.copy(error = e.message) }
            }
    }

    fun clearSavedFlag() {
        _uiState.update { it.copy(saved = false) }
    }
}
