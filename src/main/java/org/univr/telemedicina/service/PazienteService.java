package org.univr.telemedicina.service;

import javafx.application.Platform;
import org.univr.telemedicina.dao.*;

import org.univr.telemedicina.exception.*;
import org.univr.telemedicina.model.*;

import java.awt.*;
import java.net.URI;
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

    // registra una nuova rilevazione di glicemia per un paziente
    // @param idpaziente id del paziente per cui si sta registrando la rilevazione
    // @param valore valore della glicemia rilevata
    // @param timestamp data e ora della rilevazione
    // @param note eventuali note aggiuntive sulla rilevazione
    public void registraRilevazioneGlicemia(int idPaziente, int valore, LocalDateTime timestamp, String note) throws DataAccessException {

        // crea un'istanza di rilevazioneglicemia con i dati forniti
        RilevazioneGlicemia rilevazione = new RilevazioneGlicemia(idPaziente, valore, timestamp, note);

        // prova a salvare la rilevazione nel database
        try {
            rilevazioneDAO.create(rilevazione);
        } catch (DataAccessException e) {
            System.err.println("errore durante la registrazione della rilevazione di glicemia " + e.getMessage());
            throw new DataAccessException("errore durante la registrazione della rilevazione di glicemia ", e);
        }
        try{
            monitorService.checkGlicemia(rilevazione);
        }
        catch (RuntimeException e) {
            System.err.println("errore durante il controllo dei parametri di glicemia " + e.getMessage());
            throw new RuntimeException("errore durante il controllo dei parametri di glicemia ", e);
        }
        catch (DataAccessException e) {
            System.err.println(e.getMessage());
            throw new DataAccessException("errore durante il salvataggio del valore glicemico ", e);
        }
    }

    // aggiungere le assunzioni di farmaci verificando che siano coerenti con la terapia prescritta (usando assunzionefarmacodao e terapiadao)
    // @param terapia la terapia per cui si sta registrando l'assunzione
    // @param quantitaassunta quantità di farmaco assunta
    public void registraAssunzioneFarmaci(Terapia terapia, String quantitaAssunta, LocalDateTime timestamp) throws WrongAssumptionException, DataAccessException {

        // verifica se la terapia esiste per il paziente
        try {
            if(terapia.getQuantita().equals(quantitaAssunta)) {
                AssunzioneFarmaci assunzione = new AssunzioneFarmaci(terapia.getIDTerapia(), terapia.getIDPaziente(), timestamp, quantitaAssunta);

                // usavi un istanza generica di assunzionefarmaci per il test dobbiamo usare dependency injection
                assunzioneFarmaciDAO.aggiungiAssunzione(assunzione);
            }
            else {

                // se la quantità assunta non corrisponde a quella prescritta registra comunque l'assunzione e notifica il medico
                AssunzioneFarmaci assunzione = new AssunzioneFarmaci(terapia.getIDTerapia(), terapia.getIDPaziente(), timestamp, quantitaAssunta);

                // usavi un istanza generica di assunzionefarmaci per il test dobbiamo usare dependency injection
                assunzioneFarmaciDAO.aggiungiAssunzione(assunzione);
                notificheService.send(terapia.getIDMedico(), 2, "assunzione di farmaci non corretta", "il paziente " + pazienteDAO.findNameById(terapia.getIDPaziente()) + " ha assunto una quantità di farmaco diversa da quella prescritta quantità assunta " + quantitaAssunta, "assunzione farmaci");
                throw new WrongAssumptionException("quantita' assunta non corrisponde con quantita' da assumere assunzione registrata comunque");
            }

        } catch (WrongAssumptionException e) {
            System.err.println("quantita' assunta non corrisponde con quantita' da assumere " + e.getMessage());
            throw new WrongAssumptionException("quantita' assunta non corrisponde con quantita' da assumere ");
        } catch (DataAccessException e) {
            throw new DataAccessException("errore durante la registrazione dell'assunzione di farmaci ", e);
        }
    }

    // segnalare sintomi patologie o terapie concomitanti (utilizzando condizionipazientedao)
    // @param idpaziente id del paziente per cui si sta registrando la condizione
    // @param tipo tipo di condizione (sintomo patologia fattore di rischio comorbidita)
    // @param descrizione descrizione della condizione
    // @param periodo periodo in cui la condizione è stata riscontrata
    // @throws dataaccessexception se si verifica un errore durante l'accesso al database
    public void segnalaCondizionePaziente(int idPaziente, String tipo, String descrizione, String periodo) throws DataAccessException {

        try{
            // il tipo (sintomo patologia fattore di rischio comorbidita) viene forzato da una scelta nella gui quindi non dovrebbe essere necessario controllare il tipo\
            CondizioniPaziente condizione = new CondizioniPaziente(idPaziente, tipo, descrizione, periodo, java.time.LocalDate.now());
            condizioniDAO.create(condizione);

        } catch (DataAccessException e) {
            System.err.println("errore durante la registrazione della condizione del paziente " + e.getMessage());
            throw new DataAccessException("errore durante la registrazione della condizione del paziente ", e);
        }
    }

    // modifica una condizione esistente per un paziente
    // @param condizione l'oggetto condizionipaziente contenente i dati aggiornati della condizione
    // @throws dataaccessexception se si verifica un errore durante l'accesso ai dati
    public void modificaCondizione(CondizioniPaziente condizione) throws DataAccessException {
        try {
            condizioniDAO.update(condizione);
        } catch (DataAccessException e) {
            System.err.println("errore durante la modifica della condizione " + e.getMessage());
            throw new DataAccessException("errore durante la modifica della condizione ", e);
        }
    }

    // elimina una condizione esistente per un paziente
    // @param idcondizione id della condizione da eliminare
    // @throws dataaccessexception se si verifica un errore durante l'accesso ai dati
    public void eliminaCondizione(int idCondizione) throws DataAccessException {
        try {
            condizioniDAO.delete(idCondizione);
        } catch (DataAccessException e) {
            System.err.println("errore during l'eliminazione della condizione " + e.getMessage());
            throw new DataAccessException("errore during l'eliminazione della condizione ", e);
        }
    }
    
    // invia un'email al medico di riferimento del paziente con un oggetto e un corpo specificati
    // @param idpaziente id del paziente per cui si vuole inviare l'email
    // @param subject oggetto dell'email
    // @param body corpo dell'email
    // @throws dataaccessexception se si verifica un errore durante l'accesso ai dati
    // @throws sqlexception se si verifica un errore sql durante la ricerca dell'email del medico
    public void inviaEmailMedicoRiferimento(int idPaziente, String subject, String body) throws DataAccessException, SQLException {

        String emailMedico;

        // recupera l'indirizzo email del medico di riferimento dal database
        try{
            // trova l'email del medico di riferimento associato al paziente
            emailMedico = utenteDAO.findEmailById(pazienteDAO.findMedByIDPaziente(idPaziente)).orElse(null);
        } catch (DataAccessException e) {
            System.err.println("errore durante la ricerca dell'email del medico di riferimento " + e.getMessage());
            throw new DataAccessException("errore durante la ricerca dell'email del medico di riferimento ", e);
        }

        // se l'email del medico è null o vuota lancia un'eccezione
        if (emailMedico == null || emailMedico.isEmpty()) {
            throw new DataAccessException("nessun medico di riferimento trovato per il paziente con id " + idPaziente);
        }

        // eseguiamo tutta l'operazione in un nuovo thread
        new Thread(() -> {

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MAIL)) {
                try {
                    String uriString = String.format("mailto:%s?subject=%s&body=%s",
                            emailMedico,
                            URLEncoder.encode(subject, StandardCharsets.UTF_8).replace("+", "%20"),
                            URLEncoder.encode(body, StandardCharsets.UTF_8).replace("+", "%20")
                    );

                    URI mailto = new URI(uriString);
                    Desktop.getDesktop().mail(mailto);

                } catch (Exception e) {
                    final String messaggioErrore = "impossibile aprire il client di posta " + e.getMessage();
                    Platform.runLater(() -> mostraErroreInUI(messaggioErrore));
                }
            } else {
                final String messaggioErrore = "funzionalità di invio email non supportata su questo sistema";
                Platform.runLater(() -> mostraErroreInUI(messaggioErrore));
            }
        }).start(); // avvia il thread
    }

    // mostra un messaggio di errore
    // @param messaggio il messaggio di errore da visualizzare
    private void mostraErroreInUI(String messaggio) {
        System.out.println("errore ui " + messaggio);
    }

    // raccoglie dati necessari per la dashboard del singolo paziente
    public PazienteDashboard getDatiPazienteDasboard(Utente utente) throws MedicoServiceException {
        try{

            //recupera liste di informazioni dai dao
            List<RilevazioneGlicemia> elencoRilevazioni = rilevazioneDAO.getRilevazioniByPaziente(utente.getIDUtente());
            List<Terapia> elencoTerapie = teratpiaDAO.listTherapiesByPatId(utente.getIDUtente());
            List<CondizioniPaziente> elencoCondizioni = condizioniDAO.listByIDPatId(utente.getIDUtente());
            List<AssunzioneFarmaci> elencoAssunzioni = assunzioneFarmaciDAO.leggiAssunzioniFarmaci(utente.getIDUtente());

            return new PazienteDashboard(utente, elencoRilevazioni, elencoTerapie, elencoCondizioni, elencoAssunzioni);
        }catch (DataAccessException e){
            throw new MedicoServiceException("errore durante il recupero dei dati del paziente " + e.getMessage(), e);
        }

    }
}