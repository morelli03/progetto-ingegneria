package org.univr.telemedicina.model;

import java.time.LocalDateTime;

public class Notifica {
    private int IdNotifica;
    private int IdDestinatario;
    private int Priorita;
    private String Titolo;
    private String Messaggio;
    private String Tipo;
    private int Letta;
    private LocalDateTime Timestamp;


    // Costruttore per scrivere
    public Notifica(int idDestinatario, int priorita, String titolo, String messaggio, String tipo, LocalDateTime timestamp) {
        this.IdDestinatario = idDestinatario;
        this.Priorita = priorita;
        this.Titolo = titolo;
        this.Messaggio = messaggio;
        this.Tipo = tipo;
        this.Letta = 0; // Default: notifica non letta
        this.Timestamp = timestamp;
    }

    // Costruttore per leggere
    public Notifica(int IDNotifica ,int idDestinatario, int priorita, String titolo, String messaggio, String tipo, int letta, LocalDateTime timestamp) {
        this.IdNotifica = IDNotifica;
        this.IdDestinatario = idDestinatario;
        this.Priorita = priorita;
        this.Titolo = titolo;
        this.Messaggio = messaggio;
        this.Tipo = tipo;
        this.Letta = letta; // Default: notifica non letta
        this.Timestamp = timestamp;
    }

    // metodi getter e setter
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
