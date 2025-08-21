package org.univr.telemedicina.service;

import org.univr.telemedicina.dao.AssunzioneFarmaciDAO;
import org.univr.telemedicina.dao.PazientiDAO;
import org.univr.telemedicina.dao.TerapiaDAO;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.RilevazioneGlicemia;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// classe che gestisce le notifiche dei pazienti e dei medici
// gestisce livelli di glicemia anormale puntualità di assunzione dei farmaci
public class MonitorService {

    private final TerapiaDAO terapiaDAO;
    private final AssunzioneFarmaciDAO assunzioneFarmaciDAO;
    private final NotificheService notificheService;
    private final PazientiDAO pazientiDAO;

    // costruttore del servizio inizializza i dao necessari
    public MonitorService(TerapiaDAO terapiaDAO, AssunzioneFarmaciDAO assunzioneFarmaciDAO, NotificheService notificheService, PazientiDAO pazientiDAO) {
        this.terapiaDAO = terapiaDAO;
        this.assunzioneFarmaciDAO = assunzioneFarmaciDAO;
        this.notificheService = notificheService;
        this.pazientiDAO = pazientiDAO;
    }


    // controlla se il paziente ha registrato un'assunzione di farmaci oggi
    // se è tardi e non lo ha fatto invia una notifica al paziente
    // @throws dataaccessexception se si verifica un errore durante l'accesso ai dati
    public void checkFarmaciDaily() throws DataAccessException {


        // prende gli id di tutti i pazienti con una terapia in corso
        List<Integer> pazientiAttivi = terapiaDAO.getActivePatientIds();

        // se non ci sono pazienti attivi esce
        if (pazientiAttivi.isEmpty()) {
            System.out.println("nessun paziente con terapie attive trovato controllo terminato");
            return;
        }

        // prende le frequenze totali richieste per tutti i pazienti
        Map<Integer, Integer> frequenzeRichieste = terapiaDAO.getFrequenzeGiornalierePerPazienti(pazientiAttivi);

        // prende il conteggio delle assunzioni di oggi per tutti i pazienti in un colpo solo
        Map<Integer, Integer> assunzioniEffettuate = assunzioneFarmaciDAO.getConteggioAssunzioniGiornoPerPazienti(pazientiAttivi, LocalDate.now());

        int oraAttuale = LocalDateTime.now().getHour();

        // per ogni paziente attivo controlla se ha registrato le assunzioni di farmaci
        for (Integer idPaziente : pazientiAttivi) {

            // uso getordefault per gestire in sicurezza i pazienti
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
                    // sostituzione della notifica con un output su console
                    //system.out.println("notifica a paziente id " + idpaziente + " ricorda di registrare le assunzioni dei farmaci per oggi");
                    notificheService.send(idPaziente, 1, "assunzioni farmaci incompleta", "hai dimenticato di registrare le assunzioni dei farmaci per oggi", "assunzioni farmaci");
                }
                //se ha registrato qualcosa ma non tutto ed è già sera
                else if (numeroAssunzioniOggi > 0 && oraAttuale >= 18) {
                    // sostituzione della notifica con un output su console
                    //system.out.println("notifica a paziente id " + idpaziente + " hai registrato " + numeroassunzionoggi + " su " + frequenzarichiesta + " assunzioni richieste ricorda di completare la terapia");
                    notificheService.send(idPaziente, 1, "assunzioni farmaci incompleta", "hai registrato " + numeroAssunzioniOggi + " su " + frequenzaRichiesta + " assunzioni richieste ricorda di completare la terapia", "assunzioni farmaci");
                }
            }
        }
    }

    // controlla se un paziente non ha registrato le assunzioni di farmaci per tre giorni consecutivi
    // questo metodo deve essere runnato la sera verso le 18:00
    // @throws dataaccessexception se si verifica un errore durante l'accesso ai dati
    public void checkFarmaci3Daily() throws DataAccessException {

        // prende gli id di tutti i pazienti con una terapia in corso
        List<Integer> pazientiAttivi = terapiaDAO.getActivePatientIds();

        // se non ci sono pazienti attivi esce
        if (pazientiAttivi.isEmpty()) {
            System.out.println("nessun paziente con terapie attive trovato controllo terminato");
            return;
        }
        // motliplico per 3 perche pèer tre giorni
        Map<Integer, Integer> frequenzeRichieste = terapiaDAO.getFrequenzeGiornalierePerPazienti(pazientiAttivi);


        for (Integer idPaziente : pazientiAttivi) {
            int numeroAssunzioni = 0;
            int frequenzaRichiesta3Giorni = frequenzeRichieste.getOrDefault(idPaziente, 0) * 3;

            // somma tutte le assunzioni degli ultimi 3 giorni
            for(int i =0; i < 3; i++) {
                //prende dalla mappa il conteggio per il paziente
                numeroAssunzioni += assunzioneFarmaciDAO.getConteggioAssunzioniGiornoPerPazienti(List.of(idPaziente), LocalDate.now().minusDays(i)).getOrDefault(idPaziente, 0);
            }

            // se il numero di assunzioni effettuate negli ultimi 3 giorni è inferiore alla frequenza richiesta
            if(numeroAssunzioni < frequenzaRichiesta3Giorni){
                //system.out.println("notifica a paziente id " + idpaziente + " non hai registrato le assunzioni dei farmaci per tre giorni consecutivi controlla la tua terapia");
                notificheService.send(pazientiDAO.getMedicoRiferimentoByPazienteId(idPaziente).orElseThrow(), 2, "mancata aderenza alla terapia", "l'utente " + pazientiDAO.findNameById(idPaziente) + " non ha seguito la terapia per più di 3 giorni", "assunzioni farmaci");
            }
        }


    }

    // controlla il valore della glicemia registrato da un paziente
    // se il valore è anormale invia una notifica al paziente
    // @param rilevazione l'oggetto rilevazioneglicemia contenente i dati della rilevazione
    public void checkGlicemia(RilevazioneGlicemia rilevazione) throws DataAccessException {
        if(rilevazione.getNote().equals("Prima colazione") || rilevazione.getNote().equals("Prima pranzo") || rilevazione.getNote().equals("Prima cena")){
            //entra se è prima dei pasti
            if(rilevazione.getValore() <= 80 || rilevazione.getValore() >= 130){
                //system.out.println("notifica a paziente id " + rilevazione.getidpaziente() + " valore glicemico anormale prima dei pasti " + rilevazione.getvalore() + " mg/dl controlla la tua dieta");
                notificheService.send(pazientiDAO.getMedicoRiferimentoByPazienteId(rilevazione.getIdPaziente()).orElseThrow(), 3, "glicemia anormale", "il paziente " + pazientiDAO.findNameById(rilevazione.getIdPaziente()) + " ha registrato un valore glicemico anormale prima dei pasti " + rilevazione.getValore() + " mg/dl", "glicemia");
            }
        } else {
            //entra se è due ore dopo i pasti
            if(rilevazione.getValore() >= 180){
                //system.out.println("notifica a paziente id " + rilevazione.getidpaziente() + " valore glicemico anormale dopo i pasti " + rilevazione.getvalore() + " mg/dl controlla la tua dieta");
                notificheService.send(pazientiDAO.getMedicoRiferimentoByPazienteId(rilevazione.getIdPaziente()).orElseThrow(), 3, "glicemia anormale", "il paziente " + pazientiDAO.findNameById(rilevazione.getIdPaziente()) + " ha registrato un valore glicemico anormale dopo i pasti " + rilevazione.getValore() + " mg/dl", "glicemia");
            }
        }

    }
}