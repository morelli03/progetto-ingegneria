package org.univr.telemedicina.model;

import java.time.LocalDate;

// rappresenta una terapia
public class Terapia {
    // identificativo univoco della terapia
    private int IDTerapia;
    // identificativo del paziente a cui è prescritta la terapia
    private int IDPaziente;
    // identificativo del medico che ha prescritto la terapia
    private int IDMedico;
    // nome del farmaco
    private String NomeFarmaco;
    // quantità del farmaco
    private String Quantita;
    // frequenza giornaliera di assunzione
    private int FrequenzaGiornaliera;
    // indicazioni per l'assunzione
    private String Indicazioni;
    // data di inizio della terapia
    private LocalDate DataInizio;
    // data di fine della terapia
    private LocalDate DataFine;


    // costruttore per leggere una terapia dal database
    public Terapia(int IDTerapia, int IDPaziente, int IDMedico, String NomeFarmaco, String Quantita, int FrequenzaGiornaliera, String Indicazioni, LocalDate DataInizio, LocalDate DataFine) {
        this.IDTerapia = IDTerapia;
        this.IDPaziente = IDPaziente;
        this.IDMedico = IDMedico;
        this.NomeFarmaco = NomeFarmaco;
        this.Quantita = Quantita;
        this.FrequenzaGiornaliera = FrequenzaGiornaliera;
        this.Indicazioni = Indicazioni;
        this.DataInizio = DataInizio;
        this.DataFine = DataFine;
    }

    // costruttore per inserire una nuova terapia non serve idterapia perché è auto-incrementato
    public Terapia(int IDPaziente, int IDMedico, String NomeFarmaco, String Quantita, int FrequenzaGiornaliera, String Indicazioni, LocalDate DataInizio, LocalDate DataFine) {
        this.IDPaziente = IDPaziente;
        this.IDMedico = IDMedico;
        this.NomeFarmaco = NomeFarmaco;
        this.Quantita = Quantita;
        this.FrequenzaGiornaliera = FrequenzaGiornaliera;
        this.Indicazioni = Indicazioni;
        this.DataInizio = DataInizio;
        this.DataFine = DataFine;
    }


    // metodi getter e setter per i campi della classe
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
    public int getIDMedico() {
        return IDMedico;
    }
    public void setIDMedico(int IDMedico) {
        this.IDMedico = IDMedico;
    }
    public String getNomeFarmaco() {
        return NomeFarmaco;
    }
    public void setNomeFarmaco(String nomeFarmaco) {
        NomeFarmaco = nomeFarmaco;
    }
    public String getQuantita() {
        return Quantita;
    }
    public void setQuantita(String quantita) {
        Quantita = quantita;
    }
    public int getFrequenzaGiornaliera() {
        return FrequenzaGiornaliera;
    }
    public void setFrequenzaGiornaliera(int frequenzaGiornaliera) {
        FrequenzaGiornaliera = frequenzaGiornaliera;
    }
    public String getIndicazioni() {
        return Indicazioni;
    }
    public void setIndicazioni(String indicazioni) {
        Indicazioni = indicazioni;
    }
    public LocalDate getDataInizio() {
        return DataInizio;
    }
    public void setDataInizio(LocalDate dataInizio) {
        DataInizio = dataInizio;
    }
    public LocalDate getDataFine() {
        return DataFine;
    }
    public void setDataFine(LocalDate dataFine) {
        DataFine = dataFine;
    }
}
