package org.univr.telemedicina.model;

import java.time.LocalDateTime;

public class LogOperazioni {
    private int IDLog;
    private int IDMedicoOperante;
    private String TipoOperazione;
    private String DescrizioneOperazione;
    private LocalDateTime Timestamp;

    //costruttore per lettura
    public LogOperazioni(int IDLog, int IDMedicoOperante, String TipoOperazione, String DescrizioneOperazione, LocalDateTime Timestamp) {
        this.IDLog = IDLog;
        this.IDMedicoOperante = IDMedicoOperante;
        this.TipoOperazione = TipoOperazione;
        this.DescrizioneOperazione = DescrizioneOperazione;
        this.Timestamp = Timestamp;
    }

    //costruttore per inserimento, non c'è IDLog perché è auto-incrementato nel database
    public LogOperazioni(int IDMedicoOperante, String TipoOperazione, String DescrizioneOperazione, LocalDateTime Timestamp) {
        this.IDMedicoOperante = IDMedicoOperante;
        this.TipoOperazione = TipoOperazione;
        this.DescrizioneOperazione = DescrizioneOperazione;
        this.Timestamp = Timestamp;
    }

    //metodi getter e setter
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
