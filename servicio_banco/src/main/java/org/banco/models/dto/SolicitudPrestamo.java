package org.banco.models.dto;

public class SolicitudPrestamo {
    private String idCliente;
    private double montoTotal;
    private String dni;

    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }
    public double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(double montoTotal) { this.montoTotal = montoTotal; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
}