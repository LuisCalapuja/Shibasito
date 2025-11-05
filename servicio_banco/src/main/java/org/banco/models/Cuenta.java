package org.banco.models;

import java.util.Date;

public class Cuenta {
    private String idCuenta;
    private String idCliente;
    private double saldo;
    private Date fechaApertura;

    public Cuenta(String idCuenta, String idCliente, double saldo, Date fechaApertura) {
        this.idCuenta = idCuenta;
        this.idCliente = idCliente;
        this.saldo = saldo;
        this.fechaApertura = fechaApertura;
    }

    public String getIdCuenta() { return idCuenta; }
    public String getIdCliente() { return idCliente; }
    public double getSaldo() { return saldo; }
    public Date getFechaApertura() { return fechaApertura; }

    @Override
    public String toString() {
        return "Cuenta{" +
                "idCuenta='" + idCuenta + '\'' +
                ", idCliente='" + idCliente + '\'' +
                ", saldo=" + saldo +
                '}';
    }
}