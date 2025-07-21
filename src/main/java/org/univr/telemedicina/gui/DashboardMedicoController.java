package org.univr.telemedicina.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.univr.telemedicina.model.Utente;

public class DashboardMedicoController {

    // Aggiungi un campo per memorizzare l'utente che ha fatto il login
    private Utente medicoLoggato;

    @FXML
    private Label benvenutoLabel; // Esempio: una label per mostrare il nome

    // ... altri elementi @FXML

    /**
     * Metodo per inizializzare il controller con i dati dell'utente.
     * Questo metodo viene chiamato dal LoginController.
     * @param utente L'utente che ha effettuato l'accesso.
     */
    public void initData(Utente utente) {
        this.medicoLoggato = utente;

        // Ora puoi usare i dati dell'utente per popolare la dashboard
        System.out.println("Dashboard caricata per il medico: " + medicoLoggato.getEmail());

        // Esempio: aggiornare un'etichetta di benvenuto
        // benvenutoLabel.setText("Benvenuto, Dott. " + medicoLoggato.getCognome());
    }

    @FXML
    public void initialize() {
        // Il codice qui viene eseguito PRIMA di initData.
        // Usalo per impostazioni generali della UI.
    }
}