package com.stolz.connect.domain.model

data class CustomContact(
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val email: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
