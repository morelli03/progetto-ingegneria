package org.univr.telemedicina.model;
import java.time.LocalDateTime;

// rappresenta una rilevazione di glicemia
public class RilevazioneGlicemia {
    // identificativo univoco della rilevazione
    private int idRilevazione;
    // identificativo del paziente a cui si riferisce la rilevazione
    private int idPaziente;
    // valore della glicemia
    private int valore;
    // data e ora della rilevazione
    private LocalDateTime timestamp;
    // note sulla rilevazione
    private String note;

    // costruttore vuoto per creare un oggetto senza parametri
    public RilevazioneGlicemia() {}

    // costruttore per leggere una rilevazione dal database
    public RilevazioneGlicemia(int idRilevazione, int idPaziente, int valore, LocalDateTime timestamp, String note) {
        this.idRilevazione = idRilevazione;
        this.idPaziente = idPaziente;
        this.valore = valore;
        this.timestamp = timestamp;
        this.note = note;
    }

    // costruttore per inserire una nuova rilevazione non serve idrilevazione perché è auto-incrementato
    public RilevazioneGlicemia(int idPaziente, int valore, LocalDateTime timestamp, String note) {
        this.idPaziente = idPaziente;
        this.valore = valore;
        this.timestamp = timestamp;
        this.note = note;
    }

    // metodi getter e setter per i campi della classe
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
}


