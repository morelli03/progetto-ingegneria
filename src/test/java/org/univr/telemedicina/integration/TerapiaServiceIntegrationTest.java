package org.univr.telemedicina.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.univr.telemedicina.dao.*;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.exception.TherapyException;
import org.univr.telemedicina.model.LogOperazione;
import org.univr.telemedicina.model.Paziente;
import org.univr.telemedicina.model.Terapia;
import org.univr.telemedicina.model.Utente;
import org.univr.telemedicina.service.TerapiaService;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TerapiaServiceIntegrationTest {

    private TerapiaService terapiaService;
    private TerapiaDAO terapiaDAO;
    private LogOperazioniDAO logOperazioniDAO;
    private UtenteDAO utenteDAO;
    private PazientiDAO pazientiDAO;
    private Utente medico;
    private Utente paziente;

    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseManager.setURL("jdbc:sqlite:test.sqlite");

        // DAOs
        terapiaDAO = new TerapiaDAO();
        logOperazioniDAO = new LogOperazioniDAO();
        utenteDAO = new UtenteDAO();
        pazientiDAO = new PazientiDAO();

        // Service
        terapiaService = new TerapiaService(terapiaDAO, logOperazioniDAO);

        // Creazione medico e paziente
        medico = new Utente(0, "medico@example.com", "password", "Medico", "Test", "Medico", LocalDate.now());
        paziente = new Utente(0, "paziente@example.com", "password", "Paziente", "Test", "Paziente", LocalDate.now());

        medico = utenteDAO.create(medico);
        paziente = utenteDAO.create(paziente);

        // Associazione paziente a medico
        Paziente pazienteInfo = new Paziente(paziente.getIDUtente(), medico.getIDUtente());
        pazientiDAO.create(pazienteInfo);
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM Utenti");
            stmt.executeUpdate("DELETE FROM Pazienti");
            stmt.executeUpdate("DELETE FROM Terapie");
            stmt.executeUpdate("DELETE FROM LogOperazioni");
        }
    }

    @Test
    void testAssegnaTerapia() throws TherapyException, DataAccessException {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(10);

        terapiaService.assegnaTerapia(paziente.getIDUtente(), medico.getIDUtente(), "Test Farmaco", "10mg", 1, "Test Indicazioni", startDate, endDate);

        // Verifica che la terapia sia stata creata
        List<Terapia> terapie = terapiaDAO.listTherapiesByPatId(paziente.getIDUtente());
        assertNotNull(terapie);
        assertEquals(1, terapie.size());

        Terapia terapia = terapie.get(0);
        assertEquals("Test Farmaco", terapia.getNomeFarmaco());
        assertEquals(paziente.getIDUtente(), terapia.getIDPaziente());

        // Verifica che il log sia stato creato
        List<LogOperazione> logs = logOperazioniDAO.findLogsByPazienteId(paziente.getIDUtente());
        assertNotNull(logs);
        assertEquals(1, logs.size());

        LogOperazione log = logs.get(0);
        assertEquals("assegna terapia", log.getTipoOperazione());
        assertTrue(log.getDescrizioneOperazione().contains("prescritto il farmaco"));
    }
}
