package org.univr.telemedicina.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.univr.telemedicina.model.Paziente;
import org.univr.telemedicina.model.Utente;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PazientiDAOTest {
    private PazientiDAO pazientiDAO;
    private UtenteDAO utenteDAO;
    private Utente medico;
    private Utente paziente;

    // Questo metodo verrà eseguito DOPO ogni test
    @AfterEach
    void tearDown() {
        // Svuotiamo le tabelle per garantire l'isolamento dei test
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // L'ordine è importante per via delle chiavi esterne (foreign keys)!
            // Inizia dalle tabelle che dipendono dalle altre.
            stmt.execute("DELETE FROM AssunzioniFarmaci");
            stmt.execute("DELETE FROM RilevazioniGlicemia");
            stmt.execute("DELETE FROM CondizioniPaziente");
            stmt.execute("DELETE FROM LogOperazioni");
            stmt.execute("DELETE FROM Notifiche");
            stmt.execute("DELETE FROM Terapie");
            stmt.execute("DELETE FROM Pazienti");
            stmt.execute("DELETE FROM Utenti");

        } catch (Exception e) {
            System.err.println("Impossibile pulire il database di test: " + e.getMessage());
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        pazientiDAO = new PazientiDAO();
        utenteDAO = new UtenteDAO();
        medico = utenteDAO.create(new Utente(0, "medico.pazienti@email.com", "pass", "Medico", "Pazienti", "Medico", LocalDate.now()));
        paziente = utenteDAO.create(new Utente(0, "paziente.pazienti@email.com", "pass", "Paziente", "Pazienti", "Paziente", LocalDate.now()));
    }

    @Test
    void testCreateAndFindPazientiByMedId() throws Exception {
        // ARRANGE
        Paziente associazione = new Paziente(paziente.getIDUtente(), medico.getIDUtente());

        // ACT
        pazientiDAO.create(associazione);
        List<Utente> result = pazientiDAO.findPazientiByMedId(medico.getIDUtente());

        // ASSERT
        assertEquals(1, result.size());
        assertEquals(paziente.getIDUtente(), result.get(0).getIDUtente());
    }

    @Test
    void testGetMedicoRiferimentoByPazienteId() throws Exception {
        // ARRANGE
        pazientiDAO.create(new Paziente(paziente.getIDUtente(), medico.getIDUtente()));

        // ACT
        Optional<Integer> result = pazientiDAO.getMedicoRiferimentoByPazienteId(paziente.getIDUtente());

        // ASSERT
        assertTrue(result.isPresent());
        assertEquals(medico.getIDUtente(), result.get().intValue());
    }

    @Test
    void testFindNameById() throws Exception {
        // ACT
        String nomeCompleto = pazientiDAO.findNameById(paziente.getIDUtente());

        // ASSERT
        assertEquals("Paziente Pazienti", nomeCompleto);
    }
}