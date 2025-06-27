package org.univr.telemedicina.service;

import org.univr.telemedicina.dao.AssunzioneFarmaciDAO;
import org.univr.telemedicina.dao.TerapiaDAO;
import org.univr.telemedicina.exception.DataAccessException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Classe che gestisce le notifiche dei pazienti e dei medici.
 * Gestisce: livelli di glicemia anormale, puntualità di assunzione dei farmaci.
 */
public class MonitorService {

    private final TerapiaDAO terapiaDAO;
    private final AssunzioneFarmaciDAO assunzioneFarmaciDAO;
    /**
     * Costruttore del servizio. Inizializza i DAO necessari.
     */
    public MonitorService() {
        this.terapiaDAO = new TerapiaDAO();
        this.assunzioneFarmaciDAO = new AssunzioneFarmaciDAO();
    }


    /**
     * Controlla se il paziente ha registrato un'assunzione di farmaci oggi.
     * Se è tardi e non lo ha fatto, invia una notifica al paziente.
     */
    public void checkFarmaciDaily() throws DataAccessException {
        // prende gli ID di tutti i pazienti con una terapia in corso
        List<Integer> pazientiAttivi = terapiaDAO.getActivePatientIds();

        // se non ci sono pazienti attivi esce
        if (pazientiAttivi.isEmpty()) {
            System.out.println("Nessun paziente con terapie attive trovato. Controllo terminato.");
            return;
        }

        // prende le frequenze totali richieste per tutti i pazienti
        Map<Integer, Integer> frequenzeRichieste = terapiaDAO.getFrequenzeGiornalierePerPazienti(pazientiAttivi);

        // prende il conteggio delle assunzioni di oggi per tutti i pazienti in un colpo solo
        Map<Integer, Integer> assunzioniEffettuate = assunzioneFarmaciDAO.getConteggioAssunzioniGiornoPerPazienti(pazientiAttivi, LocalDate.now());

        int oraAttuale = LocalDateTime.now().getHour();

        // per ogni paziente attivo, controlla se ha registrato le assunzioni di farmaci
        for (Integer idPaziente : pazientiAttivi) {

            // uso getOrDefault per gestire in sicurezza i pazienti.
            int frequenzaRichiesta = frequenzeRichieste.getOrDefault(idPaziente, 0);
            int numeroAssunzioniOggi = assunzioniEffettuate.getOrDefault(idPaziente, 0);

            // se la frequenza richiesta è 0 non c'è nulla da controllare per questo paziente
            if (frequenzaRichiesta == 0) {
                continue;
            }

            // se il numero di assunzioni effettuate oggi finora è inferiore alla frequenza richiesta
            if (numeroAssunzioniOggi < frequenzaRichiesta) {

                // se non ha ancora registrato nulla ed è pomeriggio
                if (numeroAssunzioniOggi == 0 && oraAttuale >= 12) {
                    // Sostituzione della notifica con un output su console
                    System.out.println("NOTIFICA a Paziente ID " + idPaziente + ": Ricorda di registrare le assunzioni dei farmaci per oggi.");
                }
                //se ha registrato qualcosa ma non tutto, ed è già sera
                else if (numeroAssunzioniOggi > 0 && oraAttuale >= 18) {
                    // Sostituzione della notifica con un output su console
                    System.out.println("NOTIFICA a Paziente ID " + idPaziente + ": Hai registrato " + numeroAssunzioniOggi + " su " + frequenzaRichiesta + " assunzioni richieste. Ricorda di completare la terapia.");
                }
            }
        }
    }


}