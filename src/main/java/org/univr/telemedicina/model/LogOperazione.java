package org.univr.telemedicina.model;

import java.time.LocalDateTime;

// rappresenta un'operazione di log
public class LogOperazione {
    // identificativo univoco del log
    private int IDLog;
    // identificativo del medico che ha eseguito l'operazione
    private int IDMedicoOperante;
    // identificativo del paziente interessato dall'operazione
    private int IDPazienteInteressato;
    // tipo di operazione
    private String TipoOperazione;
    // descrizione dell'operazione
    private String DescrizioneOperazione;
    // data e ora dell'operazione
    private LocalDateTime Timestamp;

    // costruttore per leggere un log dal database
    public LogOperazione(int IDLog, int IDMedicoOperante, int IDPazienteInteressato, String TipoOperazione, String DescrizioneOperazione, LocalDateTime Timestamp) {
        this.IDLog = IDLog;
        this.IDMedicoOperante = IDMedicoOperante;
        this.IDPazienteInteressato = IDPazienteInteressato;
        this.TipoOperazione = TipoOperazione;
        this.DescrizioneOperazione = DescrizioneOperazione;
        this.Timestamp = Timestamp;
    }

    // costruttore per inserire un nuovo log non serve idlog perché è auto-incrementato
    public LogOperazione(int IDMedicoOperante, int IDPazienteInteressato, String TipoOperazione, String DescrizioneOperazione, LocalDateTime Timestamp) {
        this.IDMedicoOperante = IDMedicoOperante;
        this.IDPazienteInteressato = IDPazienteInteressato;
        this.TipoOperazione = TipoOperazione;
        this.DescrizioneOperazione = DescrizioneOperazione;
        this.Timestamp = Timestamp;
    }

    // costruttore vuoto per creare un oggetto senza parametri
    public LogOperazione(){}

    // metodi getter e setter per i campi della classe
    public int getIDLog() {
        return IDLog;
    }
    public void setIDLog(int IDLog) {
        this.IDLog = IDLog;
    }

    public int getIDMedicoOperante() {
        return IDMedicoOperante;
    }
    public void setIDMedicoOperante(int IDMedicoOperante) {
        this.IDMedicoOperante = IDMedicoOperante;
    }

    public int getIDPazienteInteressato() {
        return IDPazienteInteressato;
    }
    public void setIDPazienteInteressato(int IDPazienteInteressato) {
        this.IDPazienteInteressato = IDPazienteInteressato;
    }

    public String getTipoOperazione() {
        return TipoOperazione;
    }
    public void setTipoOperazione(String TipoOperazione) {
        this.TipoOperazione = TipoOperazione;
    }

    public String getDescrizioneOperazione() {
        return DescrizioneOperazione;
    }
    public void setDescrizioneOperazione(String DescrizioneOperazione) {
        this.DescrizioneOperazione = DescrizioneOperazione;
    }

    public LocalDateTime getTimestamp() {
        return Timestamp;
    }
    public void setTimestamp(LocalDateTime Timestamp) {
        this.Timestamp = Timestamp;
    }
}
