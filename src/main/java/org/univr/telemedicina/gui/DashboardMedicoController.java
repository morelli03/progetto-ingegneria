package org.univr.telemedicina.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.univr.telemedicina.dao.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import org.univr.telemedicina.exception.MedicoServiceException;
import org.univr.telemedicina.model.*;
import org.univr.telemedicina.service.MedicoService;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    @FXML
    private Button creaModificaTerapiaButton;

    @FXML
    private Button creaModificaCondizioniButton;

    @FXML
    private VBox formContainer;

    @FXML
    private AnchorPane informazioniPazienteContainer;

    @FXML
    private AnchorPane terapiePrescritteContainer;

    @FXML
    private LineChart<String, Number> glicemiaChart;


    // Inizializza i DAO necessari per il servizio medico
    private final PazientiDAO pazientiDAO = new PazientiDAO();
    private final RilevazioneGlicemiaDAO rivelazioneGlicemiaDAO = new RilevazioneGlicemiaDAO();
    private final CondizioniPazienteDAO condizioniPazienteDAO = new CondizioniPazienteDAO();
    private final LogOperazioniDAO logOperazioniDAO = new LogOperazioniDAO();
    private final TerapiaDAO terapiaDAO = new TerapiaDAO();
    private final AssunzioneFarmaciDAO assunzioneFarmaciDAO = new AssunzioneFarmaciDAO();

    // Inizializza il servizio medico con i DAO
    private MedicoService medicoService = new MedicoService(pazientiDAO, rivelazioneGlicemiaDAO, condizioniPazienteDAO, logOperazioniDAO, terapiaDAO, assunzioneFarmaciDAO);

    private Parent formTerapia;
    private Parent formCondizioni;

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

        // Carica i form FXML per terapia e condizioni
        // Questi form saranno caricati in un VBox per essere visualizzati dinamicamente
        try {
            formTerapia = FXMLLoader.load(getClass().getResource("/org/univr/telemedicina/gui/fxml/form_terapia.fxml"));
            formCondizioni = FXMLLoader.load(getClass().getResource("/org/univr/telemedicina/gui/fxml/form_condizioni.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Imposta il form di default
        handleCreaModificaTerapiaButton(null);
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
        indiceAderenzaGlobale.setText(String.format("%.2f", aderenzaGlobale * 100) + "%");

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

        // Clear previous data
        informazioniPazienteContainer.getChildren().clear();
        terapiePrescritteContainer.getChildren().clear();
        glicemiaChart.getData().clear();

        try {
            // Get patient dashboard data
            PazienteDashboard datiPaziente = medicoService.getDatiPazienteDasboard(paziente);

            // Populate patient conditions
            VBox condizioniBox = new VBox(5); // Use a VBox for vertical layout
            for (CondizioniPaziente condizione : datiPaziente.getElencoCondizioni()) {
                Label label = new Label(condizione.getTipo() + ": " + condizione.getDescrizione());
                condizioniBox.getChildren().add(label);
            }
            informazioniPazienteContainer.getChildren().add(condizioniBox);

            // Populate therapies
            VBox terapieBox = new VBox(5);
            for (Terapia terapia : datiPaziente.getElencoTerapie()) {
                Label label = new Label(terapia.getNomeFarmaco() + " - " + terapia.getQuantita());
                terapieBox.getChildren().add(label);
            }
            terapiePrescritteContainer.getChildren().add(terapieBox);

            // Populate glycemia chart
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Andamento Glicemia");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            for (RilevazioneGlicemia rilevazione : datiPaziente.getElencoRilevazioni()) {
                series.getData().add(new XYChart.Data<>(rilevazione.getTimestamp().format(formatter), rilevazione.getValore()));
            }
            glicemiaChart.getData().add(series);

        } catch (MedicoServiceException e) {
            // Handle exception (e.g., show an alert to the user)
            e.printStackTrace();
        }
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
        mensileButton.getStyleClass().remove("deactivated-button-graph");
        mensileButton.getStyleClass().add("activated-button-graph");
        settimanaleButton.getStyleClass().remove("activated-button-graph");
        settimanaleButton.getStyleClass().add("deactivated-button-graph");
    }

    @FXML
    private void setSettimanaleButton(ActionEvent event) {
        // Logica per il pulsante destra
        System.out.println("Pulsante settimana premuto");
        // Aggiungi qui la logica per aggiornare il grafico
        settimanaleButton.getStyleClass().remove("deactivated-button-graph");
        settimanaleButton.getStyleClass().add("activated-button-graph");
        mensileButton.getStyleClass().remove("activated-button-graph");
        mensileButton.getStyleClass().add("deactivated-button-graph");
    }

    @FXML
    private void handleCreaModificaTerapiaButton(ActionEvent event) {
        // Logica per il pulsante Crea/Modifica Terapia
        creaModificaTerapiaButton.getStyleClass().remove("deactivated-button-graph");
        creaModificaTerapiaButton.getStyleClass().add("activated-button-graph");
        creaModificaCondizioniButton.getStyleClass().remove("activated-button-graph");
        creaModificaCondizioniButton.getStyleClass().add("deactivated-button-graph");
        System.out.println("Pulsante Crea/Modifica Terapia premuto");
        formContainer.getChildren().setAll(formTerapia);
    }

    @FXML
    private void handleCreaModificaCondizioniButton(ActionEvent event) {
        // Logica per il pulsante Crea/Modifica Condizioni
        creaModificaCondizioniButton.getStyleClass().remove("deactivated-button-graph");
        creaModificaCondizioniButton.getStyleClass().add("activated-button-graph");
        creaModificaTerapiaButton.getStyleClass().remove("activated-button-graph");
        creaModificaTerapiaButton.getStyleClass().add("deactivated-button-graph");
        System.out.println("Pulsante Crea/Modifica Condizioni premuto");
        formContainer.getChildren().setAll(formCondizioni);
    }
}