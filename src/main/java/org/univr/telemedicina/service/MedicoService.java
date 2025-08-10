package org.univr.telemedicina.service;

import org.univr.telemedicina.dao.*;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.exception.MedicoServiceException;
import org.univr.telemedicina.model.*;

import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * Service layer che implementa tutte le funzionalità a disposizione del medico.
 * Permette di visualizzare le liste pazienti, i dati aggregati (dashboard) e aggiornare le condizioni cliniche.
 * */

public class MedicoService {

    private final PazientiDAO pazientiDAO;
    private final RilevazioneGlicemiaDAO rivelazioneGlicemiaDAO;
    private final CondizioniPazienteDAO condizioniPazienteDAO;
    private final LogOperazioniDAO logOperazioniDAO;
    private final TerapiaDAO terapiaDAO;
    private final AssunzioneFarmaciDAO assunzioneFarmaciDAO;

    //costruttore
    public MedicoService(PazientiDAO pazientiDAO, RilevazioneGlicemiaDAO rivelazioneGlicemiaDAO, CondizioniPazienteDAO condizioniPazienteDAO, LogOperazioniDAO logOperazioniDAO, TerapiaDAO terapiaDAO, AssunzioneFarmaciDAO assunzioneFarmaciDAO) {
        this.pazientiDAO = pazientiDAO;
        this.rivelazioneGlicemiaDAO = rivelazioneGlicemiaDAO;
        this.condizioniPazienteDAO = condizioniPazienteDAO;
        this.logOperazioniDAO = logOperazioniDAO;
        this.terapiaDAO = terapiaDAO;
        this.assunzioneFarmaciDAO = assunzioneFarmaciDAO;
    }

    /**
     * elenco dei pazienti assegnati al medico
     */
    public List<Utente> getPazientiAssegnati(int idMedico) throws MedicoServiceException {
        try {
            // recupera i pazienti assegnati al medico
            return pazientiDAO.findPazientiByMedId(idMedico);
        } catch (DataAccessException e) {
            throw new MedicoServiceException("Errore durante il recupero dei pazienti assegnati: " + e.getMessage(), e);
        }
    }

    /**
     * restituisce una lista di pazienti attivi, ovvero chi ha una terapia in corso
     */
    public List<Utente> getPazientiAttivi(int idMedico) throws MedicoServiceException {
        try {
            List<Utente> pazientiAssegnati = getPazientiAssegnati(idMedico);

            // Filtra i pazienti che hanno almeno una terapia attiva
            List<Integer> pazientiAttiviGlobal = terapiaDAO.getActivePatientIds();
            return pazientiAssegnati.stream()
                    .filter(paziente -> pazientiAttiviGlobal.contains(paziente.getIDUtente()))
                    .toList();

        } catch (DataAccessException e) {
            throw new MedicoServiceException("Errore durante il recupero dei pazienti attivi: " + e.getMessage(), e);
        }
    }

    /**
     * raccoglie dati necessari per la dashboard del singolo paziente
     */
    public PazienteDashboard getDatiPazienteDasboard(Utente utente) throws MedicoServiceException{
        try{

            //recupera liste di informazioni dai DAO
            List<RilevazioneGlicemia> elencoRilevazioni = rivelazioneGlicemiaDAO.getRilevazioniByPaziente(utente.getIDUtente());
            List<Terapia> elencoTerapie = terapiaDAO.listTherapiesByPatId(utente.getIDUtente());
            List<CondizioniPaziente> elencoCondizioni = condizioniPazienteDAO.listByIDPatId(utente.getIDUtente());
            List<AssunzioneFarmaci> elencoAssunzioni = assunzioneFarmaciDAO.leggiAssunzioniFarmaci(utente.getIDUtente());

            return new PazienteDashboard(utente, elencoRilevazioni, elencoTerapie, elencoCondizioni, elencoAssunzioni);
        }catch (DataAccessException e){
            throw new MedicoServiceException("Errore durante il recupero dei dati del paziente: " + e.getMessage(), e);
        }

    }

    /**
     * aggiunge una nuova condizione
     */

    public void addCondizioniPaziente(int idMedicoOperante, int IDPaziente, String tipo, String descrizione, String periodo, LocalDate dataRegistrazione) throws MedicoServiceException {

        // validazione dell'input
        if (!("patologia".equalsIgnoreCase(tipo) || "fattoriRischio".equalsIgnoreCase(tipo) || "comorbidita".equalsIgnoreCase(tipo))) {
            throw new MedicoServiceException("Tipo di condizione non valido. Deve essere 'Patologia' o 'FattoreRischio' o 'Comorbidita'.");
        }

        try {

            CondizioniPaziente condizione = new CondizioniPaziente(IDPaziente, tipo, descrizione, periodo, dataRegistrazione);

            condizioniPazienteDAO.create(condizione);

            // LOGGING: Traccia l'aggiornamento delle condizioni del paziente
            String descrizioneLog = "Aggiunta condizione " + condizione.getTipo() + ":" + condizione.getDescrizione();
            logOperazione(idMedicoOperante, condizione.getIDPaziente(), "AGGIORNAMENTO_CONDIZIONI", descrizioneLog);

        } catch (DataAccessException e) {
            throw new MedicoServiceException("Errore durante l'aggiornamento delle condizioni del paziente: " + e.getMessage(), e);
        }
    }

