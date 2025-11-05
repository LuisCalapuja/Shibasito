package org.banco.middleware;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

// patrón RPC (Petición/Respuesta)
public class RabbitPublisher implements AutoCloseable {

    private Connection connection;
    private Channel channel;

    public RabbitPublisher() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
    }

    /**
     * Llama a un servicio (como RENIEC) y espera una respuesta.
     * @param colaPeticion La cola del servicio
     * @param mensaje El mensaje (ej. el DNI)
     * @return La respuesta del servicio
     */
    public String llamarRpc(String colaPeticion, String mensaje) throws IOException, InterruptedException {
        final String correlationId = UUID.randomUUID().toString();

        String colaRespuesta = channel.queueDeclare().getQueue();

        // propiedades del mensaje
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(correlationId)
                .replyTo(colaRespuesta)
                .build();

        // Publicar mensaje
        channel.basicPublish("", colaPeticion, props, mensaje.getBytes(StandardCharsets.UTF_8));
        System.out.println("[BANCO] Enviando petición a " + colaPeticion + " (ID: " + correlationId + ")");

        final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

        String ctag = channel.basicConsume(colaRespuesta, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(correlationId)) {
                response.offer(new String(delivery.getBody(), StandardCharsets.UTF_8));
            }
        }, consumerTag -> {});

        String result = response.take(); // Esperar la respuesta

        channel.basicCancel(ctag);
        channel.queueDelete(colaRespuesta);

        return result;
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }
}