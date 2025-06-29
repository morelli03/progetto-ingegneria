package org.univr.telemedicina.service;

import org.univr.telemedicina.dao.*;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.exception.MedicoServiceException;
import org.univr.telemedicina.model.*;

import java.util.List;
import java.time.LocalDateTime;
/**
 * errori in condizioniPazienteDAO
 * 1- query SQL sbagliata nel metodo listByIDPatId
 * 2- inserimento id autoincrementato nel metodo crate
 *
 * in UtenteDAO
 * Aggiungi il Metodo findById a UtenteDAO.java
 *
 * //crea MedicoServiceException
 *
 * //dubbi su quando lanciare quale eccezione
 */


public class MedicoService {

    private final PazientiDAO pazientiDAO;
    private final RilevazioneGlicemiaDAO rivelazioneGlicemiaDAO;
    private final CondizioniPazienteDAO condizioniPazienteDAO;
    private final LogOperazioniDAO logOperazioniDAO;
    private final TerapiaDAO terapiaDAO;
    private final AssunzioneFarmaciDAO assunzioneFarmaciDAO;
    private final UtenteDAO utenteDAO;

    //costruttore
    public MedicoService(PazientiDAO pazientiDAO, RilevazioneGlicemiaDAO rivelazioneGlicemiaDAO,
                         CondizioniPazienteDAO condizioniPazienteDAO, LogOperazioniDAO logOperazioniDAO,
                         TerapiaDAO terapiaDAO, AssunzioneFarmaciDAO assunzioneFarmaciDAO, UtenteDAO utenteDAO) {
        this.pazientiDAO = new PazientiDAO();
        this.rivelazioneGlicemiaDAO = new RilevazioneGlicemiaDAO();
        this.condizioniPazienteDAO = new CondizioniPazienteDAO();
        this.logOperazioniDAO = new LogOperazioniDAO();
        this.terapiaDAO = new TerapiaDAO();
        this.assunzioneFarmaciDAO = new AssunzioneFarmaciDAO();
        this.utenteDAO = new UtenteDAO();
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
    public PazienteDashboard getDatiPazienteDasboard(int idPaziente, int idMedico) throws MedicoServiceException{
        try{
            //recupero i dati
            //da implementare metodo findById in UtenteDAO
            Paziente datiPaziente = pazientiDAO.findById(idPaziente)
                    .orElseThrow(() -> new DataAccessException("Paziente non trovato con ID: " + idPaziente, null));

            //recupera liste di informazioni dai DAO
            List<RilevazioneGlicemia> elencoRilevazioni = rivelazioneGlicemiaDAO.getRilevazioniByPaziente(idPaziente);
            List<Terapia> elencoTerapie = terapiaDAO.listTherapiesByPatId(idPaziente);
            List<CondizioniPaziente> elencoCondizioni = condizioniPazienteDAO.listByIDPatId(idPaziente);
            List<AssunzioneFarmaci> elencoAssunzioni = assunzioneFarmaciDAO.leggiAssunzioniFarmaci(idPaziente);

            // LOGGING: Traccia chi ha visualizzato i dati del paziente, come da requisiti
            //String descrizioneLog = "Visualizzazione dati dashboard del paziente ID " + idPaziente;
            //registraOperazione(idMedicoOperante, idPaziente, "VISUALIZZAZIONE_DATI", descrizioneLog);

            return new PazienteDashboard(datiPaziente, elencoRilevazioni, elencoTerapie, elencoCondizioni, elencoAssunzioni);
        }catch (DataAccessException e){
            throw new MedicoServiceException("Errore durante il recupero dei dati del paziente: " + e.getMessage(), e);
        }

    }

    /**
     * aggiunge una nuova condizione
     */

    public void addCondizioniPaziente(int idMedicoOperante, CondizioniPaziente condizione) throws MedicoServiceException {
        try {
            // Aggiorna le condizioni del paziente
            condizioniPazienteDAO.create(condizione);

            // LOGGING: Traccia l'aggiornamento delle condizioni del paziente
            String descrizioneLog = "Aggiunta condizione " + condizione.getTipo() + ":" + condizione.getDescrizione(); ;
            registraOperazione(idMedicoOperante, condizione.getIDPaziente(), "AGGIORNAMENTO_CONDIZIONI", descrizioneLog);

        } catch (DataAccessException e) {
            throw new MedicoServiceException("Errore durante l'aggiornamento delle condizioni del paziente: " + e.getMessage(), e);
        }
    }

    /*
     * aggiorna una condizione
     */
    //da aggiungere il metodo update in CondizioniPazienteDAO

    public void updateCondizioniPaziente(int idMedicoOperante, CondizioniPaziente condizione) throws MedicoServiceException {
        try {
            // Aggiorna le condizioni del paziente

            condizioniPazienteDAO.update(condizione);

            // LOGGING: Traccia l'aggiornamento delle condizioni del paziente
            String descrizioneLog = "Aggiornata condizione ID "+ condizione.getIDCondizione() + " con descrizione " + condizione.getDescrizione();
            registraOperazione(idMedicoOperante, condizione.getIDPaziente(), "AGGIORNAMENTO_CONDIZIONI", descrizioneLog);

        } catch (DataAccessException e) {
            throw new MedicoServiceException("Errore durante l'aggiornamento delle condizioni del paziente: " + e.getMessage(), e);
        }
    }

    /**
     * registra un'operazione effettuata da un medico su un paziente
     */
    public void registraOperazione(int idMedico, int idPaziente, String tipoOperazione, String descrizione) throws DataAccessException {

        LogOperazione log = new LogOperazione();
        log.setIDMedicoOperante(idMedico);
        log.setIDPazienteInteressato(idPaziente);
        log.setTipoOperazione(tipoOperazione);
        log.setDescrizioneOperazione(descrizione);
        log.setTimestamp(LocalDateTime.now());

        logOperazioniDAO.createLog(log);;


    }


}
