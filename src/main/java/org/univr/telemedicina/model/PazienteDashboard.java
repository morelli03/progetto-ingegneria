package org.univr.telemedicina.model;
import org.univr.telemedicina.model.CondizioniPaziente;
import org.univr.telemedicina.model.RilevazioneGlicemia;
import org.univr.telemedicina.model.Terapia;
import org.univr.telemedicina.model.Utente;

import java.util.List;

public class PazienteDashboard{

    private final Paziente datiPaziente;
    private final List<RilevazioneGlicemia> elencoRilevazioni;
    private final List<Terapia> elencoTerapie;
    private final List<CondizioniPaziente> elencoCondizioni;
    private final List<AssunzioneFarmaci> elencoAssunzioni;

    public PazienteDashboard(Paziente datiPaziente, List<RilevazioneGlicemia> elencoRilevazioni, List<Terapia> elencoTerapie, List<CondizioniPaziente> elencoCondizioni, List<AssunzioneFarmaci> elencoAssunzioni) {
        this.datiPaziente = datiPaziente;
        this.elencoRilevazioni = elencoRilevazioni;
        this.elencoTerapie = elencoTerapie;
        this.elencoCondizioni = elencoCondizioni;
        this.elencoAssunzioni = elencoAssunzioni;

    }

    // --- Metodi Getter ---

    public Paziente getDatiPaziente() {
        return datiPaziente;
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


