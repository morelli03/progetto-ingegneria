package org.univr.telemedicina.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.univr.telemedicina.dao.*;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.*;
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
    private TerapiaDAO terapiaDAO;
    private Utente medico;
    private Utente paziente;

    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseManager.setURL("jdbc:sqlite:test.sqlite");
        terapiaDAO = new TerapiaDAO();
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
            stmt.executeUpdate("DELETE FROM Terapie");
            stmt.executeUpdate("DELETE FROM AssunzioniFarmaci");
        }
    }

    @Test
    void testCheckGlicemiaAnormaleDopoPasti() throws DataAccessException {
        RilevazioneGlicemia rilevazione = new RilevazioneGlicemia(paziente.getIDUtente(), 200, LocalDateTime.now(), "Dopo cena");
        monitorService.checkGlicemia(rilevazione);
        List<Notifica> notifiche = notificheDAO.leggiNotifichePerId(medico.getIDUtente());
        assertEquals(1, notifiche.size());
        assertTrue(notifiche.get(0).getMessaggio().contains("valore glicemico anormale dopo i pasti"));
    }

    @Test
    void testCheckGlicemiaAnormalePrimaPasti() throws DataAccessException {
        RilevazioneGlicemia rilevazione = new RilevazioneGlicemia(paziente.getIDUtente(), 70, LocalDateTime.now(), "Prima pranzo");
        monitorService.checkGlicemia(rilevazione);
        List<Notifica> notifiche = notificheDAO.leggiNotifichePerId(medico.getIDUtente());
        assertEquals(1, notifiche.size());
        assertTrue(notifiche.get(0).getMessaggio().contains("valore glicemico anormale prima dei pasti"));
    }

    @Test
    void testCheckGlicemiaNormale() throws DataAccessException {
        RilevazioneGlicemia rilevazione = new RilevazioneGlicemia(paziente.getIDUtente(), 100, LocalDateTime.now(), "Prima pranzo");
        monitorService.checkGlicemia(rilevazione);
        List<Notifica> notifiche = notificheDAO.leggiNotifichePerId(medico.getIDUtente());
        assertTrue(notifiche.isEmpty());
    }

    @Test
    void testCheckFarmaci3DailyNotificaMedico() throws DataAccessException {
        // Crea una terapia per il paziente
        Terapia terapia = new Terapia(paziente.getIDUtente(), medico.getIDUtente(), "Test Farmaco", "10mg", 1, "Test Indicazioni", LocalDate.now().minusDays(5), LocalDate.now().plusDays(5));
        terapiaDAO.assignTherapy(terapia);

        // Nessuna assunzione farmaci per 3 giorni
        monitorService.checkFarmaci3Daily();

        List<Notifica> notifiche = notificheDAO.leggiNotifichePerId(medico.getIDUtente());
        assertEquals(1, notifiche.size());
        assertTrue(notifiche.get(0).getMessaggio().contains("non è stato costante nella terapia per 3 giorni."));
    }
}