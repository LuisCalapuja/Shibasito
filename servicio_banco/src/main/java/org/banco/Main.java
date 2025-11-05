package org.banco;

import org.banco.db.BancoDatabase;
import org.banco.middleware.BancoConsumidor;
import org.banco.middleware.RabbitPublisher;

public class Main {

    public static void main(String[] args) {
        try {
            BancoDatabase db = new BancoDatabase();

            // llamar a RENIEC
            RabbitPublisher publisher = new RabbitPublisher();

            // escuchar al Cliente
            BancoConsumidor consumidor = new BancoConsumidor(db, publisher);
            consumidor.iniciarConsumidores();

            System.out.println("Servicio Banco iniciado. Presione CTRL+C para detener.");

            // Mantener el hilo principal vivo
            Object lock = new Object();
            synchronized (lock) {
                lock.wait();
            }

        } catch (Exception e) {
            System.err.println("Error fatal al iniciar el servicio de banco:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}