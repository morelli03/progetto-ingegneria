package org.univr.telemedicina.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.univr.telemedicina.dao.*;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.exception.MedicoServiceException;
import org.univr.telemedicina.exception.TherapyException;
import org.univr.telemedicina.model.*;
import org.univr.telemedicina.service.MedicoService;
import org.univr.telemedicina.service.NotificheService;
import org.univr.telemedicina.service.TerapiaService;
import javafx.scene.control.Tooltip;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DashboardMedicoController {

    // aggiungi un campo per memorizzare l'utente che ha fatto il login
    private Utente medicoLoggato;

    // campi per il form terapia
    private ComboBox<Object> terapiaComboBox;
    private TextField farmacoTextField;
    private TextField quantitaTextField;
    private TextField frequenzaTextField;
    private TextArea indicazioniTextArea;
    private DatePicker dataInizioPicker;
    private DatePicker dataFinePicker;
    private CheckBox dataFineCheckBox;

    // Campi per il form condizioni
    private ComboBox<Object> condizioneComboBox;
    private ComboBox<String> tipoCondizioneComboBox;
    private TextArea descrizioneCondizioneTextArea;
    private TextArea periodoCondizioneTextArea;

    private final PazientiDAO pazientiDAO = new PazientiDAO();
    private final RilevazioneGlicemiaDAO rivelazioneGlicemiaDAO = new RilevazioneGlicemiaDAO();
    private final CondizioniPazienteDAO condizioniPazienteDAO = new CondizioniPazienteDAO();
    private final LogOperazioniDAO logOperazioniDAO = new LogOperazioniDAO();
    private final TerapiaDAO terapiaDAO = new TerapiaDAO();
    private final AssunzioneFarmaciDAO assunzioneFarmaciDAO = new AssunzioneFarmaciDAO();
    private final MedicoService medicoService = new MedicoService(pazientiDAO, rivelazioneGlicemiaDAO, condizioniPazienteDAO, logOperazioniDAO, terapiaDAO, assunzioneFarmaciDAO);
    private final TerapiaService terapiaService = new TerapiaService(terapiaDAO, logOperazioniDAO);
    private final NotificheService notificheService = new NotificheService(new NotificheDAO());
    private ScheduledExecutorService notificationScheduler;
    private List<Notifica> allNotifications;


    private Parent formTerapia;
    private Parent formCondizioni;
    private Utente pazienteSelezionato;
    private PazienteDashboard datiPazienteCorrente;
    private String tipoVista = "mensile"; // o "settimanale"
    private LocalDate dataCorrente = LocalDate.now();
    private String formCorrente;

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
    private Button saveButton;
    @FXML
    private Button deleteButton;
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
    @FXML
    private Button notificationButton;

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
        initializeNotifications();

        try {
            formTerapia = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/univr/telemedicina/gui/fxml/form_terapia.fxml")));
            formCondizioni = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/univr/telemedicina/gui/fxml/form_condizioni.fxml")));

            // Inizializzazione campi form terapia
            terapiaComboBox = (ComboBox<Object>) formTerapia.lookup("#terapiaComboBox");
            farmacoTextField = (TextField) formTerapia.lookup("#farmacoTextField");
            quantitaTextField = (TextField) formTerapia.lookup("#quantitaTextField");
            frequenzaTextField = (TextField) formTerapia.lookup("#frequenzaTextField");
            indicazioniTextArea = (TextArea) formTerapia.lookup("#indicazioniTextArea");
            dataInizioPicker = (DatePicker) formTerapia.lookup("#dataInizioPicker");
            dataFinePicker = (DatePicker) formTerapia.lookup("#dataFinePicker");
            dataFineCheckBox = (CheckBox) formTerapia.lookup("#dataFineCheckBox");

            // Inizializzazione campi form condizioni
            condizioneComboBox = (ComboBox<Object>) formCondizioni.lookup("#condizioneComboBox");
            tipoCondizioneComboBox = (ComboBox<String>) formCondizioni.lookup("#tipoCondizioneComboBox");
            descrizioneCondizioneTextArea = (TextArea) formCondizioni.lookup("#descrizioneCondizioneTextArea");
            periodoCondizioneTextArea = (TextArea) formCondizioni.lookup("#periodoCondizioneTextArea");

            // Aggiungi listener
            terapiaComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    mostraDettagliTerapia(newSelection);
                }
            });

            condizioneComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    mostraDettagliCondizione(newSelection);
                }
            });

            dataFineCheckBox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                dataFinePicker.setDisable(!isNowSelected);
                if (!isNowSelected) {
                    dataFinePicker.setValue(null);
                }
            });

            // Imposta stato iniziale
            dataFineCheckBox.setSelected(false);
            dataFinePicker.setDisable(true);
            deleteButton.setDisable(true);

            tipoCondizioneComboBox.setItems(FXCollections.observableArrayList("FattoreRischio", "Patologia", "Comorbidita"));

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
        ageLable.setText(String.valueOf(eta) + " anni");

        informazioniPazienteContainer.getChildren().clear();
        terapiePrescritteContainer.getChildren().clear();

        try {
            datiPazienteCorrente = medicoService.getDatiPazienteDasboard(paziente);

            VBox condizioniBox = new VBox(5);
            for (CondizioniPaziente condizione : datiPazienteCorrente.getElencoCondizioni()) {

                if(Objects.equals(condizione.getTipo(), "FattoreRischio")) {
                    Label label = new Label("Fattore di rischio: " + condizione.getDescrizione());
                    label.getStyleClass().add("black-text");
                    condizioniBox.getChildren().add(label);
                } else if(Objects.equals(condizione.getTipo(), "Patologia")) {
                    Label label = new Label("Patologia: " + condizione.getDescrizione());
                    label.getStyleClass().add("black-text");
                    condizioniBox.getChildren().add(label);
                } else if(Objects.equals(condizione.getTipo(), "Comorbidita")) {
                    Label label = new Label("Comorbidità: " + condizione.getDescrizione());
                    label.getStyleClass().add("black-text");
                    condizioniBox.getChildren().add(label);
                } else if(Objects.equals(condizione.getTipo(), "Sintomo")) {
                    Label label = new Label("Sintomo: " + condizione.getDescrizione());
                    label.getStyleClass().add("black-text");
                    condizioniBox.getChildren().add(label);
                } else if(Objects.equals(condizione.getTipo(), "TerapiaConcomitante")) {
                    Label label = new Label("Terapia concomitante: " + condizione.getDescrizione());
                    label.getStyleClass().add("black-text");
                    condizioniBox.getChildren().add(label);
                }
            }
            informazioniPazienteContainer.getChildren().add(condizioniBox);

            VBox terapieBox = new VBox(5);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (Terapia terapia : datiPazienteCorrente.getElencoTerapie()) {
                String dataFineText = (terapia.getDataFine() != null) ? terapia.getDataFine().format(formatter) : "N/A";

                Label label = new Label(terapia.getNomeFarmaco() + " | " +
                        terapia.getQuantita() + " | " +
                        "freq. giornaliera: " + terapia.getFrequenzaGiornaliera() + " | " +
                        terapia.getDataInizio().format(formatter) + " - " + dataFineText);
                label.getStyleClass().add("black-text");
                terapieBox.getChildren().add(label);
            }
            terapiePrescritteContainer.getChildren().add(terapieBox);

            updateChart();
            caricaTerapiePaziente();
            caricaCondizioniPaziente();

        } catch (MedicoServiceException e) {
            e.printStackTrace();
        }
    }

    private void updateChart() {
        glicemiaChart.getData().clear();
        if (pazienteSelezionato == null) return;

        List<RilevazioneGlicemia> rilevazioni = datiPazienteCorrente.getElencoRilevazioni();

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
                dataPoints.add(new XYChart.Data<>(rilevazione.getTimestamp().format(formatter), rilevazione.getValore(), rilevazione));
            }
        }

        java.util.Collections.reverse(dataPoints);
        series.getData().addAll(dataPoints);
        glicemiaChart.getData().add(series);

        final Tooltip tooltip = new Tooltip();
        tooltip.setStyle("-fx-font-size: 14px;");

        for (XYChart.Data<String, Number> data : series.getData()) {

            data.getNode().setStyle("-fx-background-color: #454545;");

            // ingrandisco leggermente il punto per renderlo più facile da selezionare
            data.getNode().setScaleX(1.2);
            data.getNode().setScaleY(1.2);

            data.getNode().setOnMouseEntered(event -> {
                RilevazioneGlicemia rilevazione = (RilevazioneGlicemia) data.getExtraValue();
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                String tooltipText = String.format(
                        "Valore: %d mg/dL\nOra: %s\n%s",
                        rilevazione.getValore(),
                        rilevazione.getTimestamp().format(timeFormatter),
                        rilevazione.getNote() != null && !rilevazione.getNote().isEmpty() ? rilevazione.getNote() : "Nessuna nota"
                );

                tooltip.setText(tooltipText);
                tooltip.show(data.getNode().getScene().getWindow(), event.getScreenX() + 15, event.getScreenY() + 15);

                // quando il mouse è sopra
                data.getNode().setScaleX(1.5);
                data.getNode().setScaleY(1.5);
            });

            data.getNode().setOnMouseExited(event -> {
                tooltip.hide();
                data.getNode().setStyle("-fx-background-color: #454545;");
                data.getNode().setScaleX(1.2);
                data.getNode().setScaleY(1.2);
            });
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
        formCorrente = "terapia";
        // Update button styles - ensure only one button is activated
        setButtonActivated(creaModificaTerapiaButton);
        setButtonDeactivated(creaModificaCondizioniButton);
        formContainer.getChildren().setAll(formTerapia);
    }

    @FXML
    private void handleCreaModificaCondizioniButton(ActionEvent event) {
        formCorrente = "condizioni";
        // Update button styles - ensure only one button is activated
        setButtonActivated(creaModificaCondizioniButton);
        setButtonDeactivated(creaModificaTerapiaButton);
        formContainer.getChildren().setAll(formCondizioni);
    }
    
    private void setButtonActivated(Button button) {
        button.getStyleClass().remove("deactivated-button-graph");
        if (!button.getStyleClass().contains("activated-button-graph")) {
            button.getStyleClass().add("activated-button-graph");
        }
    }
    
    private void setButtonDeactivated(Button button) {
        button.getStyleClass().remove("activated-button-graph");
        if (!button.getStyleClass().contains("deactivated-button-graph")) {
            button.getStyleClass().add("deactivated-button-graph");
        }
    }

    @FXML
    private void handleSaveButton(ActionEvent event) {
        if (pazienteSelezionato == null) {
            showAlert("Errore", "Nessun paziente selezionato.");
            return;
        }

        if ("terapia".equals(formCorrente)) {
            salvaTerapia();
        } else if ("condizioni".equals(formCorrente)) {
            salvaCondizione();
        }
    }

    @FXML
    private void handleDeleteButton(ActionEvent event) {
        if (pazienteSelezionato == null) {
            showAlert("Errore", "Nessun paziente selezionato.");
            return;
        }

        if ("terapia".equals(formCorrente)) {
            eliminaTerapia();
        } else if ("condizioni".equals(formCorrente)) {
            eliminaCondizione();
        }
    }

    private void caricaTerapiePaziente() {
        terapiaComboBox.getItems().clear();
        terapiaComboBox.getItems().add("Nuova terapia");
        if (datiPazienteCorrente != null) {
            terapiaComboBox.getItems().addAll(datiPazienteCorrente.getElencoTerapie());
        }
        terapiaComboBox.setCellFactory(param -> new ListCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else if (item instanceof String) {
                    setText((String) item);
                } else {
                    setText(((Terapia) item).getNomeFarmaco());
                }
            }
        });
        terapiaComboBox.setButtonCell(new ListCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else if (item instanceof String) {
                    setText((String) item);
                } else {
                    setText(((Terapia) item).getNomeFarmaco());
                }
            }
        });
        pulisciFormTerapia();
    }

    private void caricaCondizioniPaziente() {
        condizioneComboBox.getItems().clear();
        condizioneComboBox.getItems().add("Nuova condizione");
        if (datiPazienteCorrente != null) {
            List<CondizioniPaziente> condizioniFiltrate = datiPazienteCorrente.getElencoCondizioni().stream()
                    .filter(c -> "FattoreRischio".equals(c.getTipo()) ||
                            "Patologia".equals(c.getTipo()) ||
                            "Comorbidita".equals(c.getTipo()))
                    .collect(Collectors.toList());
            condizioneComboBox.getItems().addAll(condizioniFiltrate);
        }
        condizioneComboBox.setCellFactory(param -> new ListCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else if (item instanceof String) {
                    setText((String) item);
                } else {
                    setText(((CondizioniPaziente) item).getTipo() + " - " + ((CondizioniPaziente) item).getDescrizione());
                }
            }
        });
        condizioneComboBox.setButtonCell(new ListCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else if (item instanceof String) {
                    setText((String) item);
                } else {
                    setText(((CondizioniPaziente) item).getTipo() + " - " + ((CondizioniPaziente) item).getDescrizione());
                }
            }
        });
        pulisciFormCondizione();
    }

    private void mostraDettagliTerapia(Object terapiaObj) {
        deleteButton.setDisable(!(terapiaObj instanceof Terapia));
        if (terapiaObj instanceof Terapia) {
            Terapia terapia = (Terapia) terapiaObj;
            farmacoTextField.setText(terapia.getNomeFarmaco());
            quantitaTextField.setText(terapia.getQuantita());
            frequenzaTextField.setText(String.valueOf(terapia.getFrequenzaGiornaliera()));
            indicazioniTextArea.setText(terapia.getIndicazioni());
            dataInizioPicker.setValue(terapia.getDataInizio());
            if (terapia.getDataFine() != null) {
                dataFinePicker.setValue(terapia.getDataFine());
                dataFineCheckBox.setSelected(true);
            } else {
                dataFinePicker.setValue(null);
                dataFineCheckBox.setSelected(false);
            }
        } else {
            pulisciFormTerapia();
        }
    }

    private void mostraDettagliCondizione(Object condizioneObj) {
        deleteButton.setDisable(!(condizioneObj instanceof CondizioniPaziente));
        if (condizioneObj instanceof CondizioniPaziente) {
            CondizioniPaziente condizione = (CondizioniPaziente) condizioneObj;
            tipoCondizioneComboBox.setValue(condizione.getTipo());
            descrizioneCondizioneTextArea.setText(condizione.getDescrizione());
            periodoCondizioneTextArea.setText(condizione.getPeriodo());
        } else {
            pulisciFormCondizione();
        }
    }

    private void pulisciFormTerapia() {
        terapiaComboBox.getSelectionModel().selectFirst();
        farmacoTextField.clear();
        quantitaTextField.clear();
        frequenzaTextField.clear();
        indicazioniTextArea.clear();
        dataInizioPicker.setValue(LocalDate.now());
        dataFinePicker.setValue(null);
        dataFineCheckBox.setSelected(false);
    }

    private void pulisciFormCondizione() {
        condizioneComboBox.getSelectionModel().selectFirst();
        tipoCondizioneComboBox.getSelectionModel().clearSelection();
        descrizioneCondizioneTextArea.clear();
        periodoCondizioneTextArea.clear();
    }

    private void salvaTerapia() {
        String nomeFarmaco = farmacoTextField.getText();
        String quantita = quantitaTextField.getText();
        String indicazioni = indicazioniTextArea.getText();
        LocalDate dataInizio = dataInizioPicker.getValue();
        LocalDate dataFine = dataFinePicker.getValue();

        if (nomeFarmaco.isEmpty() || quantita.isEmpty() || dataInizio == null) {
            showAlert("Errore", "Compilare tutti i campi obbligatori.");
            return;
        }

        int frequenza;
        try {
            frequenza = Integer.parseInt(frequenzaTextField.getText());
        } catch (NumberFormatException e) {
            showAlert("Errore", "La frequenza deve essere un numero.");
            return;
        }

        Object selected = terapiaComboBox.getSelectionModel().getSelectedItem();
        try {
            if (selected instanceof Terapia) {
                Terapia terapia = (Terapia) selected;
                terapia.setNomeFarmaco(nomeFarmaco);
                terapia.setQuantita(quantita);
                terapia.setFrequenzaGiornaliera(frequenza);
                terapia.setIndicazioni(indicazioni);
                terapia.setDataInizio(dataInizio);
                terapia.setDataFine(dataFine);
                terapiaService.modificaTerapia(terapia, medicoLoggato.getIDUtente());
            } else {
                terapiaService.assegnaTerapia(pazienteSelezionato.getIDUtente(), medicoLoggato.getIDUtente(), nomeFarmaco, quantita, frequenza, indicazioni, dataInizio, dataFine);
            }
            showAlert("Successo", "Terapia salvata con successo.");
            pazienteSelezionato(pazienteSelezionato); // Ricarica i dati
        } catch (TherapyException e) {
            showAlert("Errore", "Errore durante il salvataggio della terapia.");
            e.printStackTrace();
        }
    }

    private void salvaCondizione() {
        String tipo = tipoCondizioneComboBox.getValue();
        String descrizione = descrizioneCondizioneTextArea.getText();
        String periodo = periodoCondizioneTextArea.getText();

        if (tipo == null || descrizione.isEmpty()) {
            showAlert("Errore", "Compilare tutti i campi obbligatori.");
            return;
        }

        Object selected = condizioneComboBox.getSelectionModel().getSelectedItem();
        try {
            if (selected instanceof CondizioniPaziente) {
                CondizioniPaziente condizione = (CondizioniPaziente) selected;
                condizione.setTipo(tipo);
                condizione.setDescrizione(descrizione);
                condizione.setPeriodo(periodo);
                medicoService.updateCondizioniPaziente(medicoLoggato.getIDUtente(), condizione);
            } else {
                medicoService.addCondizioniPaziente(medicoLoggato.getIDUtente(), pazienteSelezionato.getIDUtente(), tipo, descrizione, periodo, LocalDate.now());
            }
            showAlert("Successo", "Condizione salvata con successo.");
            pazienteSelezionato(pazienteSelezionato); // Ricarica i dati
        } catch (MedicoServiceException e) {
            showAlert("Errore", "Errore durante il salvataggio della condizione.");
            e.printStackTrace();
        }
    }

    private void eliminaTerapia() {
        Object selected = terapiaComboBox.getSelectionModel().getSelectedItem();
        if (selected instanceof Terapia) {
            Terapia terapia = (Terapia) selected;
            if (showConfirmationDialog("Sei sicuro di voler eliminare questa terapia?")) {
                try {
                    terapiaService.eliminaTerapia(terapia.getIDTerapia(), medicoLoggato.getIDUtente(), pazienteSelezionato.getIDUtente());
                    showAlert("Successo", "Terapia eliminata con successo.");
                    pazienteSelezionato(pazienteSelezionato); // Ricarica i dati
                } catch (TherapyException e) {
                    showAlert("Errore", "Errore durante l'eliminazione della terapia.");
                    e.printStackTrace();
                }
            }
        }
    }

    private void eliminaCondizione() {
        Object selected = condizioneComboBox.getSelectionModel().getSelectedItem();
        if (selected instanceof CondizioniPaziente) {
            CondizioniPaziente condizione = (CondizioniPaziente) selected;
            if (showConfirmationDialog("Sei sicuro di voler eliminare questa condizione?")) {
                try {
                    medicoService.eliminaCondizione(condizione.getIDCondizione(), medicoLoggato.getIDUtente(), pazienteSelezionato.getIDUtente());
                    showAlert("Successo", "Condizione eliminata con successo.");
                    pazienteSelezionato(pazienteSelezionato); // Ricarica i dati
                } catch (MedicoServiceException e) {
                    showAlert("Errore", "Errore durante l'eliminazione della condizione.");
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean showConfirmationDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma");
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void handleLogout(ActionEvent actionEvent) {
        if (notificationScheduler != null && !notificationScheduler.isShutdown()) {
            notificationScheduler.shutdownNow();
        }

        if(!showConfirmationDialog("Sei sicuro di voler effettuare il logout?")) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/univr/telemedicina/gui/fxml/login.fxml"));
            Parent root = loader.load();
            LoginController loginController = loader.getController();

            Stage stage = (Stage) nameLable.getScene().getWindow();
            Scene scene = new Scene(root, 1440, 1024);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeNotifications() {
        notificationScheduler = Executors.newSingleThreadScheduledExecutor();
        notificationScheduler.scheduleAtFixedRate(this::checkNotifications, 0, 10, TimeUnit.SECONDS);
    }

    private void checkNotifications() {
        if (medicoLoggato == null) {
            return;
        }
        try {
            List<Notifica> newNotifications = notificheService.read(medicoLoggato.getIDUtente());
            Platform.runLater(() -> updateNotificationBell(newNotifications));
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    private void updateNotificationBell(List<Notifica> notifications) {
        this.allNotifications = notifications;
        notificationButton.getStyleClass().removeAll("notification-yellow", "notification-orange", "notification-red");

        if (notifications == null || notifications.isEmpty()) {
            return;
        }

        int maxPriority = notifications.stream()
                .filter(n -> n.getLetta() == 0) // Considera solo le non lette
                .mapToInt(Notifica::getPriorita)
                .max()
                .orElse(0);

        switch (maxPriority) {
            case 1:
                notificationButton.getStyleClass().add("notification-yellow");
                break;
            case 2:
                notificationButton.getStyleClass().add("notification-orange");
                break;
            case 3:
                notificationButton.getStyleClass().add("notification-red");
                break;
            default:
                break;
        }
    }

    @FXML
    private void handleNotificationClick(ActionEvent event) {
        if (allNotifications == null || allNotifications.isEmpty()) {
            showAlert("Notifiche", "Nessuna notifica presente.");
            return;
        }

        Popup popup = createNotificationsPopup();
        Point2D buttonPos = notificationButton.localToScreen(0, notificationButton.getHeight());
        popup.show(notificationButton.getScene().getWindow(), buttonPos.getX()-150, buttonPos.getY());

        markNotificationsAsRead(allNotifications);
    }

    private Popup createNotificationsPopup() {
        Popup popup = new Popup();
        popup.setAutoHide(true);

        ListView<Notifica> listView = new ListView<>();
        listView.setItems(FXCollections.observableArrayList(allNotifications));
        listView.setCellFactory(param -> new NotificationListCell());

        VBox popupContent = new VBox(listView);
        popupContent.getStyleClass().add("notification-popup");
        popup.getContent().add(popupContent);

        return popup;
    }

    private void markNotificationsAsRead(List<Notifica> notifications) {
        List<Notifica> unread = notifications.stream()
                .filter(n -> n.getLetta() == 0)
                .collect(Collectors.toList());

        if (!unread.isEmpty()) {
            for (Notifica notifica : unread) {
                try {
                    notificheService.setNotificaLetta(notifica.getIdNotifica());
                } catch (DataAccessException e) {
                    e.printStackTrace();
                    showAlert("Errore", "Impossibile segnare la notifica come letta: " + notifica.getTitolo());
                }
            }
            // Ricarica le notifiche per aggiornare lo stato visivo del campanello
            checkNotifications();
        }
    }

    private static class NotificationListCell extends ListCell<Notifica> {
        private final HBox topHBox = new HBox(5);
        private final Circle priorityCircle = new Circle(5);
        private final Label titleLabel = new Label();
        private final Label messageLabel = new Label();
        private final Label timestampLabel = new Label();
        private final VBox contentVBox = new VBox(5);

        public NotificationListCell() {
            titleLabel.getStyleClass().add("notification-title");
            messageLabel.getStyleClass().add("notification-message");
            messageLabel.setWrapText(true);
            timestampLabel.getStyleClass().add("notification-timestamp");

            topHBox.setAlignment(Pos.CENTER_LEFT);
            topHBox.getChildren().addAll(priorityCircle, titleLabel);
            contentVBox.getChildren().addAll(topHBox, messageLabel, timestampLabel);
            contentVBox.setPadding(new Insets(5));
        }

        @Override
        protected void updateItem(Notifica item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                titleLabel.setText(item.getTitolo());
                messageLabel.setText(item.getMessaggio());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                timestampLabel.setText(item.getTimestamp().format(formatter));

                if (item.getLetta() == 0) {
                    priorityCircle.setVisible(true);
                    switch (item.getPriorita()) {
                        case 1: priorityCircle.setFill(Color.YELLOW); break;
                        case 2: priorityCircle.setFill(Color.ORANGE); break;
                        case 3: priorityCircle.setFill(Color.RED); break;
                        default: priorityCircle.setVisible(false); break;
                    }
                } else {
                    priorityCircle.setVisible(false);
                }
                setGraphic(contentVBox);
            }
        }
    }
}