    /**
     * Aggiorna una condizione esistente di un paziente.
     * @param idMedicoOperante L'ID del medico che esegue l'operazione.
     * @param condizione L'oggetto CondizioniPaziente da aggiornare.
     *                   DEVE contenere l'IDCondizione della riga da modificare.
     * @throws MedicoServiceException se l'aggiornamento fallisce o i dati non sono validi.
     */
    public void updateCondizioniPaziente(int idMedicoOperante, CondizioniPaziente condizione) throws MedicoServiceException {

        // 1. Validazione dell'input
        if (condizione == null || condizione.getIDCondizione() <= 0) {
            throw new MedicoServiceException("ID della condizione non valido per l'aggiornamento.");
        }

        if (!("patologia".equalsIgnoreCase(condizione.getTipo()) || "fattoriRischio".equalsIgnoreCase(condizione.getTipo()) || "comorbidita".equalsIgnoreCase(condizione.getTipo()))) {
            throw new MedicoServiceException("Tipo di condizione non valido. Deve essere 'Patologia' o 'FattoreRischio' o 'Comorbidita'.");
        }

        try {
            // 2. Chiamata al DAO con l'oggetto completo
            // Il DAO ora ha tutte le informazioni necessarie, incluso l'ID per la clausola WHERE.
            condizioniPazienteDAO.update(condizione);

            // 3. Logging corretto, perché ora l'ID è presente
            String descrizioneLog = "Aggiornata condizione ID " + condizione.getIDCondizione() + " con descrizione: " + condizione.getDescrizione();
            logOperazione(idMedicoOperante, condizione.getIDPaziente(), "AGGIORNAMENTO_CONDIZIONI", descrizioneLog);

        } catch (DataAccessException e) {
            // Fornisce un messaggio di errore più specifico
            throw new MedicoServiceException("Errore durante l'aggiornamento della condizione ID " + condizione.getIDCondizione(), e);
        }
    }

    /**
     * registra un'operazione effettuata da un medico su un paziente
     */
    public void logOperazione(int idMedico, int idPaziente, String tipoOperazione, String descrizione) throws DataAccessException {

        LogOperazione log = new LogOperazione(idMedico, idPaziente, tipoOperazione, descrizione, LocalDateTime.now());

        logOperazioniDAO.createLog(log);
    }


    /**
     * Calcola l'indice di aderenza globale dei pazienti assegnati al medico.
     * L'aderenza è calcolata come la media delle aderenze individuali dei pazienti.
     *
     * @param pazienti Lista di pazienti da considerare per il calcolo dell'aderenza.
     * @return L'indice di aderenza globale come valore compreso tra 0 e 1.
     * @throws MedicoServiceException se si verifica un errore durante il calcolo.
     */
    public double calcolaAderenzaGlobale(List<Utente> pazienti) throws MedicoServiceException {
        double aderenzaMedia = 0;
        int pazientiConTerapia = 0;

        for (Utente paziente : pazienti) {
            try {
                List<Terapia> terapie = terapiaDAO.listTherapiesByPatId(paziente.getIDUtente());
                if (terapie.isEmpty()) {
                    continue;
                }

                pazientiConTerapia++;
                double aderenzaPaziente = calcolaAderenzaPaziente(paziente.getIDUtente(), terapie);
                aderenzaMedia += aderenzaPaziente;
            } catch (DataAccessException e) {
                throw new MedicoServiceException("Errore nel calcolo dell'aderenza per il paziente " + paziente.getIDUtente(), e);
            }
        }

        if (pazientiConTerapia > 0) {
            return aderenzaMedia / pazientiConTerapia;
        } else {
            return 0.0;
        }
    }

    private double calcolaAderenzaPaziente(int idPaziente, List<Terapia> terapie) throws DataAccessException {
        int dosiTotaliPrescritte = 0;
        int dosiTotaliAssunte = 0;

        for (Terapia terapia : terapie) {
            LocalDate fineTerapia = (terapia.getDataFine() != null && terapia.getDataFine().isBefore(LocalDate.now())) ? terapia.getDataFine() : LocalDate.now();
            long giorniTerapia = java.time.temporal.ChronoUnit.DAYS.between(terapia.getDataInizio(), fineTerapia) + 1;
            dosiTotaliPrescritte += (int) (terapia.getFrequenzaGiornaliera() * giorniTerapia);
        }

        dosiTotaliAssunte = assunzioneFarmaciDAO.leggiAssunzioniFarmaci(idPaziente).size();

        if (dosiTotaliPrescritte > 0) {
            return (double) dosiTotaliAssunte / dosiTotaliPrescritte;
        } else {
            return 0.0;
        }
    }
}
