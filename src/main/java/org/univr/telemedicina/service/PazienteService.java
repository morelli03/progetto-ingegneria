package org.univr.telemedicina.service;

import org.univr.telemedicina.dao.*;

import org.univr.telemedicina.exception.MedicoServiceException;
import org.univr.telemedicina.model.*;

import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.exception.WrongAssumptionException;

import java.sql.SQLException;
import java.time.LocalDateTime;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PazienteService {
    
    private final RilevazioneGlicemiaDAO rilevazioneDAO;
    private final MonitorService monitorService;
    private final CondizioniPazienteDAO condizioniDAO;
    private final UtenteDAO utenteDAO;
    private final PazientiDAO pazienteDAO;
    private final AssunzioneFarmaciDAO assunzioneFarmaciDAO;
    private final TerapiaDAO teratpiaDAO;
    private final NotificheService notificheService;


    public PazienteService(RilevazioneGlicemiaDAO rilevazioneDAO, MonitorService monitorService, CondizioniPazienteDAO condizioniDAO, UtenteDAO utenteDAO, PazientiDAO pazienteDAO, AssunzioneFarmaciDAO assunzioneFarmaciDAO, TerapiaDAO terapiaDAO, NotificheService notificheService) {
        this.rilevazioneDAO = rilevazioneDAO;
        this.monitorService = monitorService;
        this.condizioniDAO = condizioniDAO;
        this.utenteDAO = utenteDAO;
        this.pazienteDAO = pazienteDAO;
        this.assunzioneFarmaciDAO = assunzioneFarmaciDAO;
        this.teratpiaDAO = terapiaDAO;
        this.notificheService = notificheService;
    }

    /**
     * Registra una nuova rilevazione di glicemia per un paziente.
     *
     * @param idPaziente ID del paziente per cui si sta registrando la rilevazione
     * @param valore Valore della glicemia rilevata
     * @param timestamp Data e ora della rilevazione
     * @param note Eventuali note aggiuntive sulla rilevazione
     */
    public void registraRilevazioneGlicemia(int idPaziente, int valore, LocalDateTime timestamp, String note) throws DataAccessException {

        // Crea un'istanza di RilevazioneGlicemia con i dati forniti
        RilevazioneGlicemia rilevazione = new RilevazioneGlicemia(idPaziente, valore, timestamp, note);

        // Prova a salvare la rilevazione nel database
        try {
            rilevazioneDAO.create(rilevazione);
        } catch (DataAccessException e) {
            System.err.println("Errore durante la registrazione della rilevazione di glicemia: " + e.getMessage());
            throw new DataAccessException("Errore durante la registrazione della rilevazione di glicemia: ", e);
        }
        try{
            monitorService.checkGlicemia(rilevazione);
        }
        catch (RuntimeException e) {
            System.err.println("Errore durante il controllo dei parametri di glicemia: " + e.getMessage());
            throw new RuntimeException("Errore durante il controllo dei parametri di glicemia: ", e);
        }
        catch (DataAccessException e) {
            System.err.println(e.getMessage());
            throw new DataAccessException("Errore durante il salvataggio del valore glicemico: ", e);
        }
    }

    /**
     * Aggiungere le assunzioni di farmaci, verificando che siano coerenti con la terapia prescritta. (usando AssunzioneFarmacoDAO e TerapiaDAO)
     * @param terapia la terapia per cui si sta registrando l'assunzione
     * @param quantitaAssunta Quantità di farmaco assunta
     */
    public void registraAssunzioneFarmaci(Terapia terapia, String quantitaAssunta, LocalDateTime timestamp) throws WrongAssumptionException, DataAccessException {

        // Verifica se la terapia esiste per il paziente
        try {
            if(terapia.getQuantita().equals(quantitaAssunta)) {
                AssunzioneFarmaci assunzione = new AssunzioneFarmaci(terapia.getIDTerapia(), terapia.getIDPaziente(), timestamp, quantitaAssunta);

                // usavi un istanza generica di assunzionefarmaci, per il test dobbiamo usare dependency injection
                assunzioneFarmaciDAO.aggiungiAssunzione(assunzione);
            }
            else {

                // Se la quantità assunta non corrisponde a quella prescritta, registra comunque l'assunzione e notifica il medico
                AssunzioneFarmaci assunzione = new AssunzioneFarmaci(terapia.getIDTerapia(), terapia.getIDPaziente(), timestamp, quantitaAssunta);

                // usavi un istanza generica di assunzionefarmaci, per il test dobbiamo usare dependency injection
                assunzioneFarmaciDAO.aggiungiAssunzione(assunzione);
                notificheService.send(terapia.getIDMedico(), 2, "Assunzione di farmaci non corretta", "Il paziente " + pazienteDAO.findNameById(terapia.getIDPaziente()) + " ha assunto una quantità di farmaco diversa da quella prescritta. Quantità assunta: " + quantitaAssunta, "Assunzione Farmaci");
                throw new WrongAssumptionException("Quantita' assunta non corrisponde con quantita' da assumere, assunzione registrata comunque.");
            }

        } catch (WrongAssumptionException e) {
            System.err.println("Quantita' assunta non corrisponde con quantita' da assumere: " + e.getMessage());
            throw new WrongAssumptionException("Quantita' assunta non corrisponde con quantita' da assumere: ");
        } catch (DataAccessException e) {
            throw new DataAccessException("Errore durante la registrazione dell'assunzione di farmaci: ", e);
        }
    }

    /**
     * Segnalare sintomi, patologie o terapie concomitanti (utilizzando CondizioniPazienteDAO)
     * @param idPaziente ID del paziente per cui si sta registrando la condizione
     * @param tipo Tipo di condizione (Sintomo, Patologia, Fattore di Rischio, Comorbidita)
     * @param descrizione Descrizione della condizione
     * @param periodo Periodo in cui la condizione è stata riscontrata
     * @throws DataAccessException se si verifica un errore durante l'accesso al database
     */

    public void segnalaCondizionePaziente(int idPaziente, String tipo, String descrizione, String periodo) throws DataAccessException {

        try{
            // Il tipo (Sintomo, Patologia, Fattore di Rischio, Comorbidita) viene forzato da una scelta nella GUI, quindi non dovrebbe essere necessario controllare il tipo\
            CondizioniPaziente condizione = new CondizioniPaziente(idPaziente, tipo, descrizione, periodo, java.time.LocalDate.now());
            condizioniDAO.create(condizione);

        } catch (DataAccessException e) {
            System.err.println("Errore durante la registrazione della condizione del paziente: " + e.getMessage());
            throw new DataAccessException("Errore durante la registrazione della condizione del paziente: ", e);
        }
    }
    
    /**
     * Invia un'email al medico di riferimento del paziente con un oggetto e un corpo specificati.
     *
     * @param idPaziente ID del paziente per cui si vuole inviare l'email
     * @param subject Oggetto dell'email
     * @param body Corpo dell'email
     * @return URL per aprire il client di posta elettronica con i campi precompilati
     * @throws DataAccessException Se si verifica un errore durante l'accesso ai dati
     * @throws SQLException Se si verifica un errore SQL durante la ricerca dell'email del medico
     */
    public String inviaEmailMedicoRiferimento(int idPaziente, String subject, String body) throws DataAccessException, SQLException {

        String emailMedico;

        // Recupera l'indirizzo email del medico di riferimento dal database
        try{
            // Trova l'email del medico di riferimento associato al paziente
            emailMedico = utenteDAO.findEmailById(pazienteDAO.findMedByIDPaziente(idPaziente)).orElse(null);
        } catch (DataAccessException e) {
            System.err.println("Errore durante la ricerca dell'email del medico di riferimento: " + e.getMessage());
            throw new DataAccessException("Errore durante la ricerca dell'email del medico di riferimento: ", e);
        }

        // Se l'email del medico è null o vuota, lancia un'eccezione
        if (emailMedico == null || emailMedico.isEmpty()) {
            throw new DataAccessException("Nessun medico di riferimento trovato per il paziente con ID: " + idPaziente);
        }

        // Codifica l'oggetto e il corpo dell'email per l'URL
        String encodedSubject = URLEncoder.encode(subject, StandardCharsets.UTF_8);
        String encodedBody = URLEncoder.encode(body, StandardCharsets.UTF_8);

        // Crea l'URL per aprire il client di posta elettronica
        return "mailto:" + emailMedico + "?subject=" + encodedSubject + "&body=" + encodedBody;
    }

    /**
     * raccoglie dati necessari per la dashboard del singolo paziente
     */
    public PazienteDashboard getDatiPazienteDasboard(Utente utente) throws MedicoServiceException {
        try{

            //recupera liste di informazioni dai DAO
            List<RilevazioneGlicemia> elencoRilevazioni = rilevazioneDAO.getRilevazioniByPaziente(utente.getIDUtente());
            List<Terapia> elencoTerapie = teratpiaDAO.listTherapiesByPatId(utente.getIDUtente());
            List<CondizioniPaziente> elencoCondizioni = condizioniDAO.listByIDPatId(utente.getIDUtente());
            List<AssunzioneFarmaci> elencoAssunzioni = assunzioneFarmaciDAO.leggiAssunzioniFarmaci(utente.getIDUtente());

            return new PazienteDashboard(utente, elencoRilevazioni, elencoTerapie, elencoCondizioni, elencoAssunzioni);
        }catch (DataAccessException e){
            throw new MedicoServiceException("Errore durante il recupero dei dati del paziente: " + e.getMessage(), e);
        }

    }
}