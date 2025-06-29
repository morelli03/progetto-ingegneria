package org.univr.telemedicina.service;

import org.univr.telemedicina.dao.AssunzioneFarmaciDAO;
import org.univr.telemedicina.dao.TerapiaDAO;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.RilevazioneGlicemia;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
     * @throws DataAccessException Se si verifica un errore durante l'accesso ai dati.
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

    /**
     * Controlla se un paziente non ha registrato le assunzioni di farmaci per tre giorni consecutivi.
     * Questo metodo DEVE ESSERE runnato la sera, verso le 18:00.
     * @throws DataAccessException Se si verifica un errore durante l'accesso ai dati.
     */
    public void checkFarmaci3Daily() throws DataAccessException {
        // prende gli ID di tutti i pazienti con una terapia in corso
        List<Integer> pazientiAttivi = terapiaDAO.getActivePatientIds();

        // se non ci sono pazienti attivi esce
        if (pazientiAttivi.isEmpty()) {
            System.out.println("Nessun paziente con terapie attive trovato. Controllo terminato.");
            return;
        }
                                            // motliplico per 3 perche pèer tre giorni
        Map<Integer, Integer> frequenzeRichieste = terapiaDAO.getFrequenzeGiornalierePerPazienti(pazientiAttivi);
        int numeroAssunzioni = 0;

        for (Integer idPaziente : pazientiAttivi) {
            int frequenzaRichiesta3Giorni = frequenzeRichieste.getOrDefault(idPaziente, 0) * 3;

            // somma tutte le assunzioni degli ultimi 3 giorni
            for(int i =0; i < 3; i++) {
                                                                                                                                            //prende dalla mappa il conteggio per il paziente
                numeroAssunzioni += assunzioneFarmaciDAO.getConteggioAssunzioniGiornoPerPazienti(List.of(idPaziente), LocalDate.now().minusDays(i)).getOrDefault(idPaziente, 0);
            }

            if(numeroAssunzioni < frequenzaRichiesta3Giorni){
                System.out.println("NOTIFICA a Paziente ID " + idPaziente + ": Non hai registrato le assunzioni dei farmaci per tre giorni consecutivi. Controlla la tua terapia.");
            }
        }


    }

    /**
     * Controlla il valore della glicemia registrato da un paziente.
     * Se il valore è anormale, invia una notifica al paziente.
     * @param rilevazione L'oggetto RilevazioneGlicemia contenente i dati della rilevazione.
     */
    public void checkGlicemia(RilevazioneGlicemia rilevazione){

        LocalTime oraRilevazione = rilevazione.getTimestamp().toLocalTime();

        //check due ore dopo i pasti
        if( oraRilevazione.isAfter(LocalTime.of(8, 0)) && oraRilevazione.isBefore(LocalTime.of(11, 0)) ||
                oraRilevazione.isAfter(LocalTime.of(12, 30)) && oraRilevazione.isBefore(LocalTime.of(14, 0)) ||
                oraRilevazione.isAfter(LocalTime.of(19, 30)) && oraRilevazione.isBefore(LocalTime.of(21, 0))) {
            //entra se è due ore dopo i pasti
            if(rilevazione.getValore() <= 100 || rilevazione.getValore() >= 180){
                System.out.println("NOTIFICA a Paziente ID " + rilevazione.getIdPaziente() + ": Valore glicemico anormale dopo i pasti: " + rilevazione.getValore() + " mg/dL. Controlla la tua dieta.");
            }
        } else {
            //entra se è prima dei pasti
            if(rilevazione.getValore() <= 80 || rilevazione.getValore() >= 130){
                System.out.println("NOTIFICA a Paziente ID " + rilevazione.getIdPaziente() + ": Valore glicemico anormale prima dei pasti: " + rilevazione.getValore() + " mg/dL. Controlla la tua dieta.");
            }
        }

    }
}