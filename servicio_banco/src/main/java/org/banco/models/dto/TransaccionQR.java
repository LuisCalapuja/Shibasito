package org.banco.models.dto;

public class TransaccionQR {
    private String idClienteOrigen;
    private String idCuentaDestino;
    private double monto;
    private String tipo; // "retiro" (para pagar) o "depósito"

    public String getIdClienteOrigen() { return idClienteOrigen; }
    public void setIdClienteOrigen(String idClienteOrigen) { this.idClienteOrigen = idClienteOrigen; }
    public String getIdCuentaDestino() { return idCuentaDestino; }
    public void setIdCuentaDestino(String idCuentaDestino) { this.idCuentaDestino = idCuentaDestino; }
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}