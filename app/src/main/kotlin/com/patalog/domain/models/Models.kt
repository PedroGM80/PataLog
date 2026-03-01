package com.patalog.domain.models

import java.time.LocalDateTime

/**
 * Representa un animal/paciente.
 */
data class Animal(
    val id: Long = 0,
    val name: String,
    val species: String,
    val breed: String = "",
    val ownerId: Long,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Representa un propietario.
 */
data class Owner(
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Representa una consulta veterinaria.
 */
data class Consultation(
    val id: Long = 0,
    val animalId: Long,
    val date: LocalDateTime = LocalDateTime.now(),
    val transcript: String = "",
    val report: String = "",
    val notes: String = ""
)

/**
 * Configuracion de la clinica (Sprint 3).
 */
data class ClinicConfig(
    val name: String = "Clinica Veterinaria",
    val address: String = "",
    val phone: String = "",
    val logoPath: String? = null,
    val vetLicense: String = "",
    val ollamaModel: String = "llama3",
    val transcriptionLanguage: String = "es",
    val pdfOutputFolder: String = "",
    val darkMode: Boolean = false
)
