const amqp = require('amqplib');
const { validarCiudadano } = require('../db/reniec_db');

const COLA_VALIDACION = 'cola_validacion_reniec';

async function iniciarConsumidorReniec() {
    
    try {
        // Conectar a RabbitMQ
        const connection = await amqp.connect('amqp://localhost');
        const channel = await connection.createChannel();
        console.log("[RENIEC] Conectado a RabbitMQ.");

        await channel.assertQueue(COLA_VALIDACION, { durable: false });

        channel.prefetch(1); 

        console.log(`[RENIEC] Esperando peticiones en la cola '${COLA_VALIDACION}'...`);

        // 3. Consumir mensajes de la cola
        channel.consume(COLA_VALIDACION, async (msg) => {
            if (msg.content) {
                const dni = msg.content.toString();
                console.log(`[RENIEC] Petición recibida para DNI: ${dni}`);

                // Propiedades de Petición/Respuesta
                const replyTo = msg.properties.replyTo;
                const correlationId = msg.properties.correlationId;

                const ciudadano = await validarCiudadano(dni);
                const respuesta = ciudadano ? JSON.stringify(ciudadano) : "";

                // Enviar la respuesta a la cola
                channel.sendToQueue(replyTo, Buffer.from(respuesta), {
                    correlationId: correlationId
                });

                channel.ack(msg);
                console.log(`[RENIEC] Respuesta enviada para DNI: ${dni}`);
            }
        }, {
            noAck: false // Se requiere ACK manual
        });

    } catch (e) {
        console.error("Error en el consumidor de RENIEC:", e);
        process.exit(1);
    }
}

module.exports = { iniciarConsumidorReniec };