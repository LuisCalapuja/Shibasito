package com.shibasito.cliente

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity

import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

import com.shibasito.cliente.databinding.ActivityMainBinding
import com.shibasito.cliente.middleware.RabbitCliente
import com.shibasito.cliente.models.SolicitudPrestamo
import com.shibasito.cliente.models.TransaccionQR

import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding

    private val rabbitCliente = RabbitCliente()
    private val gson = Gson()

    private var clienteIdValidado: String? = null
    private var dniValidado: String? = null

    // --- Colas de RabbitMQ ---
    private val COLA_RENIEC = "cola_validacion_reniec"
    private val COLA_PRESTAMOS = "cola_solicitud_prestamo"
    private val COLA_TRANSACCIONES = "cola_transaccion_qr"

    private val qrScannerLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_LONG).show()
        } else {
            addLog("Escaneo exitoso: ${result.contents}")
            manejarResultadoQR(result.contents)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Iniciar la conexión a RabbitMQ en segundo plano
        lifecycleScope.launch {
            rabbitCliente.connect()
        }

        // listeners de los botones
        binding.btnValidar.setOnClickListener { onValidarClicked() }
        binding.btnScanQR.setOnClickListener { onScanQRClicked() }
        binding.btnSolicitarPrestamo.setOnClickListener { onSolicitarPrestamoClicked() }
    }

    private fun onValidarClicked() {
        val dni = binding.txtDni.text.toString()
        val idCliente = binding.txtClienteId.text.toString()

        if (dni.isEmpty() || idCliente.isEmpty()) {
            Toast.makeText(this, "Debe ingresar DNI y ID Cliente", Toast.LENGTH_SHORT).show()
            return
        }

        addLog("Yo: Validando mi identidad con DNI $dni...")

        lifecycleScope.launch {
            // 1. Llamar al servicio RENIEC
            val respuestaReniec = rabbitCliente.call(COLA_RENIEC, dni)

            // 'respuestaReniec' será un JSON si es exitoso, o un string vacío si falla
            if (respuestaReniec.isNotEmpty() && !respuestaReniec.startsWith("Error:")) {
                clienteIdValidado = idCliente
                dniValidado = dni

                // 'respuestaReniec' es el JSON del ciudadano
                addLog("ChatB: ¡Bienvenido! Usuario $idCliente validado por RENIEC.")
                println("Datos RENIEC: $respuestaReniec") // Para depuración
                Toast.makeText(this@MainActivity, "RENIEC: Usuario validado", Toast.LENGTH_SHORT).show()

                // Habilitar el resto de la UI
                binding.btnValidar.isEnabled = false
                binding.btnScanQR.isEnabled = true
                binding.btnSolicitarPrestamo.isEnabled = true
            } else {
                addLog("ChatB: DNI $dni no encontrado en RENIEC. Acceso denegado.")
                Toast.makeText(this@MainActivity, "RENIEC: DNI no encontrado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onScanQRClicked() {
        val options = ScanOptions()
        options.setPrompt("Escanea un QR de pago Shibasito")
        options.setOrientationLocked(true)
        qrScannerLauncher.launch(options)
    }

    private fun manejarResultadoQR(qrData: String) {
        try {
            val partes = qrData.split(":")
            val tipo = partes[0]
            val monto = partes[1].toDouble()
            val cuentaDestino = partes[2]

            val transaccion = TransaccionQR(
                idClienteOrigen = this.clienteIdValidado!!,
                idCuentaDestino = cuentaDestino,
                monto = monto,
                tipo = if (tipo == "pago") "retiro" else "depósito"
            )

            addLog("Yo: Quiero realizar un $tipo de S/ $monto a $cuentaDestino.")

            lifecycleScope.launch {
                val mensajeJson = gson.toJson(transaccion)
                // Llamar al servicio BANCO
                val respuestaBanco = rabbitCliente.call(COLA_TRANSACCIONES, mensajeJson)
                addLog("ChatB: $respuestaBanco")
            }

        } catch (e: Exception) {
            addLog("ChatB: Error. El código QR no es válido ($qrData).")
            e.printStackTrace()
        }
    }

    private fun onSolicitarPrestamoClicked() {
        val monto = 5000.0 // Monto de prueba

        val solicitud = SolicitudPrestamo(
            idCliente = this.clienteIdValidado!!,
            montoTotal = monto,
            dni = this.dniValidado!!
        )

        addLog("Yo: Hola, quiero solicitar un préstamo de S/ $monto.")

        lifecycleScope.launch {
            addLog("ChatB: Solicitud de préstamo recibida. Validando en RENIEC...")
            // Llamar al servicio BANCO
            val mensajeJson = gson.toJson(solicitud)
            val respuestaBanco = rabbitCliente.call(COLA_PRESTAMOS, mensajeJson)

            // Esta llamada es bloqueante. El banco no responderá hasta
            // que ÉL MISMO haya consultado a RENIEC.

            addLog("ChatB: $respuestaBanco")
        }
    }

    private fun addLog(mensaje: String) {
        runOnUiThread {
            val logActual = binding.txtResultados.text.toString()
            binding.txtResultados.text = "$logActual\n> $mensaje"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Limpiar la conexión al cerrar la app
        rabbitCliente.close()
    }
}
