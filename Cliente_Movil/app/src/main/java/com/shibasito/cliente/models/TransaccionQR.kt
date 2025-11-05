package com.shibasito.cliente.models

// DTO para enviar una transacción (leída del QR) al banco
data class TransaccionQR(
    val idClienteOrigen: String,
    val idCuentaDestino: String, // Asumimos que el QR nos da esto
    val monto: Double,
    val tipo: String // "depósito" o "retiro"
)