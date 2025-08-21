package org.univr.telemedicina.model;

import java.util.List;

// rappresenta i dati della dashboard di un paziente
public class PazienteDashboard{

    // dati dell'utente
    private final Utente utente;
    // elenco delle rilevazioni di glicemia
    private final List<RilevazioneGlicemia> elencoRilevazioni;
    // elenco delle terapie
    private final List<Terapia> elencoTerapie;
    // elenco delle condizioni del paziente
    private final List<CondizioniPaziente> elencoCondizioni;
    // elenco delle assunzioni di farmaci
    private final List<AssunzioneFarmaci> elencoAssunzioni;

    // costruttore per creare un oggetto con parametri
    public PazienteDashboard(Utente utente, List<RilevazioneGlicemia> elencoRilevazioni, List<Terapia> elencoTerapie, List<CondizioniPaziente> elencoCondizioni, List<AssunzioneFarmaci> elencoAssunzioni) {
        this.utente = utente;
        this.elencoRilevazioni = elencoRilevazioni;
        this.elencoTerapie = elencoTerapie;
        this.elencoCondizioni = elencoCondizioni;
        this.elencoAssunzioni = elencoAssunzioni;

    }

    // metodi getter per i campi della classe

    public Utente getDatiUtente() {
        return utente;
    }

    public List<RilevazioneGlicemia> getElencoRilevazioni() {
        return elencoRilevazioni;
    }

    public List<Terapia> getElencoTerapie() {
        return elencoTerapie;
    }

    public List<CondizioniPaziente> getElencoCondizioni() {
        return elencoCondizioni;
    }

    public List<AssunzioneFarmaci> getElencoAssunzioni() {
        return elencoAssunzioni;
    }
}


