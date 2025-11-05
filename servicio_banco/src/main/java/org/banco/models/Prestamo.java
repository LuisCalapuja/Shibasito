package org.banco.models;

import java.util.Date;

public class Prestamo {
    private String idPrestamo;
    private String idCliente;
    private double montoTotal;
    private double saldoPendiente;
    private String estado;
    private Date fechaSolicitud;

    public Prestamo(String idPrestamo, String idCliente, double montoTotal, double saldoPendiente, String estado, Date fechaSolicitud) {
        this.idPrestamo = idPrestamo;
        this.idCliente = idCliente;
        this.montoTotal = montoTotal;
        this.saldoPendiente = saldoPendiente;
        this.estado = estado;
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getIdPrestamo() { return idPrestamo; }
    public String getIdCliente() { return idCliente; }
    public double getMontoTotal() { return montoTotal; }
    public double getSaldoPendiente() { return saldoPendiente; }
    public String getEstado() { return estado; }
    public Date getFechaSolicitud() { return fechaSolicitud; }

    @Override
    public String toString() {
        return "Prestamo{" +
                "idPrestamo='" + idPrestamo + '\'' +
                ", idCliente='" + idCliente + '\'' +
                ", montoTotal=" + montoTotal +
                ", estado='" + estado + '\'' +
                '}';
    }
}