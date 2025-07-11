package org.univr.telemedicina.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

import java.io.IOException;
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

    /**
     * Carica la scena della dashboard e la mostra.
     * @param utente L'utente che ha effettuato il login.
     */
    private void navigateToDashboard(Utente utente) {
        try {
            // Carica il file FXML della dashboard (dovrai crearlo in seguito)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/univr/telemedicina/fxml/dashboard.fxml"));
            Scene scene = new Scene(loader.load());

            // (Opzionale ma raccomandato) Passa i dati dell'utente al controller della dashboard
            // DashboardController controller = loader.getController();
            // controller.initData(utente);

            // Prendi lo Stage (la finestra) corrente e imposta la nuova scena
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle("Dashboard");
            stage.setScene(scene);
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossibile caricare la dashboard.");
        }
    }
}