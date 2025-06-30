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
    public MedicoService() {
        this.pazientiDAO = new PazientiDAO();
        this.rivelazioneGlicemiaDAO = new RilevazioneGlicemiaDAO();
        this.condizioniPazienteDAO = new CondizioniPazienteDAO();
        this.logOperazioniDAO = new LogOperazioniDAO();
        this.terapiaDAO = new TerapiaDAO();
        this.assunzioneFarmaciDAO = new AssunzioneFarmaciDAO();
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

        if(!("anamnestiche".equalsIgnoreCase(tipo) || "fattoriRischio".equalsIgnoreCase(tipo))){
            throw new MedicoServiceException("Tipo di condizione non valido. Deve essere 'anamnestiche' o 'fattoriRischio'.");
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
     * aggiorna una condizione
     */
    //da aggiungere il metodo update in CondizioniPazienteDAO

    public void updateCondizioniPaziente(int idMedicoOperante, int IDPaziente, String tipo, String descrizione, String periodo, LocalDate dataRegistrazione ) throws MedicoServiceException {

        if(!("anamnestiche".equalsIgnoreCase(tipo) || "fattoriRischio".equalsIgnoreCase(tipo))){
            throw new MedicoServiceException("Tipo di condizione non valido. Deve essere 'anamnestiche' o 'fattoriRischio'.");
        }

        try {
            // Aggiorna le condizioni del paziente
            CondizioniPaziente condizione = new CondizioniPaziente (IDPaziente, tipo, descrizione, periodo, dataRegistrazione);


            condizioniPazienteDAO.update(condizione);

            // LOGGING: Traccia l'aggiornamento delle condizioni del paziente
            String descrizioneLog = "Aggiornata condizione ID "+ condizione.getIDCondizione() + " con descrizione " + condizione.getDescrizione();
            logOperazione(idMedicoOperante, condizione.getIDPaziente(), "AGGIORNAMENTO_CONDIZIONI", descrizioneLog);

        } catch (DataAccessException e) {
            throw new MedicoServiceException("Errore durante l'aggiornamento delle condizioni del paziente: " + e.getMessage(), e);
        }
    }

    /**
     * registra un'operazione effettuata da un medico su un paziente
     */
    public void logOperazione(int idMedico, int idPaziente, String tipoOperazione, String descrizione) throws DataAccessException {

        LogOperazione log = new LogOperazione(idMedico, idPaziente, tipoOperazione, descrizione, LocalDateTime.now());

        logOperazioniDAO.createLog(log);
    }


}
