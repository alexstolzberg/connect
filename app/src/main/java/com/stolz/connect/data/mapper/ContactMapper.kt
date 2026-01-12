package com.stolz.connect.data.mapper

import com.stolz.connect.data.local.entity.CustomContactEntity
import com.stolz.connect.domain.model.CustomContact

fun CustomContactEntity.toDomain(): CustomContact {
    return CustomContact(
        id = id,
        name = name,
        phoneNumber = phoneNumber,
        email = email,
        notes = notes,
        createdAt = createdAt
    )
}

fun CustomContact.toEntity(): CustomContactEntity {
    return CustomContactEntity(
        id = id,
        name = name,
        phoneNumber = phoneNumber,
        email = email,
        notes = notes,
        createdAt = createdAt
    )
}
