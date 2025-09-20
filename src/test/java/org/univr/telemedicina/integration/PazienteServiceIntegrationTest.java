package org.univr.telemedicina.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.univr.telemedicina.dao.*;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.exception.WrongAssumptionException;
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
    private TerapiaDAO terapiaDAO;
    private CondizioniPazienteDAO condizioniPazienteDAO;
    private AssunzioneFarmaciDAO assunzioneFarmaciDAO;
    private Utente medico;
    private Utente paziente;

    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseManager.setURL("jdbc:sqlite:test.sqlite");

        // DAOs
        rilevazioneGlicemiaDAO = new RilevazioneGlicemiaDAO();
        condizioniPazienteDAO = new CondizioniPazienteDAO();
        utenteDAO = new UtenteDAO();
        pazientiDAO = new PazientiDAO();
        assunzioneFarmaciDAO = new AssunzioneFarmaciDAO();
        terapiaDAO = new TerapiaDAO();
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
            stmt.executeUpdate("DELETE FROM Terapie");
            stmt.executeUpdate("DELETE FROM CondizioniPaziente");
            stmt.executeUpdate("DELETE FROM AssunzioniFarmaci");
        }
    }

    @Test
    void testRegistraRilevazioneGlicemiaAnormale() throws DataAccessException {
        LocalDateTime timestamp = LocalDateTime.now();
        pazienteService.registraRilevazioneGlicemia(paziente.getIDUtente(), 250, timestamp, "Dopo pranzo");

        List<RilevazioneGlicemia> rilevazioni = rilevazioneGlicemiaDAO.getRilevazioniByPaziente(paziente.getIDUtente());
        assertEquals(1, rilevazioni.size());
        assertEquals(250, rilevazioni.get(0).getValore());

        List<Notifica> notifiche = notificheDAO.leggiNotifichePerId(medico.getIDUtente());
        assertEquals(1, notifiche.size());
        assertEquals("glicemia anormale", notifiche.get(0).getTitolo());
    }

    @Test
    void testRegistraRilevazioneGlicemiaNormale() throws DataAccessException {
        LocalDateTime timestamp = LocalDateTime.now();
        pazienteService.registraRilevazioneGlicemia(paziente.getIDUtente(), 100, timestamp, "Dopo pranzo");

        List<RilevazioneGlicemia> rilevazioni = rilevazioneGlicemiaDAO.getRilevazioniByPaziente(paziente.getIDUtente());
        assertEquals(1, rilevazioni.size());
        assertEquals(100, rilevazioni.get(0).getValore());

        List<Notifica> notifiche = notificheDAO.leggiNotifichePerId(medico.getIDUtente());
        assertTrue(notifiche.isEmpty());
    }

    @Test
    void testRegistraAssunzioneFarmaciCorretta() throws DataAccessException, WrongAssumptionException {
        Terapia terapia = new Terapia(paziente.getIDUtente(), medico.getIDUtente(), "Test Farmaco", "10mg", 1, "Test Indicazioni", LocalDate.now(), LocalDate.now().plusDays(10));
        terapiaDAO.assignTherapy(terapia);
        Terapia newTerapia = terapiaDAO.listTherapiesByPatId(paziente.getIDUtente()).get(0);

        pazienteService.registraAssunzioneFarmaci(newTerapia, "10mg", LocalDateTime.now());

        List<AssunzioneFarmaci> assunzioni = assunzioneFarmaciDAO.leggiAssunzioniFarmaci(paziente.getIDUtente());
        assertEquals(1, assunzioni.size());
        assertEquals("10mg", assunzioni.get(0).getQuantitaAssunta());
    }

    @Test
    void testRegistraAssunzioneFarmaciSbagliata() throws DataAccessException {
        Terapia terapia = new Terapia(paziente.getIDUtente(), medico.getIDUtente(), "Test Farmaco", "10mg", 1, "Test Indicazioni", LocalDate.now(), LocalDate.now().plusDays(10));
        terapia.setIDMedico(medico.getIDUtente());
        terapiaDAO.assignTherapy(terapia);
        Terapia newTerapia = terapiaDAO.listTherapiesByPatId(paziente.getIDUtente()).get(0);

        assertThrows(WrongAssumptionException.class, () -> {
            pazienteService.registraAssunzioneFarmaci(newTerapia, "20mg", LocalDateTime.now());
        });

        List<Notifica> notifiche = notificheDAO.leggiNotifichePerId(medico.getIDUtente());
        assertEquals(1, notifiche.size());
        assertEquals("assunzione di farmaci non corretta", notifiche.get(0).getTitolo());
    }

    @Test
    void testSegnalaCondizionePaziente() throws DataAccessException {
        pazienteService.segnalaCondizionePaziente(paziente.getIDUtente(), "Sintomo", "Mal di testa", "occasionale");

        List<CondizioniPaziente> condizioni = condizioniPazienteDAO.listByIDPatId(paziente.getIDUtente());
        assertEquals(1, condizioni.size());
        assertEquals("Mal di testa", condizioni.get(0).getDescrizione());
    }

    @Test
    void testModificaCondizione() throws DataAccessException {
        CondizioniPaziente condizione = new CondizioniPaziente(paziente.getIDUtente(), "Sintomo", "Mal di testa", "occasionale", LocalDate.now());
        condizioniPazienteDAO.create(condizione);
        List<CondizioniPaziente> condizioni = condizioniPazienteDAO.listByIDPatId(paziente.getIDUtente());
        CondizioniPaziente daModificare = condizioni.get(0);
        daModificare.setDescrizione("Emicrania");

        pazienteService.modificaCondizione(daModificare);

        List<CondizioniPaziente> updatedCondizioni = condizioniPazienteDAO.listByIDPatId(paziente.getIDUtente());
        assertEquals("Emicrania", updatedCondizioni.get(0).getDescrizione());
    }

    @Test
    void testEliminaCondizione() throws DataAccessException {
        CondizioniPaziente condizione = new CondizioniPaziente(paziente.getIDUtente(), "Sintomo", "Mal di testa", "occasionale", LocalDate.now());
        condizioniPazienteDAO.create(condizione);
        List<CondizioniPaziente> condizioni = condizioniPazienteDAO.listByIDPatId(paziente.getIDUtente());
        CondizioniPaziente daEliminare = condizioni.get(0);

        pazienteService.eliminaCondizione(daEliminare.getIDCondizione());

        List<CondizioniPaziente> remainingCondizioni = condizioniPazienteDAO.listByIDPatId(paziente.getIDUtente());
        assertTrue(remainingCondizioni.isEmpty());
    }
}