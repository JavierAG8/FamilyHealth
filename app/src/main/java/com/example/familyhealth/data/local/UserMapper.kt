package com.example.familyhealth.data.local

import com.example.familyhealth.data.User

fun UserEntity.toDomain(): User =
    User(
        id = id,
        name = name,
        email = email,
        age = age
    )

fun User.toEntity(): UserEntity =
    UserEntity(
        id = id,
        name = name,
        email = email,
        age = age
    )
