package org.univr.telemedicina.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.univr.telemedicina.model.CondizioniPaziente;
import org.univr.telemedicina.model.Utente;

import java.sql.Connection;
import java.time.LocalDate;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CondizioniPazienteDAOTest {

    private CondizioniPazienteDAO condizioniPazienteDAO;
    private UtenteDAO utenteDAO;
    private Utente paziente;

    @BeforeEach
    void setUp() throws Exception {
        DatabaseManager.setURL("jdbc:sqlite:test.sqlite");
        condizioniPazienteDAO = new CondizioniPazienteDAO();
        utenteDAO = new UtenteDAO();
        LocalDate dataNascita = LocalDate.of(1990, 5, 15);
        paziente = utenteDAO.create(new Utente(0, "paziente.condizioni@email.com", "pass", "Paziente", "Condizioni", "Paziente", dataNascita));
    }

    @AfterEach
    void tearDown() {
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM CondizioniPaziente");
            stmt.execute("DELETE FROM Utenti");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCreateAndListByIDPatId() throws Exception {
        // ARRANGE
        CondizioniPaziente condizione = new CondizioniPaziente(paziente.getIDUtente(), "Comorbidita", "Ipertensione", "pregressa", LocalDate.now());
        CondizioniPaziente condizione2 = new CondizioniPaziente(paziente.getIDUtente(), "Sintomo", "Mal di testa", "ultima settimana", LocalDate.now());

        // ACT
        condizioniPazienteDAO.create(condizione);
        condizioniPazienteDAO.create(condizione2);

        // NOTA: La query originale in listByIDPatId era errata (cercava in Terapie). 
        // Assumo che sia stata corretta per far passare il test.
        // Se la query è "SELECT * FROM CondizioniPaziente WHERE IDPaziente = ?"
        List<CondizioniPaziente> result = condizioniPazienteDAO.listByIDPatId(paziente.getIDUtente());
        System.out.println("Condizioni trovate: " + result.size());

        //stampa tutte le condizioni trovate
        for (CondizioniPaziente c : result) {
            System.out.println("ID: " + c.getIDCondizione() + ", Descrizione: " + c.getDescrizione());
        }

        // Se la query non è stata corretta, il test fallirebbe.
        // Per ora, lascio un'asserzione che verifichi il corretto funzionamento atteso.
        // Se fallisce, la causa è nel codice del DAO e non nel test.
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertEquals("Ipertensione", result.get(0).getDescrizione());
        
        // Dato l'errore noto nel DAO, testo che la chiamata non sollevi eccezioni
        assertNotNull(result, "Il risultato non dovrebbe essere nullo.");
    }
}