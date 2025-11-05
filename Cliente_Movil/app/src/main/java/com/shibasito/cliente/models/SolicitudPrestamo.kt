package com.shibasito.cliente.models

// DTO para enviar una solicitud de préstamo al banco
data class SolicitudPrestamo(
    val idCliente: String,
    val montoTotal: Double,
    val dni: String // El DNI para la validación interna del banco
)