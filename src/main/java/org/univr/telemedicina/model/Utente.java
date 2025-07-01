package org.univr.telemedicina.model;

import java.time.LocalDate;

public class Utente {
    private int IDUtente;
    private String email;
    private String hashedPassword;
    private String nome;
    private String cognome;
    private String Ruolo;
    private LocalDate DataNascita;  //importante che sia di tipo java.sql.Date per compatibilità con il database

    public Utente() {} //costruttore vuoto per poter aggiungere un utente senza parametri

    public Utente(int IDUtente, String email, String hashedPassword, String nome, String cognome, String ruolo, LocalDate dataNascita) { //costruttore con parametri
        this.IDUtente = IDUtente;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.nome = nome;
        this.cognome = cognome;
        this.Ruolo = ruolo;
        this.DataNascita = dataNascita;
    }

    //metodi getter e setter
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
