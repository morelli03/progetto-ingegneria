package org.univr.telemedicina.service;

import org.univr.telemedicina.dao.AssunzioneFarmaciDAO;
import org.univr.telemedicina.dao.TerapiaDAO;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.AssunzioneFarmaci;

import java.util.List;

/**
 * Classe che gestisce le notifiche dei pazienti e dei medici.
 * Gestisce: livelli di glicemia anormale, verificare la coerenza tra assunzioni di farmaci e terapia prescritta,
 * puntualità di assunzione dei farmaci.
 * Deve inviare al paziente una notifica quando non ha registrato una assunzione di farmaci dopo 2 ore rispetto
 * all'ora prevista.
 * Sarà un service che per i check dei farmaci runna ogni minuto o due, per i livelli di glicemia è in real time.
 * Penso di fare tipo 3 o 4 medoti, per esempio quando uno registra una misurazione subito dopo che è stata scritta in
 * db viene chiamato sto metodo che controlla se ci sono anomalie e se si invia una notifica al medico.
 *
 */
public class MonitorService {

    // =========== Task per il Controllo dell'aderenza alla Terapia ====================================================

    //non serve mettere il controllo del farmaco prescritto, perché l'utente può selezionare solo i farmaci prescritti.

    /**
     * Controlla se il paziente ha registrato un'assunzione di farmaci dopo 2 ore rispetto dall'ora prevista.
     * Se non lo ha fatto, invia una notifica al paziente.
     */
    public void checkFarmaciDaily(){
        //prende la lista di tutti i pazienti che hanno una terapia attiva
        TerapiaDAO terapiaDAO = new TerapiaDAO();
        List<Integer> pazientiAttivi;
        try {
            pazientiAttivi = terapiaDAO.getActivePatientIds();
        } catch (DataAccessException e) {
            System.err.println("Errore durante il recupero dei pazienti attivi: " + e.getMessage());
            // qui devo lanciare un'eccezione personalizzata
            return; // ritorna se c'è un errore nel database
        }

        //crea un oggetto AssunzioneFarmaciDAO per leggere le assunzioni di farmaci
        AssunzioneFarmaciDAO assunzioneFarmaciDAO = new AssunzioneFarmaciDAO();

        //scorre ogni paziente attivo e verifica se ha registrato un'assunzione di farmaci entro le 2 ore
        for (Integer idPaziente : pazientiAttivi) {
            //leggi assunzioni di quel paziente di oggi
            List<AssunzioneFarmaci> assunzioni;
            try {
                assunzioni = assunzioneFarmaciDAO.leggiAssunzioniGiorno(idPaziente, java.time.LocalDate.now());
            } catch (DataAccessException e) {
                throw new RuntimeException(e);
            }

            //non ha ancora registrato nulla oggi, o errore del db - DA RISOLVERE FACCIO ORA
            if(assunzioni.isEmpty()){

            }
        }
    }

}
