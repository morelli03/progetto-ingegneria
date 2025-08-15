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
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.univr.telemedicina.dao.*;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.Notifica;
import org.univr.telemedicina.model.Utente;
import org.univr.telemedicina.service.MonitorService;
import org.univr.telemedicina.service.NotificheService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.univr.telemedicina.service.PazienteService;


public class DashboardPazienteController {

    private Utente pazienteLoggato;
    private final NotificheService notificheService = new NotificheService(new NotificheDAO());
    private final MonitorService monitorService = new MonitorService(new TerapiaDAO(), new AssunzioneFarmaciDAO(), notificheService, new PazientiDAO());
    private final PazienteService pazienteService = new PazienteService(new RilevazioneGlicemiaDAO(), monitorService, new CondizioniPazienteDAO(), new UtenteDAO(), new PazientiDAO(), new AssunzioneFarmaciDAO(), new TerapiaDAO());
    private ScheduledExecutorService notificationScheduler;
    private List<Notifica> allNotifications;

    @FXML
    private Button notificationButton;
    @FXML
    private Text nomeCognomeLabel;
    @FXML
    private TextField valoreGlicemicoField;
    @FXML
    private ChoiceBox<String> periodoChoiceBox;
    @FXML
    private DatePicker dataPicker;
    @FXML
    private TextField oraField;
    @FXML
    private TextField minutiField;

    @FXML
    public void initialize() {
        periodoChoiceBox.setItems(FXCollections.observableArrayList(
                "Prima colazione", "Dopo colazione", "Prima pranzo",
                "Dopo pranzo", "Prima cena", "Dopo cena"));
    }


    public void initData(Utente paziente){
        this.pazienteLoggato = paziente;
        nomeCognomeLabel.setText(paziente.getNome() + " " + paziente.getCognome());
        initializeNotifications();
    }

    @FXML
    public void handleNotificationClick(ActionEvent actionEvent) {
        if (allNotifications == null || allNotifications.isEmpty()) {
            showAlert("Notifiche", "Nessuna notifica presente.");
            return;
        }

        Popup popup = createNotificationsPopup();
        Point2D buttonPos = notificationButton.localToScreen(0, notificationButton.getHeight());
        popup.show(notificationButton.getScene().getWindow(), buttonPos.getX() - 150, buttonPos.getY());

        markNotificationsAsRead(allNotifications);
    }

    @FXML
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

            Stage stage = (Stage) notificationButton.getScene().getWindow();
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
        if (pazienteLoggato == null) {
            return;
        }
        try {
            List<Notifica> newNotifications = notificheService.read(pazienteLoggato.getIDUtente());
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

    public void handleLeftProfiloButton(ActionEvent actionEvent) {
    }

    public void handleRightProfiloButton(ActionEvent actionEvent) {
    }

    public void handleLeftTerapieButton(ActionEvent actionEvent) {
    }

    public void handleRightTerapieButton(ActionEvent actionEvent) {
    }

    public void handleSaveButtonMisurazioni(ActionEvent actionEvent) {
        try {
            // Input validation
            if (valoreGlicemicoField.getText().isEmpty() ||
                periodoChoiceBox.getValue() == null ||
                dataPicker.getValue() == null ||
                oraField.getText().isEmpty() ||
                minutiField.getText().isEmpty()) {
                showAlert("Errore", "Tutti i campi devono essere compilati.");
                return;
            }

            int valoreGlicemico = Integer.parseInt(valoreGlicemicoField.getText());
            String periodo = periodoChoiceBox.getValue();
            LocalDate localDate = dataPicker.getValue();
            int ora = Integer.parseInt(oraField.getText());
            int minuti = Integer.parseInt(minutiField.getText());

            // Time validation
            if (ora < 0 || ora > 23 || minuti < 0 || minuti > 59) {
                showAlert("Errore", "Ora o minuti non validi.");
                return;
            }

            // Converto in LocalDateTime
            LocalDateTime dataOra = LocalDateTime.of(localDate, LocalTime.of(ora, minuti));

            // Using the "note" field to store the "periodo"
            pazienteService.registraRilevazioneGlicemia(pazienteLoggato.getIDUtente(), valoreGlicemico, dataOra, periodo);


            showAlert("Successo", "Misurazione salvata con successo.");
            clearMisurazioniFields();
        } catch (NumberFormatException e) {
            showAlert("Errore", "Il valore glicemico, l'ora e i minuti devono essere numeri validi.");
        } catch (DataAccessException e) {
            showAlert("Errore", "Errore durante il salvataggio della misurazione nel database.");
            e.printStackTrace();
        }
    }

    private void clearMisurazioniFields() {
        valoreGlicemicoField.clear();
        periodoChoiceBox.getSelectionModel().clearSelection();
        dataPicker.setValue(null);
        oraField.clear();
        minutiField.clear();
    }

    public void handleSaveButtonAssunzioneFarmaci(ActionEvent actionEvent) {
    }

    public void handleAggModSintomoButton(ActionEvent actionEvent) {
    }

    public void handleAggModPatologiaButton(ActionEvent actionEvent) {
    }

    public void handleAggModTerapiaConcomButton(ActionEvent actionEvent) {
    }

    public void handleDeleteButtonAzioni(ActionEvent actionEvent) {
    }

    public void handleSaveButtonAzioni(ActionEvent actionEvent) {
    }
}