package org.univr.telemedicina.model;

import java.sql.Timestamp;

public class AssunzioneFarmaci {
    private int IDAssunzione;
    private int IDTerapia;
    private int IDPaziente;
    private Timestamp TimestampAssunzione;
    private String QuantitaAssunta;

    public AssunzioneFarmaci(int IDAssunzione, int IDTerapia, int IDPaziente, Timestamp TimestampAssunzione, String QuantitaAssunta) {
        this.IDAssunzione = IDAssunzione;
        this.IDTerapia = IDTerapia;
        this.IDPaziente = IDPaziente;
        this.TimestampAssunzione = TimestampAssunzione;
        this.QuantitaAssunta = QuantitaAssunta;
    }

    //metodi getter e setter
    public int getIDAssunzione() {
        return IDAssunzione;
    }
    public void setIDAssunzione(int IDAssunzione) {
        this.IDAssunzione = IDAssunzione;
    }

    public int getIDTerapia() {
        return IDTerapia;
    }
    public void setIDTerapia(int IDTerapia) {
        this.IDTerapia = IDTerapia;
    }

    public int getIDPaziente() {
        return IDPaziente;
    }
    public void setIDPaziente(int IDPaziente) {
        this.IDPaziente = IDPaziente;
    }

    public Date getTimestampAssunzione() {
        return TimestampAssunzione;
    }
    public void setTimestampAssunzione(Timestamp TimestampAssunzione) {
        this.TimestampAssunzione = TimestampAssunzione;
    }

    public String getQuantitaAssunta() {
        return QuantitaAssunta;
    }
    public void setQuantitaAssunta(String QuantitaAssunta) {
        this.QuantitaAssunta = QuantitaAssunta;
    }

}
