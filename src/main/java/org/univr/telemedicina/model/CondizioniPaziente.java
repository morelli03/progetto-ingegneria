package org.univr.telemedicina.model;

import java.time.LocalDate;

// rappresenta le condizioni di un paziente
public class CondizioniPaziente {
    // identificativo univoco della condizione
    private int IDCondizione;
    // identificativo del paziente a cui si riferisce la condizione
    private int IDPaziente;
    // tipo di condizione
    private String Tipo;
    // descrizione della condizione
    private String Descrizione;
    // periodo della condizione
    private String Periodo;
    // data di registrazione della condizione
    private LocalDate DataRegistrazione;

    // costruttore vuoto per creare un oggetto senza parametri
    public CondizioniPaziente() {}

    // costruttore per inserire una nuova condizione non serve idcondizione perché è auto-incrementato
    public CondizioniPaziente (int IDPaziente, String Tipo, String Descrizione, String Periodo, LocalDate DataRegistrazione) {
        this.IDPaziente = IDPaziente;
        this.Tipo = Tipo;
        this.Descrizione = Descrizione;
        this.Periodo = Periodo;
        this.DataRegistrazione = DataRegistrazione;
    }

    // metodi getter e setter per i campi della classe
    public int getIDCondizione() {
        return IDCondizione;
    }
    public void setIDCondizione(int IDCondizione) {
        this.IDCondizione = IDCondizione;
    }
    public int getIDPaziente() {
        return IDPaziente;
    }
    public void setIDPaziente(int IDPaziente) {
        this.IDPaziente = IDPaziente;
    }
    public String getTipo() {
        return Tipo;
    }
    public void setTipo(String tipo) {
        Tipo = tipo;
    }
    public String getDescrizione() {
        return Descrizione;
    }
    public void setDescrizione(String descrizione) {
        Descrizione = descrizione;
    }
    public String getPeriodo() {
        return Periodo;
    }
    public void setPeriodo(String periodo) {
        Periodo = periodo;
    }
    public LocalDate getDataRegistrazione() {
        return DataRegistrazione;
    }
    public void setDataRegistrazione(LocalDate dataRegistrazione) {
        DataRegistrazione = dataRegistrazione;
    }

}
