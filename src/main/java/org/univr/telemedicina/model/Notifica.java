package org.univr.telemedicina.model;

import java.time.LocalDateTime;

// rappresenta una notifica
public class Notifica {
    // identificativo univoco della notifica
    private int IdNotifica;
    // identificativo del destinatario della notifica
    private int IdDestinatario;
    // priorità della notifica
    private int Priorita;
    // titolo della notifica
    private String Titolo;
    // messaggio della notifica
    private String Messaggio;
    // tipo di notifica
    private String Tipo;
    // indica se la notifica è stata letta
    private int Letta;
    // data e ora della notifica
    private LocalDateTime Timestamp;


    // costruttore per inserire una nuova notifica
    public Notifica(int idDestinatario, int priorita, String titolo, String messaggio, String tipo, LocalDateTime timestamp) {
        this.IdDestinatario = idDestinatario;
        this.Priorita = priorita;
        this.Titolo = titolo;
        this.Messaggio = messaggio;
        this.Tipo = tipo;
        this.Letta = 0; // 0 non letta 1 letta
        this.Timestamp = timestamp;
    }

    // costruttore per leggere una notifica dal database
    public Notifica(int IDNotifica ,int idDestinatario, int priorita, String titolo, String messaggio, String tipo, int letta, LocalDateTime timestamp) {
        this.IdNotifica = IDNotifica;
        this.IdDestinatario = idDestinatario;
        this.Priorita = priorita;
        this.Titolo = titolo;
        this.Messaggio = messaggio;
        this.Tipo = tipo;
        this.Letta = letta;
        this.Timestamp = timestamp;
    }

    // metodi getter e setter per i campi della classe
    public int getIdNotifica() {
        return IdNotifica;
    }
    public void setIdNotifica(int idNotifica) {
        this.IdNotifica = idNotifica;
    }

    public int getIdDestinatario() {
        return IdDestinatario;
    }
    public void setIdDestinatario(int idDestinatario) {
        this.IdDestinatario = idDestinatario;
    }

    public int getPriorita() {
        return Priorita;
    }
    public void setPriorita(int priorita) {
        this.Priorita = priorita;
    }

    public String getTitolo() {
        return Titolo;
    }
    public void setTitolo(String titolo) {
        this.Titolo = titolo;
    }

    public String getMessaggio() {
        return Messaggio;
    }
    public void setMessaggio(String messaggio) {
        this.Messaggio = messaggio;
    }

    public String getTipo() {
        return Tipo;
    }
    public void setTipo(String tipo) {
        this.Tipo = tipo;
    }

    public int getLetta() {
        return Letta;
    }
    public void setLetta(int letta) {
        this.Letta = letta;
    }

    public LocalDateTime getTimestamp() {
        return Timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.Timestamp = timestamp;
    }

}
