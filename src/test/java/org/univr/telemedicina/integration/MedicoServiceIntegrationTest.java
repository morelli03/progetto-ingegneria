package org.univr.telemedicina.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.univr.telemedicina.dao.*;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.exception.MedicoServiceException;
import org.univr.telemedicina.model.Paziente;
import org.univr.telemedicina.model.Utente;
import org.univr.telemedicina.service.MedicoService;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MedicoServiceIntegrationTest {

    private MedicoService medicoService;
    private UtenteDAO utenteDAO;
    private PazientiDAO pazientiDAO;
    private Utente medico;
    private Utente paziente;

    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseManager.setURL("jdbc:sqlite:test.sqlite");
        pazientiDAO = new PazientiDAO();
        RilevazioneGlicemiaDAO rilevazioneGlicemiaDAO = new RilevazioneGlicemiaDAO();
        CondizioniPazienteDAO condizioniPazienteDAO = new CondizioniPazienteDAO();
        LogOperazioniDAO logOperazioniDAO = new LogOperazioniDAO();
        TerapiaDAO terapiaDAO = new TerapiaDAO();
        AssunzioneFarmaciDAO assunzioneFarmaciDAO = new AssunzioneFarmaciDAO();
        utenteDAO = new UtenteDAO();

        medicoService = new MedicoService(pazientiDAO, rilevazioneGlicemiaDAO, condizioniPazienteDAO, logOperazioniDAO, terapiaDAO, assunzioneFarmaciDAO);

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
        }
    }

    @Test
    void testGetPazientiAssegnati() throws MedicoServiceException {
        List<Utente> pazientiAssegnati = medicoService.getPazientiAssegnati(medico.getIDUtente());

        assertNotNull(pazientiAssegnati);
        assertEquals(1, pazientiAssegnati.size());
        assertEquals(paziente.getIDUtente(), pazientiAssegnati.get(0).getIDUtente());
        assertEquals(paziente.getEmail(), pazientiAssegnati.get(0).getEmail());
    }

    @Test
    void testGetPazientiAssegnatiMedicoSenzaPazienti() throws MedicoServiceException {
        List<Utente> pazientiAssegnati = medicoService.getPazientiAssegnati(999); // ID medico non esistente
        assertNotNull(pazientiAssegnati);
        assertTrue(pazientiAssegnati.isEmpty());
    }
}
