package org.univr.telemedicina.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.univr.telemedicina.dao.*;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.exception.MedicoServiceException;
import org.univr.telemedicina.model.*;
import org.univr.telemedicina.service.MedicoService;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MedicoServiceIntegrationTest {

    private MedicoService medicoService;
    private UtenteDAO utenteDAO;
    private PazientiDAO pazientiDAO;
    private TerapiaDAO terapiaDAO;
    private RilevazioneGlicemiaDAO rilevazioneGlicemiaDAO;
    private CondizioniPazienteDAO condizioniPazienteDAO;
    private AssunzioneFarmaciDAO assunzioneFarmaciDAO;
    private LogOperazioniDAO logOperazioniDAO;
    private Utente medico;
    private Utente paziente;
    private Utente pazienteSenzaTerapia;

    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseManager.setURL("jdbc:sqlite:test.sqlite");
        pazientiDAO = new PazientiDAO();
        rilevazioneGlicemiaDAO = new RilevazioneGlicemiaDAO();
        condizioniPazienteDAO = new CondizioniPazienteDAO();
        logOperazioniDAO = new LogOperazioniDAO();
        terapiaDAO = new TerapiaDAO();
        assunzioneFarmaciDAO = new AssunzioneFarmaciDAO();
        utenteDAO = new UtenteDAO();

        medicoService = new MedicoService(pazientiDAO, rilevazioneGlicemiaDAO, condizioniPazienteDAO, logOperazioniDAO, terapiaDAO, assunzioneFarmaciDAO);

        // Creazione medico e pazienti
        medico = new Utente(0, "medico@example.com", "password", "Medico", "Test", "Medico", LocalDate.now());
        paziente = new Utente(0, "paziente@example.com", "password", "Paziente", "Test", "Paziente", LocalDate.now());
        pazienteSenzaTerapia = new Utente(0, "paziente2@example.com", "password", "Paziente2", "Test", "Paziente", LocalDate.now());


        medico = utenteDAO.create(medico);
        paziente = utenteDAO.create(paziente);
        pazienteSenzaTerapia = utenteDAO.create(pazienteSenzaTerapia);

        // Associazione pazienti a medico
        Paziente pazienteInfo = new Paziente(paziente.getIDUtente(), medico.getIDUtente());
        pazientiDAO.create(pazienteInfo);
        Paziente pazienteInfo2 = new Paziente(pazienteSenzaTerapia.getIDUtente(), medico.getIDUtente());
        pazientiDAO.create(pazienteInfo2);

        // Creazione terapia per un paziente
        Terapia terapia = new Terapia(paziente.getIDUtente(), medico.getIDUtente(), "Test Farmaco", "10mg", 1, "Test Indicazioni", LocalDate.now(), LocalDate.now().plusDays(10));
        terapiaDAO.assignTherapy(terapia);
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM Utenti");
            stmt.executeUpdate("DELETE FROM Pazienti");
            stmt.executeUpdate("DELETE FROM Terapie");
            stmt.executeUpdate("DELETE FROM RilevazioniGlicemia");
            stmt.executeUpdate("DELETE FROM CondizioniPaziente");
            stmt.executeUpdate("DELETE FROM AssunzioniFarmaci");
            stmt.executeUpdate("DELETE FROM LogOperazioni");
        }
    }

    @Test
    void testGetPazientiAssegnati() throws MedicoServiceException {
        List<Utente> pazientiAssegnati = medicoService.getPazientiAssegnati(medico.getIDUtente());

        assertNotNull(pazientiAssegnati);
        assertEquals(2, pazientiAssegnati.size());
    }

    @Test
    void testGetPazientiAssegnatiMedicoSenzaPazienti() throws MedicoServiceException {
        List<Utente> pazientiAssegnati = medicoService.getPazientiAssegnati(999); // ID medico non esistente
        assertNotNull(pazientiAssegnati);
        assertTrue(pazientiAssegnati.isEmpty());
    }

    @Test
    void testGetPazientiAttivi() throws MedicoServiceException {
        List<Utente> pazientiAttivi = medicoService.getPazientiAttivi(medico.getIDUtente());

        assertNotNull(pazientiAttivi);
        assertEquals(1, pazientiAttivi.size());
        assertEquals(paziente.getIDUtente(), pazientiAttivi.getFirst().getIDUtente());
    }

    @Test
    void testGetDatiPazienteDashboard() throws MedicoServiceException, DataAccessException {
        // Setup dati per il dashboard
        rilevazioneGlicemiaDAO.create(new RilevazioneGlicemia(paziente.getIDUtente(), 120, LocalDateTime.now(), "Prima pranzo"));
        condizioniPazienteDAO.create(new CondizioniPaziente(paziente.getIDUtente(), "Patologia", "Diabete Tipo 1", "cronica", LocalDate.now()));
        Terapia terapia = terapiaDAO.listTherapiesByPatId(paziente.getIDUtente()).getFirst();
        assunzioneFarmaciDAO.aggiungiAssunzione(new AssunzioneFarmaci(terapia.getIDTerapia(), paziente.getIDUtente(), LocalDateTime.now(), "OK"));

        PazienteDashboard dashboard = medicoService.getDatiPazienteDasboard(paziente);

        assertNotNull(dashboard);
        assertEquals(paziente.getIDUtente(), dashboard.getDatiUtente().getIDUtente());
        assertEquals(1, dashboard.getElencoRilevazioni().size());
        assertEquals(1, dashboard.getElencoTerapie().size());
        assertEquals(1, dashboard.getElencoCondizioni().size());
        assertEquals(1, dashboard.getElencoAssunzioni().size());
    }

    @Test
    void testAddCondizioniPaziente() throws MedicoServiceException, DataAccessException {
        medicoService.addCondizioniPaziente(medico.getIDUtente(), paziente.getIDUtente(), "Patologia", "Ipertensione", "cronica", LocalDate.now());

        List<CondizioniPaziente> condizioni = condizioniPazienteDAO.listByIDPatId(paziente.getIDUtente());
        assertNotNull(condizioni);
        assertEquals(1, condizioni.size());
        assertEquals("Ipertensione", condizioni.getFirst().getDescrizione());

        List<LogOperazione> logs = logOperazioniDAO.findLogsByPazienteId(paziente.getIDUtente());
        assertNotNull(logs);
        assertEquals(1, logs.size());
        assertEquals("AGGIORNAMENTO_CONDIZIONI", logs.getFirst().getTipoOperazione());
    }

    @Test
    void testUpdateCondizioniPaziente() throws MedicoServiceException, DataAccessException {
        CondizioniPaziente condizione = new CondizioniPaziente(paziente.getIDUtente(), "Patologia", "Ipertensione", "cronica", LocalDate.now());
        condizioniPazienteDAO.create(condizione);
        List<CondizioniPaziente> condizioni = condizioniPazienteDAO.listByIDPatId(paziente.getIDUtente());
        CondizioniPaziente daModificare = condizioni.getFirst();
        daModificare.setDescrizione("Ipertensione Controllata");

        medicoService.updateCondizioniPaziente(medico.getIDUtente(), daModificare);

        List<CondizioniPaziente> updatedCondizioni = condizioniPazienteDAO.listByIDPatId(paziente.getIDUtente());
        assertEquals("Ipertensione Controllata", updatedCondizioni.getFirst().getDescrizione());
    }

    @Test
    void testEliminaCondizione() throws MedicoServiceException, DataAccessException {
        CondizioniPaziente condizione = new CondizioniPaziente(paziente.getIDUtente(), "Patologia", "Ipertensione", "cronica", LocalDate.now());
        condizioniPazienteDAO.create(condizione);
        List<CondizioniPaziente> condizioni = condizioniPazienteDAO.listByIDPatId(paziente.getIDUtente());
        CondizioniPaziente daEliminare = condizioni.getFirst();

        medicoService.eliminaCondizione(daEliminare.getIDCondizione(), medico.getIDUtente(), paziente.getIDUtente());

        List<CondizioniPaziente> remainingCondizioni = condizioniPazienteDAO.listByIDPatId(paziente.getIDUtente());
        assertTrue(remainingCondizioni.isEmpty());
    }

    @Test
    void testCalcolaAderenzaGlobale() throws MedicoServiceException, DataAccessException {
        // Paziente 1: 1 assunzione su 1 giorno * 1 dose/giorno = 1 dosi
        Terapia terapia1 = terapiaDAO.listTherapiesByPatId(paziente.getIDUtente()).getFirst();
        assunzioneFarmaciDAO.aggiungiAssunzione(new AssunzioneFarmaci(terapia1.getIDTerapia(),paziente.getIDUtente(), LocalDateTime.now(), "10mg"));

        // Paziente 2: nessuna terapia

        List<Utente> pazienti = List.of(paziente, pazienteSenzaTerapia);
        double aderenza = medicoService.calcolaAderenzaGlobale(pazienti);

        // Aderenza paziente 1: 1/1 = 1.0
        // Aderenza paziente 2: 0 (nessuna terapia)
        // Aderenza globale: (1.0) / 1 = 1.0
        assertEquals(1.0, aderenza, 0.01);
    }
}