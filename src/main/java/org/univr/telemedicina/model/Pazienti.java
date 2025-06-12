package org.univr.telemedicina.model;

public class Pazienti {
    private int IDPaziente;
    private int IDMedicoRiferimento;

    public Pazienti(){} //costruttore vuoto

    public Pazienti(int IDPaziente, int IDMedicoRiferimento) {
        this.IDPaziente = IDPaziente;
        this.IDMedicoRiferimento = IDMedicoRiferimento;
    }

    //metodi getter e setter
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
