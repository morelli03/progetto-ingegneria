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
import java.util.Optional;

public class LoginController {

    // Inietta gli elementi dall'FXML nel codice Java.
    // Il nome della variabile DEVE corrispondere all'fx:id nel file FXML.
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    // Riferimenti ai servizi di backend
    private AuthService authService;

    // Il costruttore viene chiamato prima che la UI sia visibile
    public LoginController() {
        // Inizializza i servizi necessari per l'autenticazione.
        // Questa è la "Dependency Injection" manuale.
        UtenteDAO utenteDao = new UtenteDAO();
        // Nota: BCryptPasswordEncoder non ha stato, quindi è sicuro crearne uno nuovo.
        this.authService = new AuthService(utenteDao, new BCryptPasswordEncoder());
    }
    
    /**
     * Questo metodo viene chiamato quando si preme il pulsante "Login".
     * Il nome corrisponde a quello definito in onAction nell'FXML.
     */
    @FXML
    private void handleLoginButtonAction(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        // 1. Validazione base dell'input
        if (email.isEmpty() || password.isEmpty()) {
            showError("Email e password non possono essere vuoti.");
            return;
        }

        try {
            // 2. Chiama il tuo AuthService per verificare le credenziali
            Optional<Utente> utenteAutenticato = authService.verificaPassword(email, password);

            // 3. Controlla il risultato
            if (utenteAutenticato.isPresent()) {
                // Login riuscito!
                System.out.println("Login effettuato con successo per l'utente: " + utenteAutenticato.get().getEmail());
                showError(""); // Pulisce eventuali errori precedenti

                // Ora, naviga alla dashboard
                navigateToDashboard(utenteAutenticato.get());

            } else {
                // Credenziali non valide
                showError("Email o password non corretti.");
            }

        } catch (AuthServiceException e) {
            // Errore del server/database
            showError("Errore del server. Riprova più tardi.");
            System.err.println("Errore di autenticazione: " + e.getMessage());
        }
    }


    /**
     * Naviga alla dashboard corretta in base al ruolo dell'utente.
     * @param utente L'utente autenticato.
     */
    private void navigateToDashboard(Utente utente) {
        try {
            // **PASSO 1: Carica il nuovo FXML**
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/univr/telemedicina/gui/fxml/dashboardmedico.fxml"));
            Parent root = loader.load();

            // **PASSO 2: Passa i dati al nuovo controller (FONDAMENTALE)**
            // Prima ottieni il controller della dashboard
            DashboardMedicoController dashboardController = loader.getController();
            // Poi chiama un metodo per passare l'oggetto Utente
            dashboardController.initData(utente);

            // **PASSO 3: Ottieni la finestra e cambia scena**
            // Ottieni la finestra (Stage) da un qualsiasi elemento della UI, come emailField
            Stage stage = (Stage) emailField.getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Dashboard Medico");
            stage.show();

        } catch (IOException e) {
            showError("Errore critico: Impossibile caricare la dashboard.");
            e.printStackTrace();
        }
    }

    /**
     * Gestisce il click sul pulsante "Contatta amministrazione".
     * Apre il client di posta predefinito dell'utente con una mail precompilata.
     */
    /**
     * Gestisce il click sul pulsante "Contatta amministrazione".
     * Apre il client di posta predefinito su un thread separato per non bloccare la UI.
     */
    @FXML
    private void contattaAmministrazione() {
        // Eseguiamo tutta l'operazione in un nuovo thread.
        new Thread(() -> {
            String emailDestinatario = "amministrazione@telemedicina.it";
            String oggetto = "Richiesta Assistenza Account";
            String corpoMail = "Buongiorno,\n\nScrivo per richiedere assistenza riguardo il mio account.\n\nCordiali saluti,\n[Il tuo Nome]";

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
                    // Se si verifica un errore, aggiorna la UI nel thread corretto usando Platform.runLater
                    Platform.runLater(() -> showError("Errore: Impossibile aprire il client di posta."));
                }
            } else {
                // Anche questo aggiornamento della UI deve usare Platform.runLater
                Platform.runLater(() -> showError("Funzionalità non supportata su questo sistema."));
            }
        }).start(); // Avvia il thread
    }

    /**
     * Metodo di utilità per mostrare un messaggio di errore all'utente.
     */
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