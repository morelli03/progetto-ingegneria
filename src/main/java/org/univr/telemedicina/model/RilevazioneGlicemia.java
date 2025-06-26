package org.univr.telemedicina.model;

import java.sql.Timestamp;

public class RilevazioneGlicemia {
    private int idRilevazione;
    private int idPaziente;
    private int valore;
    private Timestamp timestamp; // Timestamp per registrare la data e l'ora della rilevazione
    private String note;

    //costruttore vuoto
    public RilevazioneGlicemia() {}

    public RilevazioneGlicemia(int idRilevazione, int idPaziente, int valore, Timestamp timestamp, String note) {
        this.idRilevazione = idRilevazione;
        this.idPaziente = idPaziente;
        this.valore = valore;
        this.timestamp = timestamp;
        this.note = note;
    }

    //costruttore senza idRilevazione, se idRilevazione viene genrerato automaticamente dal database
    public RilevazioneGlicemia(int idPaziente, int valore, Timestamp timestamp, String note) {
        this.idPaziente = idPaziente;
        this.valore = valore;
        this.timestamp = timestamp;
        this.note = note;
    }

    //metodi get e set
    public int getIdRilevazione() {
        return idRilevazione;
    }
    public void setIdRilevazione(int idRilevazione) {
        this.idRilevazione = idRilevazione;
    }

    public int getIdPaziente() {
        return idPaziente;
    }
    public void setIdPaziente(int idPaziente) {
        this.idPaziente = idPaziente;
    }

    public int getValore() {
        return valore;
    }
    public void setValore(int valore) {
        this.valore = valore;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
}

