package org.univr.telemedicina.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.univr.telemedicina.dao.*;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.*;
import org.univr.telemedicina.service.MonitorService;
import org.univr.telemedicina.service.NotificheService;
import org.univr.telemedicina.service.PazienteService;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PazienteServiceIntegrationTest {

    private PazienteService pazienteService;
    private RilevazioneGlicemiaDAO rilevazioneGlicemiaDAO;
    private NotificheDAO notificheDAO;
    private UtenteDAO utenteDAO;
    private PazientiDAO pazientiDAO;
    private Utente medico;
    private Utente paziente;

    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseManager.setURL("jdbc:sqlite:test.sqlite");

        // DAOs
        rilevazioneGlicemiaDAO = new RilevazioneGlicemiaDAO();
        CondizioniPazienteDAO condizioniPazienteDAO = new CondizioniPazienteDAO();
        utenteDAO = new UtenteDAO();
        pazientiDAO = new PazientiDAO();
        AssunzioneFarmaciDAO assunzioneFarmaciDAO = new AssunzioneFarmaciDAO();
        TerapiaDAO terapiaDAO = new TerapiaDAO();
        notificheDAO = new NotificheDAO();

        // Services
        NotificheService notificheService = new NotificheService(notificheDAO);
        MonitorService monitorService = new MonitorService(terapiaDAO, assunzioneFarmaciDAO, notificheService, pazientiDAO);
        pazienteService = new PazienteService(rilevazioneGlicemiaDAO, monitorService, condizioniPazienteDAO, utenteDAO, pazientiDAO, assunzioneFarmaciDAO, terapiaDAO, notificheService);

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
            stmt.executeUpdate("DELETE FROM RilevazioniGlicemia");
            stmt.executeUpdate("DELETE FROM Notifiche");
        }
    }

    @Test
    void testRegistraRilevazioneGlicemiaAnormale() throws DataAccessException {
        LocalDateTime timestamp = LocalDateTime.now();
        pazienteService.registraRilevazioneGlicemia(paziente.getIDUtente(), 250, timestamp, "Dopo pranzo");

        // Verifica che la rilevazione sia stata salvata
        List<RilevazioneGlicemia> rilevazioni = rilevazioneGlicemiaDAO.getRilevazioniByPaziente(paziente.getIDUtente());
        assertNotNull(rilevazioni);
        assertEquals(1, rilevazioni.size());
        assertEquals(250, rilevazioni.get(0).getValore());

        // Verifica che sia stata inviata una notifica al medico
        List<Notifica> notifiche = notificheDAO.leggiNotifichePerId(medico.getIDUtente());
        assertNotNull(notifiche);
        assertEquals(1, notifiche.size());
        assertEquals("glicemia anormale", notifiche.get(0).getTitolo());
    }
}
