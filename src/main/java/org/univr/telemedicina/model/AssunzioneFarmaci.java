package org.univr.telemedicina.model;

import java.time.LocalDateTime;

// rappresenta l'assunzione di un farmaco da parte di un paziente
public class AssunzioneFarmaci {
    // identificativo univoco dell'assunzione
    private int IDAssunzione;
    // identificativo della terapia a cui si riferisce l'assunzione
    private int IDTerapia;
    // identificativo del paziente che ha assunto il farmaco
    private int IDPaziente;
    // data e ora dell'assunzione
    private LocalDateTime TimestampAssunzione;
    // quantità di farmaco assunta
    private String QuantitaAssunta;


    // costruttore per leggere un'assunzione dal database
    public AssunzioneFarmaci(int IDAssunzione, int IDTerapia, int IDPaziente, LocalDateTime TimestampAssunzione, String QuantitaAssunta) {
        this.IDAssunzione = IDAssunzione;
        this.IDTerapia = IDTerapia;
        this.IDPaziente = IDPaziente;
        this.TimestampAssunzione = TimestampAssunzione;
        this.QuantitaAssunta = QuantitaAssunta;
    }

    // costruttore per inserire una nuova assunzione non serve idassunzione perché è auto-incrementato
    public AssunzioneFarmaci(int IDTerapia, int IDPaziente, LocalDateTime TimestampAssunzione, String QuantitaAssunta) {
        this.IDTerapia = IDTerapia;
        this.IDPaziente = IDPaziente;
        this.TimestampAssunzione = TimestampAssunzione;
        this.QuantitaAssunta = QuantitaAssunta;
    }

    // metodi getter e setter per i campi della classe
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

    public LocalDateTime getTimestampAssunzione() {
        return TimestampAssunzione;
    }
    public void setTimestampAssunzione(LocalDateTime TimestampAssunzione) {
        this.TimestampAssunzione = TimestampAssunzione;
    }

    public String getQuantitaAssunta() {
        return QuantitaAssunta;
    }
    public void setQuantitaAssunta(String QuantitaAssunta) {
        this.QuantitaAssunta = QuantitaAssunta;
    }

}
