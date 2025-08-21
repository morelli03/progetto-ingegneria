package org.univr.telemedicina.model;

// rappresenta un paziente
public class Paziente {
    // identificativo univoco del paziente
    private int IDPaziente;
    // identificativo del medico di riferimento del paziente
    private int IDMedicoRiferimento;

    // costruttore vuoto per creare un oggetto senza parametri
    public Paziente(){}

    // costruttore per creare un oggetto con parametri
    public Paziente(int IDPaziente, int IDMedicoRiferimento) {
        this.IDPaziente = IDPaziente;
        this.IDMedicoRiferimento = IDMedicoRiferimento;
    }

    // metodi getter e setter per i campi della classe
    public int getIDPaziente() {
        return IDPaziente;
    }
    public void setIDPaziente(int IDPaziente) {
        this.IDPaziente = IDPaziente;
    }

    public int getIDMedicoRiferimento() {
        return IDMedicoRiferimento;
    }
    public void setIDMedicoRiferimento(int IDMedicoRiferimento) {
        this.IDMedicoRiferimento = IDMedicoRiferimento;
    }
}
