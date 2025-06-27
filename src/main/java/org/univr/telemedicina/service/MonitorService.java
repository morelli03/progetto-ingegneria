package org.univr.telemedicina.service;

import org.univr.telemedicina.dao.AssunzioneFarmaciDAO;
import org.univr.telemedicina.dao.TerapiaDAO;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.AssunzioneFarmaci;
import org.univr.telemedicina.model.Terapia;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe che gestisce le notifiche dei pazienti e dei medici.
 * Gestisce: livelli di glicemia anormale, puntualità della registrazione di assunzione dei farmaci.
 * Sarà un service che per i check dei farmaci runna ogni minuto o due, per i livelli di glicemia è in real time.
 * Penso di fare tipo 3 o 4 medoti, per esempio quando uno registra una misurazione subito dopo che è stata scritta in
 * db viene chiamato sto metodo che controlla se ci sono anomalie e se si invia una notifica al medico.
 *
 */
public class MonitorService {

    // =========== Task per il Controllo dell'aderenza alla Terapia ====================================================

    //non serve mettere il controllo del farmaco prescritto, perché l'utente può selezionare solo i farmaci prescritti.

    /**
     * Controlla se il paziente ha registrato un'assunzione di farmaci oggi.
     * Se è tardi e non lo ha fatto, invia una notifica al paziente.
     */
    @SuppressWarnings("t")
    public void checkFarmaciDaily(){
        //calcolo l'ora attuale
        int oraAttuale = LocalDateTime.now().getHour();

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
                System.err.println("Errore durante la lettura delle assunzioni di farmaci per il paziente con ID " + idPaziente + ": " + e.getMessage());
                // qui devo lanciare un'eccezione personalizzata ?
                continue; // salta al prossimo paziente se c'è un errore nel database
            }


            //non ha ancora registrato nulla oggi ed è pomeriggio
            if(assunzioni.isEmpty() && oraAttuale >= 15) {
                //qui invia una notifica al paziente
                System.out.println("Paziente con ID " + idPaziente + " non ha registrato assunzioni di farmaci oggi. Invia notifica.");
                // per implementare le notifiche faremo un nuovo service, creeremo una tabella notifiche (guarda G per capire) con una colonna bool (new),
                // poi sul codice dell'interfaccia grafica ogni 30 secondi facciamo una query per leggere le notifiche con new=true.
            }
            else{ // ha registrato delle assunzioni di farmaci oggi
                // se il numero di assunzioni è minore del numero di frequenza giornaliera della terapia e è dopo le 12, invia una notifica

                int numeroAssunzioniOggi = assunzioni.size();
                int frequenzaGiornaliera = 0;

                List<Terapia> terapiePaziente = new ArrayList<>();
                try {
                    terapiePaziente = terapiaDAO.listTherapiesByPatId(idPaziente);
                } catch (DataAccessException e) {
                    System.err.println("Errore durante la lettura delle terapie per il paziente con ID " + idPaziente + ": " + e.getMessage());
                }

                // non serve controllare se terapie paziente è vuota, perché il paziente deve avere almeno una terapia attiva per essere in questa lista
                // scorre le terapie del paziente e somma le frequenze giornaliere
                for (Terapia terapia : terapiePaziente){
                    frequenzaGiornaliera = frequenzaGiornaliera + terapia.getFrequenzaGiornaliera();
                }

                //controllo se il numero di assunzioni è minore della frequenza giornaliera
                if(numeroAssunzioniOggi < frequenzaGiornaliera && LocalDateTime.now().getHour() >= 12){
                    //qui invia una notifica al paziente
                    System.out.println("Paziente con ID " + idPaziente + " ha registrato " + numeroAssunzioniOggi + " assunzioni di farmaci oggi, ma la frequenza giornaliera è " + frequenzaGiornaliera + ". Invia notifica.");
                    // per implementare le notifiche faremo un nuovo service, creeremo una tabella notifiche (guarda G per capire) con una colonna bool (new),
                    // poi sul codice dell'interfaccia grafica ogni 30 secondi facciamo una query per leggere le notifiche con new=true.
                }

            }

        }
    }

}
