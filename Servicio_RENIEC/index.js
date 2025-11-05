const { connectDB } = require('./db/reniec_db.js');
const { iniciarConsumidorReniec } = require('./middleware/reniecConsumidor.js');

/**
 * Función principal autoejecutable del servicio RENIEC
 */
(async () => {

    // Conectar a la base de datos MongoDB e iniciar el consumidor de RabbitMQ
    await connectDB();
    await iniciarConsumidorReniec();

})();