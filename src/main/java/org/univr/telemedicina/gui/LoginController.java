package org.univr.telemedicina.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.univr.telemedicina.dao.UtenteDAO;
import org.univr.telemedicina.exception.AuthServiceException;
import org.univr.telemedicina.model.Utente;
import org.univr.telemedicina.service.AuthService;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

public class LoginController {

    // inietta gli elementi dall'fxml nel codice java
    // il nome della variabile deve corrispondere all'fx:id nel file fxml
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    // riferimenti ai servizi di backend
    private AuthService authService;

    // il costruttore viene chiamato prima che la ui sia visibile
    public LoginController() {
        // inizializza i servizi necessari per l'autenticazione
        // questa è la "dependency injection" manuale
        UtenteDAO utenteDao = new UtenteDAO();
        // nota bscryptpasswordencoder non ha stato quindi è sicuro crearne uno nuovo
        this.authService = new AuthService(utenteDao, new BCryptPasswordEncoder());
    }
    
    // questo metodo viene chiamato quando si preme il pulsante "login"
    // il nome corrisponde a quello definito in onaction nell'fxml
    @FXML
    private void handleLoginButtonAction(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        // 1 validazione base dell'input
        if (email.isEmpty() || password.isEmpty()) {
            showError("email e password non possono essere vuoti");
            return;
        }

        try {
            // 2 chiama il tuo authservice per verificare le credenziali
            Optional<Utente> utenteAutenticato = authService.verificaPassword(email, password);

            // 3 controlla il risultato
            if (utenteAutenticato.isPresent()) {
                // login riuscito!
                System.out.println("login effettuato con successo per l'utente " + utenteAutenticato.get().getEmail());
                showError(""); // pulisce eventuali errori precedenti

                // ora naviga alla dashboard
                navigateToDashboard(utenteAutenticato.get());

            } else {
                // credenziali non valide
                showError("email o password non corretti");
            }

        } catch (AuthServiceException e) {
            // errore del server/database
            showError("errore del server riprova più tardi");
            System.err.println("errore di autenticazione " + e.getMessage());
        }
    }


    // naviga alla dashboard corretta in base al ruolo dell'utente
    // @param utente l'utente autenticato
    private void navigateToDashboard(Utente utente) {

        // controlla il ruolo dell'utente e carica la dashboard appropriata
        if(Objects.equals(utente.getRuolo(), "Medico")){
            try{
                // **passo 1 carica il nuovo fxml**
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/univr/telemedicina/gui/fxml/dashboardmedico.fxml"));
                Parent root = loader.load();

                // **passo 2 passa i dati al nuovo controller (fondamentale)**
                // prima ottieni il controller della dashboard
                DashboardMedicoController dashboardController = loader.getController();
                // poi chiama un metodo per passare l'oggetto utente
                dashboardController.initData(utente);

                // **passo 3 ottieni la finestra e cambia scena**
                // ottieni la finestra (stage) da un qualsiasi elemento della ui come emailfield
                Stage stage = (Stage) emailField.getScene().getWindow();

                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("dashboard");
                stage.show();
            } catch (IOException e) {
                showError("errore critico impossibile caricare la dashboard");
                e.printStackTrace();
            }
        } else if (Objects.equals(utente.getRuolo(), "Paziente")){
            try{
                // **passo 1 carica il nuovo fxml**
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/univr/telemedicina/gui/fxml/dashboardpaziente.fxml"));
                Parent root = loader.load();

                // **passo 2 passa i dati al nuovo controller (fondamentale)**
                // prima ottieni il controller della dashboard
                DashboardPazienteController dashboardController = loader.getController();
                // poi chiama un metodo per passare l'oggetto utente
                dashboardController.initData(utente);

                // **passo 3 ottieni la finestra e cambia scena**
                // ottieni la finestra (stage) da un qualsiasi elemento della ui come emailfield
                Stage stage = (Stage) emailField.getScene().getWindow();

                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("dashboard");
                stage.show();
            } catch (IOException e) {
                showError("errore critico impossibile caricare la dashboard");
                e.printStackTrace();
            }
        }


    }

    // gestisce il click sul pulsante "contatta amministrazione"
    // apre il client di posta predefinito su un thread separato per non bloccare la ui
    @FXML
    private void contattaAmministrazione() {
        // eseguiamo tutta l'operazione in un nuovo thread
        new Thread(() -> {
            String emailDestinatario = "amministrazione@telemedicina.it";
            String oggetto = "richiesta assistenza account";
            String corpoMail = "buongiorno,\n\nscrivo per richiedere assistenza riguardo il mio account.\n\ncordiali saluti,\n[il tuo nome]";

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MAIL)) {
                try {
                    String uriString = String.format("mailto:%s?subject=%s&body=%s",
                            emailDestinatario,
                            URLEncoder.encode(oggetto, StandardCharsets.UTF_8).replace("+", "%20"),
                            URLEncoder.encode(corpoMail, StandardCharsets.UTF_8).replace("+", "%20")
                    );

                    URI mailto = new URI(uriString);
                    Desktop.getDesktop().mail(mailto);

                } catch (Exception e) {
                    e.printStackTrace();
                    // se si verifica un errore aggiorna la ui nel thread corretto usando platform.runlater
                    Platform.runLater(() -> showError("errore impossibile aprire il client di posta"));
                }
            } else {
                // anche questo aggiornamento della ui deve usare platform.runlater
                Platform.runLater(() -> showError("funzionalità non supportata su questo sistema"));
            }
        }).start(); // avvia il thread
    }

    // metodo di utilità per mostrare un messaggio di errore all'utente
    private void showError(String message) {
        if (message == null || message.isEmpty()) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        } else {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }
}