package com.example.familyhealth.data

import com.example.familyhealth.data.local.UserDao
import com.example.familyhealth.data.local.toDomain
import com.example.familyhealth.data.local.toEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserLocalRepository(
    private val userDao: UserDao,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    suspend fun saveCurrentUserFromFirebase() {
        val fbUser = auth.currentUser ?: return

        val user = User(
            id = fbUser.uid,
            name = fbUser.displayName ?: "",
            email = fbUser.email ?: "",
            age = 0
        )

        userDao.insert(user.toEntity())
    }

    fun observeUser(): Flow<User?> =
        userDao.observeCurrentUser().map { it?.toDomain() }

    suspend fun clearLocalUser() {
        userDao.clear()
    }
}