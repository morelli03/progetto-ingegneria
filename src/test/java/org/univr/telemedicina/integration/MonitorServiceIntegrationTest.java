package org.univr.telemedicina.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.univr.telemedicina.dao.*;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.Notifica;
import org.univr.telemedicina.model.Paziente;
import org.univr.telemedicina.model.RilevazioneGlicemia;
import org.univr.telemedicina.model.Utente;
import org.univr.telemedicina.service.MonitorService;
import org.univr.telemedicina.service.NotificheService;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MonitorServiceIntegrationTest {

    private MonitorService monitorService;
    private NotificheDAO notificheDAO;
    private UtenteDAO utenteDAO;
    private PazientiDAO pazientiDAO;
    private Utente medico;
    private Utente paziente;

    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseManager.setURL("jdbc:sqlite:test.sqlite");
        TerapiaDAO terapiaDAO = new TerapiaDAO();
        AssunzioneFarmaciDAO assunzioneFarmaciDAO = new AssunzioneFarmaciDAO();
        notificheDAO = new NotificheDAO();
        pazientiDAO = new PazientiDAO();
        utenteDAO = new UtenteDAO();

        NotificheService notificheService = new NotificheService(notificheDAO);
        monitorService = new MonitorService(terapiaDAO, assunzioneFarmaciDAO, notificheService, pazientiDAO);

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
            stmt.executeUpdate("DELETE FROM Notifiche");
        }
    }

    @Test
    void testCheckGlicemiaAnormale() throws DataAccessException {
        RilevazioneGlicemia rilevazione = new RilevazioneGlicemia(paziente.getIDUtente(), 200, LocalDateTime.now(), "Dopo cena");

        monitorService.checkGlicemia(rilevazione);

        List<Notifica> notifiche = notificheDAO.leggiNotifichePerId(medico.getIDUtente());

        assertNotNull(notifiche);
        assertEquals(1, notifiche.size());

        Notifica notifica = notifiche.get(0);
        assertEquals(medico.getIDUtente(), notifica.getIdDestinatario());
        assertEquals("glicemia anormale", notifica.getTitolo());
        assertTrue(notifica.getMessaggio().contains("valore glicemico anormale dopo i pasti"));
    }
}
