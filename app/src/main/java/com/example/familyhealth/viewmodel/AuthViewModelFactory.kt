package com.example.familyhealth.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familyhealth.data.AuthRepository
import com.example.familyhealth.data.UserLocalRepository
import com.example.familyhealth.data.local.AppDatabase

class AuthViewModelFactory(
    private val appContext: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val db = AppDatabase.getInstance(appContext)
            val userLocalRepository = UserLocalRepository(db.userDao())
            val authRepository = AuthRepository(userLocalRepository = userLocalRepository)
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
