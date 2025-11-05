package org.banco.models;

import java.util.Date;

public class Transaccion {
    private String idTransaccion;
    private String idCuenta;
    private String tipo;
    private double monto;
    private Date fecha;

    public Transaccion(String idTransaccion, String idCuenta, String tipo, double monto, Date fecha) {
        this.idTransaccion = idTransaccion;
        this.idCuenta = idCuenta;
        this.tipo = tipo;
        this.monto = monto;
        this.fecha = fecha;
    }

    public String getIdTransaccion() { return idTransaccion; }
    public String getIdCuenta() { return idCuenta; }
    public String getTipo() { return tipo; }
    public double getMonto() { return monto; }
    public Date getFecha() { return fecha; }

    @Override
    public String toString() {
        return "Transaccion{" +
                "idTransaccion='" + idTransaccion + '\'' +
                ", tipo='" + tipo + '\'' +
                ", monto=" + monto +
                '}';
    }
}