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
    @FXML
    private Text pazientiAttivi;
    @FXML
    private Text indiceAderenzaGlobale;
    @FXML
    private Text pazientiTotali;
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
    @FXML
    private Label periodoLabel;

    private final PazientiDAO pazientiDAO = new PazientiDAO();
    private final RilevazioneGlicemiaDAO rivelazioneGlicemiaDAO = new RilevazioneGlicemiaDAO();
    private final CondizioniPazienteDAO condizioniPazienteDAO = new CondizioniPazienteDAO();
    private final LogOperazioniDAO logOperazioniDAO = new LogOperazioniDAO();
    private final TerapiaDAO terapiaDAO = new TerapiaDAO();
    private final AssunzioneFarmaciDAO assunzioneFarmaciDAO = new AssunzioneFarmaciDAO();
    private final MedicoService medicoService = new MedicoService(pazientiDAO, rivelazioneGlicemiaDAO, condizioniPazienteDAO, logOperazioniDAO, terapiaDAO, assunzioneFarmaciDAO);

    private Parent formTerapia;
    private Parent formCondizioni;
    private Utente pazienteSelezionato;
    private String tipoVista = "mensile"; // o "settimanale"
    private LocalDate dataCorrente = LocalDate.now();

    public void initData(Utente medicoLoggato) {
        this.medicoLoggato = medicoLoggato;
        System.out.println("Dashboard caricata per il medico: " + medicoLoggato.getEmail());
        pazienteMenuButton.getItems().clear();
        nameLable.setText("Nessun paziente selezionato");
        emailLable.setText("");
        dateLable.setText("");
        ageLable.setText("");
        topTexts(medicoLoggato);
        init(medicoLoggato);

        try {
            formTerapia = FXMLLoader.load(getClass().getResource("/org/univr/telemedicina/gui/fxml/form_terapia.fxml"));
            formCondizioni = FXMLLoader.load(getClass().getResource("/org/univr/telemedicina/gui/fxml/form_condizioni.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        handleCreaModificaTerapiaButton(null);
    }

    private void topTexts(Utente medicoLoggato) {
        try {
            List<Utente> pazientiTotaliCount = medicoService.getPazientiAssegnati(medicoLoggato.getIDUtente());
            pazientiTotali.setText(String.valueOf(pazientiTotaliCount.size()));

            List<Utente> pazientiAttiviList = medicoService.getPazientiAttivi(medicoLoggato.getIDUtente());
            pazientiAttivi.setText(String.valueOf(pazientiAttiviList.size()));

            double aderenzaGlobale = medicoService.calcolaAderenzaGlobale(pazientiAttiviList);
            indiceAderenzaGlobale.setText(String.format("%.2f", aderenzaGlobale * 100) + "%");
        } catch (MedicoServiceException e) {
            throw new RuntimeException(e);
        }
    }

    private void init(Utente medicoLoggato) {
        try {
            List<Utente> listaPazienti = medicoService.getPazientiAssegnati(medicoLoggato.getIDUtente());
            if (listaPazienti.isEmpty()) {
                pazienteMenuButton.setText("Nessun paziente assegnato");
            }

            for (Utente paziente : listaPazienti) {
                MenuItem menuItem = new MenuItem(paziente.getNome() + " " + paziente.getCognome() + " (" + paziente.getEmail() + ")");
                menuItem.setOnAction(event -> {
                    pazienteSelezionato(paziente);
                    pazienteMenuButton.setText(paziente.getNome() + " " + paziente.getCognome());
                });
                pazienteMenuButton.getItems().add(menuItem);
            }
        } catch (Exception e) {
            System.err.println("Errore durante il recupero dei pazienti: " + e.getMessage());
            pazienteMenuButton.setText("Errore nel caricamento dei pazienti");
        }
    }

    private void pazienteSelezionato(Utente paziente) {
        this.pazienteSelezionato = paziente;
        nameLable.setText(paziente.getNome() + " " + paziente.getCognome());
        emailLable.setText(paziente.getEmail());
        dateLable.setText(paziente.getDataNascita().toString());
        LocalDate oggi = LocalDate.now();
        long eta = ChronoUnit.YEARS.between(paziente.getDataNascita(), oggi);
        ageLable.setText(String.valueOf(eta));

        informazioniPazienteContainer.getChildren().clear();
        terapiePrescritteContainer.getChildren().clear();

        try {
            PazienteDashboard datiPaziente = medicoService.getDatiPazienteDasboard(paziente);

            VBox condizioniBox = new VBox(5);
            for (CondizioniPaziente condizione : datiPaziente.getElencoCondizioni()) {
                Label label = new Label(condizione.getTipo() + ": " + condizione.getDescrizione());
                condizioniBox.getChildren().add(label);
            }
            informazioniPazienteContainer.getChildren().add(condizioniBox);

            VBox terapieBox = new VBox(5);
            for (Terapia terapia : datiPaziente.getElencoTerapie()) {
                Label label = new Label(terapia.getNomeFarmaco() + " - " + terapia.getQuantita());
                terapieBox.getChildren().add(label);
            }
            terapiePrescritteContainer.getChildren().add(terapieBox);

            updateChart();

        } catch (MedicoServiceException e) {
            e.printStackTrace();
        }
    }

    private void updateChart() {
        glicemiaChart.getData().clear();
        if (pazienteSelezionato == null) return;

        try {
            PazienteDashboard datiPaziente = medicoService.getDatiPazienteDasboard(pazienteSelezionato);
            List<RilevazioneGlicemia> rilevazioni = datiPaziente.getElencoRilevazioni();

            LocalDate inizioPeriodo;
            LocalDate finePeriodo;

            if ("settimanale".equals(tipoVista)) {
                inizioPeriodo = dataCorrente.minusDays(dataCorrente.getDayOfWeek().getValue() - 1);
                finePeriodo = inizioPeriodo.plusDays(6);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");
                periodoLabel.setText(inizioPeriodo.format(formatter) + "-" + finePeriodo.format(formatter) + " " + dataCorrente.format(DateTimeFormatter.ofPattern("MMMM")));
            } else { // mensile
                inizioPeriodo = dataCorrente.withDayOfMonth(1);
                finePeriodo = dataCorrente.withDayOfMonth(dataCorrente.lengthOfMonth());
                periodoLabel.setText(dataCorrente.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            }

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Andamento Glicemia");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

            List<XYChart.Data<String, Number>> dataPoints = new java.util.ArrayList<>();
            for (RilevazioneGlicemia rilevazione : rilevazioni) {
                LocalDate dataRilevazione = rilevazione.getTimestamp().toLocalDate();
                if (!dataRilevazione.isBefore(inizioPeriodo) && !dataRilevazione.isAfter(finePeriodo)) {
                    dataPoints.add(new XYChart.Data<>(rilevazione.getTimestamp().format(formatter), rilevazione.getValore()));
                }
            }

            // Inverti l'ordine dei dati
            java.util.Collections.reverse(dataPoints);
            series.getData().addAll(dataPoints);
            glicemiaChart.getData().add(series);

        } catch (MedicoServiceException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSinistraButton(ActionEvent event) {
        if ("settimanale".equals(tipoVista)) {
            dataCorrente = dataCorrente.minusWeeks(1);
        } else {
            dataCorrente = dataCorrente.minusMonths(1);
        }
        updateChart();
    }

    @FXML
    private void handleDestraButton(ActionEvent event) {
        if ("settimanale".equals(tipoVista)) {
            dataCorrente = dataCorrente.plusWeeks(1);
        } else {
            dataCorrente = dataCorrente.plusMonths(1);
        }
        updateChart();
    }

    @FXML
    private void setMensileButton(ActionEvent event) {
        tipoVista = "mensile";
        updateChart();
        mensileButton.getStyleClass().remove("deactivated-button-graph");
        mensileButton.getStyleClass().add("activated-button-graph");
        settimanaleButton.getStyleClass().remove("activated-button-graph");
        settimanaleButton.getStyleClass().add("deactivated-button-graph");
    }

    @FXML
    private void setSettimanaleButton(ActionEvent event) {
        tipoVista = "settimanale";
        updateChart();
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
