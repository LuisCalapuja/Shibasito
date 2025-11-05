package org.banco.db;

import org.banco.models.Cuenta;
import org.banco.models.Prestamo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BancoDatabase {

    private Connection connect() throws SQLException {
        String DB_URL = "jdbc:postgresql://localhost:5432/banco";
        String DB_PASSWORD = "2581979";
        String DB_USER = "postgres";
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public Cuenta getCuenta(String idCliente) {
        String sql = "SELECT * FROM Cuentas WHERE id_cliente = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idCliente);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Cuenta(
                        rs.getString("id_cuenta"),
                        rs.getString("id_cliente"),
                        rs.getDouble("saldo"),
                        rs.getDate("fecha_apertura")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cuenta: " + e.getMessage());
        }
        return null;
    }

    public List<Prestamo> getPrestamos(String idCliente) {
        List<Prestamo> prestamos = new ArrayList<>();
        String sql = "SELECT * FROM Prestamos WHERE id_cliente = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idCliente);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                prestamos.add(new Prestamo(
                        rs.getString("id_prestamo"),
                        rs.getString("id_cliente"),
                        rs.getDouble("monto_total"),
                        rs.getDouble("saldo_pendiente"),
                        rs.getString("estado"),
                        rs.getDate("fecha_solicitud")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener préstamos: " + e.getMessage());
        }
        return prestamos;
    }

    public boolean registrarTransaccion(String idCuenta, String tipo, double monto) {
        String updateSaldoSql;
        if ("depósito".equals(tipo)) {
            updateSaldoSql = "UPDATE Cuentas SET saldo = saldo + ? WHERE id_cuenta = ?";
        } else if ("retiro".equals(tipo)) {
            updateSaldoSql = "UPDATE Cuentas SET saldo = saldo - ? WHERE id_cuenta = ? AND saldo >= ?";
        } else {
            return false; // Tipo no válido
        }

        String insertTransaccionSql = "INSERT INTO Transacciones (id_transaccion, id_cuenta, tipo, monto) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = connect();
            // Iniciar transacción
            conn.setAutoCommit(false);

            // Actualizar el saldo
            try (PreparedStatement pstmtUpdate = conn.prepareStatement(updateSaldoSql)) {
                pstmtUpdate.setDouble(1, monto);
                pstmtUpdate.setString(2, idCuenta);
                if ("retiro".equals(tipo)) {
                    pstmtUpdate.setDouble(3, monto); // saldo >= monto
                }

                int rowsAffected = pstmtUpdate.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Fondos insuficientes o cuenta no encontrada.");
                }
            }

            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertTransaccionSql)) {
                String idTransaccion = "TR" + UUID.randomUUID().toString().substring(0, 7);
                pstmtInsert.setString(1, idTransaccion);
                pstmtInsert.setString(2, idCuenta);
                pstmtInsert.setString(3, tipo);
                pstmtInsert.setDouble(4, monto);
                pstmtInsert.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error en la transacción, revirtiendo cambios: " + e.getMessage());
            // 4. Si algo falló, revertir
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Error al hacer rollback: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }

    public boolean solicitarPrestamo(String idCliente, double monto) {
        String sql = "INSERT INTO Prestamos (id_prestamo, id_cliente, monto_total, saldo_pendiente, estado) VALUES (?, ?, ?, ?, ?)";
        String idPrestamo = "PR" + UUID.randomUUID().toString().substring(0, 7);

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idPrestamo);
            pstmt.setString(2, idCliente);
            pstmt.setDouble(3, monto);
            pstmt.setDouble(4, monto); // Al inicio, el saldo pendiente es el monto total
            pstmt.setString(5, "activo");

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error al solicitar préstamo: " + e.getMessage());
            return false;
        }
    }
}