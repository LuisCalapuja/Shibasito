const { MongoClient } = require("mongodb");

// Configuración a MongoDB
const uri = "mongodb://localhost:27017";
const dbName = "reniec_db";
const client = new MongoClient(uri);

let collectionPersonas;

async function connectDB() {
    try {
        await client.connect();
        console.log("[RENIEC] Conectado exitosamente a MongoDB.");
        const db = client.db(dbName);
        collectionPersonas = db.collection("personas");
    } catch (e) {
        console.error("Error conectando a MongoDB:", e);
        process.exit(1);
    }
}

async function validarCiudadano(dni) {
    if (!collectionPersonas) {
        console.error("La conexión a la BD no está inicializada.");
        return null;
    }

    console.log(`[RENIEC] Buscando ciudadano con DNI: ${dni}`);
    
    try {
        // Busca por dni
        const ciudadano = await collectionPersonas.findOne({ dni: dni });
        
        if (ciudadano) {
            console.log(`[RENIEC] Ciudadano encontrado: ${ciudadano.nombres} ${ciudadano.apellido_paterno}`);
            // Devolvemos el objeto.
            delete ciudadano._id; 
            return ciudadano;
        } else {
            console.log(`[RENIEC] DNI ${dni} no encontrado.`);
            return null;
        }
    } catch (e) {
        console.error("Error al validar ciudadano:", e);
        return null;
    }
}

// Exportar las funciones
module.exports = { connectDB, validarCiudadano };