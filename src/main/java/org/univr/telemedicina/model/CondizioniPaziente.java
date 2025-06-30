package org.univr.telemedicina.model;

import java.time.LocalDate;

public class CondizioniPaziente {
    private int IDCondizione;
    private int IDPaziente;
    private String Tipo;
    private String Descrizione;
    private String Periodo;
    private LocalDate DataRegistrazione;  //importante che sia di tipo java.sql.LocalDate per compatibilità con il database

    public CondizioniPaziente() {} //costruttore vuoto per poter aggiungere un utente senza parametri

    // costruttori senza IDCondizione, gestitio da database con auto-increment
    public CondizioniPaziente (int IDPaziente, String Tipo, String Descrizione, String Periodo, LocalDate DataRegistrazione) {
        this.IDPaziente = IDPaziente;
        this.Tipo = Tipo;
        this.Descrizione = Descrizione;
        this.Periodo = Periodo;
        this.DataRegistrazione = DataRegistrazione;
    }

    //metodi getter e setter
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
