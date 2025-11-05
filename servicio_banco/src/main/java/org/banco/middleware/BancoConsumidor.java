package org.banco.middleware;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import org.banco.db.BancoDatabase;
import org.banco.models.Cuenta;
import org.banco.models.dto.SolicitudPrestamo;
import org.banco.models.dto.TransaccionQR;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class BancoConsumidor {

    private final static String COLA_TRANSACCIONES = "cola_transaccion_qr";
    private final static String COLA_PRESTAMOS = "cola_solicitud_prestamo";
    private final static String COLA_RENIEC = "cola_validacion_reniec";

    private final BancoDatabase db;
    private final RabbitPublisher publisher;
    private final Gson gson = new Gson();
    private Channel channel;

    public BancoConsumidor(BancoDatabase db, RabbitPublisher publisher) {
        this.db = db;
        this.publisher = publisher;
    }

    public void iniciarConsumidores() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        this.channel = connection.createChannel();

        System.out.println("[BANCO] Servicio de Banco en línea.");

        iniciarConsumidorTransacciones();
        iniciarConsumidorPrestamos();
    }

    private void iniciarConsumidorTransacciones() throws IOException {
        channel.queueDeclare(COLA_TRANSACCIONES, false, false, false, null);
        System.out.println("[BANCO] Escuchando en " + COLA_TRANSACCIONES);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String mensaje = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("[BANCO] Petición de transacción QR recibida: " + mensaje);

            String respuesta = "";
            try {
                TransaccionQR dto = gson.fromJson(mensaje, TransaccionQR.class);

                Cuenta cuentaOrigen = db.getCuenta(dto.getIdClienteOrigen());
                if (cuentaOrigen == null) {
                    throw new Exception("Cliente de origen no encontrado.");
                }

                boolean exito = db.registrarTransaccion(cuentaOrigen.getIdCuenta(), "retiro", dto.getMonto());

                if (exito) {
                    respuesta = "Transacción QR exitosa por S/ " + dto.getMonto();
                } else {
                    respuesta = "Error: Fondos insuficientes o cuenta inválida.";
                }

            } catch (Exception e) {
                respuesta = "Error al procesar transacción: " + e.getMessage();
            } finally {
                enviarRespuesta(delivery, respuesta);
            }
        };
        channel.basicConsume(COLA_TRANSACCIONES, false, deliverCallback, consumerTag -> {});
    }

    private void iniciarConsumidorPrestamos() throws IOException {
        channel.queueDeclare(COLA_PRESTAMOS, false, false, false, null);
        System.out.println("[BANCO] Escuchando en " + COLA_PRESTAMOS);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String mensaje = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("[BANCO] Petición de préstamo recibida: " + mensaje);

            String respuesta = "";
            try {
                SolicitudPrestamo dto = gson.fromJson(mensaje, SolicitudPrestamo.class);

                // 1. Validar DNI en RENIEC
                System.out.println("[BANCO] Validando DNI " + dto.getDni() + " en RENIEC");
                String respuestaReniec = publisher.llamarRpc(COLA_RENIEC, dto.getDni());

                if (respuestaReniec.isEmpty()) {
                    throw new Exception("Validación fallida. DNI no encontrado.");
                }

                System.out.println("[BANCO] RENIEC OK: " + respuestaReniec);

                // Si RENIEC aprueba, registrar el préstamo
                boolean exito = db.solicitarPrestamo(dto.getIdCliente(), dto.getMontoTotal());

                if (exito) {
                    respuesta = "Préstamo aprobado y registrado.";
                } else {
                    respuesta = "Error: No se pudo registrar el préstamo en la BD.";
                }

            } catch (Exception e) {
                respuesta = "Error al procesar préstamo: " + e.getMessage();
                e.printStackTrace();
            } finally {
                enviarRespuesta(delivery, respuesta);
            }
        };
        channel.basicConsume(COLA_PRESTAMOS, false, deliverCallback, consumerTag -> {});
    }

    private void enviarRespuesta(Delivery delivery, String respuesta) throws IOException {
        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                .Builder()
                .correlationId(delivery.getProperties().getCorrelationId())
                .build();

        channel.basicPublish(
                "",
                delivery.getProperties().getReplyTo(),
                replyProps,
                respuesta.getBytes(StandardCharsets.UTF_8)
        );

        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
    }
}