package org.univr.telemedicina.model;

import java.time.LocalDate;

// rappresenta un utente del sistema
public class Utente {
    // identificativo univoco dell'utente
    private int IDUtente;
    // email dell'utente
    private String email;
    // password dell'utente (hash)
    private String hashedPassword;
    // nome dell'utente
    private String nome;
    // cognome dell'utente
    private String cognome;
    // ruolo dell'utente
    private String Ruolo;
    // data di nascita dell'utente
    private LocalDate DataNascita;

    // costruttore vuoto per creare un oggetto senza parametri
    public Utente() {}

    // costruttore con parametri
    public Utente(int IDUtente, String email, String hashedPassword, String nome, String cognome, String ruolo, LocalDate dataNascita) {
        this.IDUtente = IDUtente;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.nome = nome;
        this.cognome = cognome;
        this.Ruolo = ruolo;
        this.DataNascita = dataNascita;
    }

    // metodi getter e setter per i campi della classe
    public int getIDUtente() {
        return IDUtente;
    }
    public void setIDUtente(int IDUtente) {
        this.IDUtente = IDUtente;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getRuolo() {
        return Ruolo;
    }
    public void setRuolo(String ruolo) {
        this.Ruolo = ruolo;
    }

    public LocalDate getDataNascita() {
        return DataNascita;
    }
    public void setDataNascita(LocalDate dataNascita) {
        this.DataNascita = dataNascita;
    }
}
