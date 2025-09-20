package org.univr.telemedicina.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.univr.telemedicina.dao.DatabaseManager;
import org.univr.telemedicina.dao.NotificheDAO;
import org.univr.telemedicina.dao.UtenteDAO;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.Notifica;
import org.univr.telemedicina.model.Utente;
import org.univr.telemedicina.service.NotificheService;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificheServiceIntegrationTest {

    private NotificheService notificheService;
    private NotificheDAO notificheDAO;
    private UtenteDAO utenteDAO;
    private Utente utente;

    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseManager.setURL("jdbc:sqlite:test.sqlite");
        notificheDAO = new NotificheDAO();
        utenteDAO = new UtenteDAO();
        notificheService = new NotificheService(notificheDAO);

        // Creazione utente per il test
        utente = new Utente(0, "test@example.com", "password", "Test", "User", "Paziente", LocalDate.now());
        utente = utenteDAO.create(utente);
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM Utenti");
            stmt.executeUpdate("DELETE FROM Notifiche");
        }
    }

    @Test
    void testSendAndReadNotifica() throws DataAccessException {
        notificheService.send(utente.getIDUtente(), 1, "Test Title", "Test Message", "Test Type");

        List<Notifica> notifiche = notificheService.read(utente.getIDUtente());

        assertNotNull(notifiche);
        assertEquals(1, notifiche.size());

        Notifica notifica = notifiche.get(0);
        assertEquals(utente.getIDUtente(), notifica.getIdDestinatario());
        assertEquals("Test Title", notifica.getTitolo());
        assertEquals("Test Message", notifica.getMessaggio());
        assertEquals(0, notifica.getLetta());
    }

    @Test
    void testSetNotificaLetta() throws DataAccessException {
        notificheService.send(utente.getIDUtente(), 1, "Test Title", "Test Message", "Test Type");
        List<Notifica> notifiche = notificheDAO.leggiNotifichePerId(utente.getIDUtente());
        Notifica notifica = notifiche.get(0);

        notificheService.setNotificaLetta(notifica.getIdNotifica());

        List<Notifica> updatedNotifiche = notificheDAO.leggiNotifichePerId(utente.getIDUtente());
        Notifica updatedNotifica = updatedNotifiche.get(0);

        assertTrue(updatedNotifica.getLetta() == 1);
    }

    @Test
    void testReadNessunaNotifica() throws DataAccessException {
        List<Notifica> notifiche = notificheService.read(999); // ID utente non esistente
        assertNotNull(notifiche);
        assertTrue(notifiche.isEmpty());
    }
}
