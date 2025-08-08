package org.univr.telemedicina.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.text.Text;
import org.univr.telemedicina.dao.*;
import org.univr.telemedicina.exception.MedicoServiceException;
import org.univr.telemedicina.model.Utente;
import org.univr.telemedicina.service.MedicoService;

import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardMedicoController {

    // Aggiungi un campo per memorizzare l'utente che ha fatto il login
    private Utente medicoLoggato;

    // Dichiarazione delle label collegate tramite @FXML
    @FXML
    private Label nameLable;

    @FXML
    private Label emailLable;

    @FXML
    private Label dateLable;

    @FXML
    private Label ageLable;
    
    @FXML
    private MenuButton pazienteMenuButton;
    
    //lable dati header
    @FXML
    private Text pazientiAttivi;

    @FXML
    private Text indiceAderenzaGlobale;

    @FXML
    private Text pazientiTotali;

    //tasti per cambiare il grafico di glicemia
    @FXML
    private Button sinistraButton;
    @FXML
    private Button destraButton;

    @FXML
    private Button settimanaleButton;
    @FXML
    private Button mensileButton;


    // Inizializza i DAO necessari per il servizio medico
    private final PazientiDAO pazientiDAO = new PazientiDAO();
    private final RilevazioneGlicemiaDAO rivelazioneGlicemiaDAO = new RilevazioneGlicemiaDAO();
    private final CondizioniPazienteDAO condizioniPazienteDAO = new CondizioniPazienteDAO();
    private final LogOperazioniDAO logOperazioniDAO = new LogOperazioniDAO();
    private final TerapiaDAO terapiaDAO = new TerapiaDAO();
    private final AssunzioneFarmaciDAO assunzioneFarmaciDAO = new AssunzioneFarmaciDAO();

    // Inizializza il servizio medico con i DAO
    private MedicoService medicoService = new MedicoService(pazientiDAO, rivelazioneGlicemiaDAO, condizioniPazienteDAO, logOperazioniDAO, terapiaDAO, assunzioneFarmaciDAO);

    /**
     * Metodo per inizializzare il controller con i dati dell'utente.
     * Questo metodo viene chiamato dal LoginController.
     * @param medicoLoggato L'utente che ha effettuato l'accesso.
     */
    public void initData(Utente medicoLoggato) {
        this.medicoLoggato = medicoLoggato;

        // Ora puoi usare i dati dell'utente per popolare la dashboard
        System.out.println("Dashboard caricata per il medico: " + medicoLoggato.getEmail());

        pazienteMenuButton.getItems().clear();


        // Imposta uno stato iniziale per le label
        nameLable.setText("Nessun paziente selezionato");
        emailLable.setText("");
        dateLable.setText("");
        ageLable.setText("");

        //imposta il testo nell'header
        topTexts(medicoLoggato);

        //carica i pazienti assegnati al medico
        init(medicoLoggato);
    }

    /**
     * Metodo che aggiorna le label nell'header della dashboard del medico.
     * @param medicoLoggato L'utente medico che ha effettuato l'accesso.
     */
    private void topTexts(Utente medicoLoggato) {
        // Recupera il numero totale di pazienti
        List<Utente> pazientiTotaliCount = null;
        String count2 = "0";
        try {
            pazientiTotaliCount = medicoService.getPazientiAssegnati(medicoLoggato.getIDUtente());
            count2 = String.valueOf(pazientiTotaliCount.size());

        } catch (MedicoServiceException e) {
            throw new RuntimeException(e);
        }
        pazientiTotali.setText(count2);


        // Recupera il numero di pazienti attivi
        List<Utente> pazientiAttiviList = null;
        String count1 = "0";
        try {
            pazientiAttiviList = medicoService.getPazientiAttivi(medicoLoggato.getIDUtente());
            count1 = String.valueOf(pazientiAttiviList.size());
        } catch (MedicoServiceException e) {
            throw new RuntimeException(e);
        }
        pazientiAttivi.setText(count1);

        // Calcola l'indice di aderenza globale
        double aderenzaGlobale = 0;
        try {
            aderenzaGlobale = medicoService.calcolaAderenzaGlobale(pazientiAttiviList);
        } catch (MedicoServiceException e) {
            throw new RuntimeException(e);
        }
        indiceAderenzaGlobale.setText(String.format("%.2f%%", aderenzaGlobale * 100));

    }


    /**
     * Metodo che inizializza la dashboard del medico.
     * Questo metodo viene chiamato per popolare il MenuButton con i pazienti assegnati al medico.
     * @param medicoLoggato L'utente medico che ha effettuato l'accesso.
     */
    private void init(Utente medicoLoggato){
        // 1. Recupera la lista dei pazienti assegnati al medico
        List<Utente> listaPazienti;
        try {

            // Recupera la lista dei pazienti assegnati al medico
            listaPazienti = medicoService.getPazientiAssegnati(medicoLoggato.getIDUtente());

            // 2. Controlla se la lista è vuota
            if (listaPazienti.isEmpty()) {
                pazienteMenuButton.setText("Nessun paziente assegnato");
            }

        } catch (Exception e) {
            System.err.println("Errore durante il recupero dei pazienti: " + e.getMessage());
            pazienteMenuButton.setText("Errore nel caricamento dei pazienti");
            return; // Esce dal metodo in caso di errore
        }

        // 3. Popola il MenuButton con i pazienti
        for (Utente paziente : listaPazienti) {
            MenuItem menuItem = new MenuItem(paziente.getNome() + " " + paziente.getCognome() + " (" + paziente.getEmail() + ")");

            // 4. Imposta l'azione da eseguire quando si clicca questo MenuItem
            menuItem.setOnAction(event -> {
                pazienteSelezionato(paziente);
                pazienteMenuButton.setText(paziente.getNome() + " " + paziente.getCognome()); // Aggiorna anche il testo del bottone
            });

            pazienteMenuButton.getItems().add(menuItem);
        }
    }

    /**
     * Metodo che viene chiamato quando un paziente viene selezionato dal MenuButton.
     * Aggiorna le label con i dati del paziente selezionato.
     *
     * @param paziente L'utente paziente selezionato.
     */
    private void pazienteSelezionato(Utente paziente) {
        nameLable.setText(paziente.getNome() + " " + paziente.getCognome());
        emailLable.setText(paziente.getEmail());
        dateLable.setText(paziente.getDataNascita().toString()); // Assicurati che il formato sia corretto
        LocalDate oggi = LocalDate.now();
        long eta = ChronoUnit.YEARS.between(paziente.getDataNascita(), oggi);
        ageLable.setText(String.valueOf(eta));
    }

    //metodi per i pulsanti del grafico di glicemia

    @FXML
    private void handleSinistraButton(ActionEvent event) {
        // Logica per il pulsante sinistra
        System.out.println("Pulsante sinistra premuto");
        // Aggiungi qui la logica per aggiornare il grafico
    }

    @FXML
    private void handleDestraButton(ActionEvent event) {
        // Logica per il pulsante destra
        System.out.println("Pulsante destra premuto");
        // Aggiungi qui la logica per aggiornare il grafico
    }

    @FXML
    private void setMensileButton(ActionEvent event) {
        // Logica per il pulsante destra
        System.out.println("Pulsante mese premuto");
        // Aggiungi qui la logica per aggiornare il grafico
    }

    @FXML
    private void setSettimanaleButton(ActionEvent event) {
        // Logica per il pulsante destra
        System.out.println("Pulsante settimana premuto");
        // Aggiungi qui la logica per aggiornare il grafico
    }

    //to do...
}