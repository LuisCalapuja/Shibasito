package com.shibasito.cliente.middleware

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.ArrayBlockingQueue

class RabbitCliente {

    private var connection: Connection? = null
    private var channel: Channel? = null

    companion object {
        private const val RABBITMQ_HOST = "10.0.2.2" // IP de Android para 'localhost'
    }

    suspend fun connect() {
        withContext(Dispatchers.IO) { // Ejecutar en hilo de I/O
            try {
                val factory = ConnectionFactory()
                factory.host = RABBITMQ_HOST
                factory.username = "guest"
                factory.password = "guest"
                connection = factory.newConnection()
                channel = connection?.createChannel()
                println("[CLIENTE] Conectado a RabbitMQ en $RABBITMQ_HOST")
            } catch (e: Exception) {
                println("[CLIENTE] Error al conectar a RabbitMQ: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    suspend fun call(colaPeticion: String, mensaje: String): String {
        if (channel == null) {
            println("[CLIENTE] No conectado a RabbitMQ.")
            connect() // Intento de reconexión simple
            if (channel == null) return "Error: No se pudo conectar a RabbitMQ"
        }

        return withContext(Dispatchers.IO) { // Ejecutar en hilo de I/O
            try {

                val colaRespuesta = channel!!.queueDeclare().queue
                val correlationId = UUID.randomUUID().toString()

                // Configurar propiedades de la petición
                val props = AMQP.BasicProperties.Builder()
                    .correlationId(correlationId)
                    .replyTo(colaRespuesta)
                    .build()

                println("[CLIENTE] Enviando a $colaPeticion: $mensaje")
                channel!!.basicPublish("", colaPeticion, props, mensaje.toByteArray(Charsets.UTF_8))

                val response = ArrayBlockingQueue<String>(1)

                // Empezar a consumir de la cola de respuesta
                val ctag = channel!!.basicConsume(colaRespuesta, true, { _, delivery ->
                    if (delivery.properties.correlationId == correlationId) {
                        response.offer(String(delivery.body, Charsets.UTF_8))
                    }
                }, { _ -> })

                // Esperar la respuesta
                val resultado = response.take() // Espera aquí hasta que llegue la respuesta
                channel!!.basicCancel(ctag) // Dejar de consumir
                channel!!.queueDelete(colaRespuesta) // Borrar la cola temporal
                println("<<< [CLIENTE] Respuesta recibida: $resultado")
                resultado

            } catch (e: Exception) {
                e.printStackTrace()
                "Error: ${e.message}"
            }
        }
    }

    fun close() {
        try {
            channel?.close()
            connection?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}