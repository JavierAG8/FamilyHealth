package com.example.familyhealth.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userLocalRepository: UserLocalRepository? = null
) {
    suspend fun login(email: String, password: String): Result<Unit> = try {
        auth.signInWithEmailAndPassword(email, password).await()
        userLocalRepository?.saveCurrentUserFromFirebase()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun register(email: String, password: String): Result<Unit> = try {
        auth.createUserWithEmailAndPassword(email, password).await()
        userLocalRepository?.saveCurrentUserFromFirebase()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null
    fun logout() {
        auth.signOut()
    }
}
