package org.univr.telemedicina.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import org.univr.telemedicina.dao.*;
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

    // Ti consiglio di dare un fx:id anche al MenuButton per popolarlo dinamicamente
    @FXML
    private MenuButton pazienteMenuButton;

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

        // Imposta uno stato iniziale per le label
        nameLable.setText("Nessun paziente selezionato");
        emailLable.setText("");
        dateLable.setText("");
        ageLable.setText("");
    }

    private void pazienteSelezionato(Utente paziente) {
        nameLable.setText(paziente.getNome() + " " + paziente.getCognome());
        emailLable.setText(paziente.getEmail());
        dateLable.setText(paziente.getDataNascita().toString()); // Assicurati che il formato sia corretto
        LocalDate oggi = LocalDate.now();
        long eta = ChronoUnit.YEARS.between(paziente.getDataNascita(), oggi);
        ageLable.setText(String.valueOf(eta));
    }

}