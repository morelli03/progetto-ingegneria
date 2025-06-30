package org.univr.telemedicina.model;

import java.util.List;

public class PazienteDashboard{

    private final Utente utente;
    private final List<RilevazioneGlicemia> elencoRilevazioni;
    private final List<Terapia> elencoTerapie;
    private final List<CondizioniPaziente> elencoCondizioni;
    private final List<AssunzioneFarmaci> elencoAssunzioni;

    public PazienteDashboard(Utente utente, List<RilevazioneGlicemia> elencoRilevazioni, List<Terapia> elencoTerapie, List<CondizioniPaziente> elencoCondizioni, List<AssunzioneFarmaci> elencoAssunzioni) {
        this.utente = utente;
        this.elencoRilevazioni = elencoRilevazioni;
        this.elencoTerapie = elencoTerapie;
        this.elencoCondizioni = elencoCondizioni;
        this.elencoAssunzioni = elencoAssunzioni;

    }

    // --- Metodi Getter ---

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


