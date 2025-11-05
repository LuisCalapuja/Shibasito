const { MongoClient } = require("mongodb");

// Configuración a MongoDB 
const uri = "mongodb://localhost:27017";
const dbName = "reniec_db";
const client = new MongoClient(uri);

async function setupDatabase() {
    try {
        await client.connect();
        
        const db = client.db(dbName);
        const collection = db.collection("personas");

        // Verificar si la colección ya tiene datos
        const count = await collection.countDocuments();
        if (count > 0) {
            console.log("La base de datos ya está pobladas.");
            return;
        }

        // Datos de ejemplo
        const personasDeEjemplo = [
            {
                "dni": "45679812",
                "nombres": "MARIA ELENA",
                "apellido_paterno": "GARCIA",
                "apellido_materno": "FLORES",
                "fecha_nacimiento": "1990-07-15",
                "sexo": "F",
                "direccion_actual": "Universitaria 1234"
            },
            {
                "dni": "78001234",
                "nombres": "JUAN CARLOS",
                "apellido_paterno": "RAMIREZ",
                "apellido_materno": "CRISPE",
                "fecha_nacimiento": "1985-03-22",
                "sexo": "M",
                "direccion_actual": "San Martin 456"
            },
            {
                "dni": "12345678",
                "nombres": "LUIS ALBERTO",
                "apellido_paterno": "TORRES",
                "apellido_materno": "MENDOZA",
                "fecha_nacimiento": "1992-11-05",
                "sexo": "M",
                "direccion_actual": "Samayhuamán 789"
            },
            {
                "dni": "23456789",
                "nombres": "ANA SOFIA",
                "apellido_paterno": "CHAVEZ",
                "apellido_materno": "ROJAS",
                "fecha_nacimiento": "1998-06-30",
                "sexo": "F",
                "direccion_actual": "Huancayo 121"
            },
            {
                "dni": "34567890",
                "nombres": "CARLOS JUAN",
                "apellido_paterno": "PEREZ",
                "apellido_materno": "VASQUEZ",
                "fecha_nacimiento": "1979-12-20",
                "sexo": "M",
                "direccion_actual": "Las Palmeras 101"
            }
        ];

        await collection.insertMany(personasDeEjemplo);

    } catch (e) {
        console.error("Error durante la configuración de la BD:", e);
    } finally {
        await client.close();
        console.log("Conexión de configuración cerrada.");
    }
}

setupDatabase();