package org.univr.telemedicina.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.univr.telemedicina.model.LogOperazione;
import org.univr.telemedicina.model.Utente;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogOperazioniDAOTest {

    private LogOperazioniDAO logOperazioniDAO;
    private UtenteDAO utenteDAO;
    private Utente medico;
    private Utente paziente;

    @BeforeEach
    void setUp() throws Exception {
        DatabaseManager.setURL("jdbc:sqlite:test.sqlite");
        logOperazioniDAO = new LogOperazioniDAO();
        utenteDAO = new UtenteDAO();
        LocalDate dataNascita = LocalDate.of(1975, 10, 20);
        medico = utenteDAO.create(new Utente(0, "medico.log@email.com", "pass", "Medico", "Log", "Medico", dataNascita));
        paziente = utenteDAO.create(new Utente(0, "paziente.log@email.com", "pass", "Paziente", "Log", "Paziente", dataNascita));
    }

    @AfterEach
    void tearDown() {
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM LogOperazioni");
            stmt.execute("DELETE FROM Utenti");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCreateAndGetAllLog() throws Exception {
        // ARRANGE
        LogOperazione log = new LogOperazione(medico.getIDUtente(), paziente.getIDUtente(), "TEST", "Operazione di test", LocalDateTime.now());
        LogOperazione log2 = new LogOperazione(medico.getIDUtente(), paziente.getIDUtente(), "TEST", "Operazione di test2", LocalDateTime.now());
        LogOperazione log3 = new LogOperazione(medico.getIDUtente(), paziente.getIDUtente(), "TEST", "Operazione di test3", LocalDateTime.now());
        LogOperazione log4 = new LogOperazione(medico.getIDUtente(), paziente.getIDUtente(), "TEST", "Operazione di test4", LocalDateTime.now());

        // ACT
        logOperazioniDAO.createLog(log);
        logOperazioniDAO.createLog(log2);
        logOperazioniDAO.createLog(log3);
        logOperazioniDAO.createLog(log4);
        List<LogOperazione> logs = logOperazioniDAO.getAllLog();

        // stampare tutte le operazioni trovate
        for (LogOperazione l : logs) {
            System.out.println("ID: " + l.getIDLog() + ", Tipo: " + l.getTipoOperazione() + ", Descrizione: " + l.getDescrizioneOperazione() + ", Timestamp: " + l.getTimestamp());
        }

        // ASSERT
        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().anyMatch(l -> l.getDescrizioneOperazione().equals("Operazione di test")));
    }

    @Test
    void testFindLogsByPazienteId() throws Exception {
        // ARRANGE
        LogOperazione log1 = new LogOperazione(medico.getIDUtente(), paziente.getIDUtente(), "VISITA", "Visita di controllo", LocalDateTime.now());
        logOperazioniDAO.createLog(log1);

        // ACT
        List<LogOperazione> result = logOperazioniDAO.findLogsByPazienteId(paziente.getIDUtente());

        // ASSERT
        assertEquals(1, result.size());
        assertEquals("VISITA", result.get(0).getTipoOperazione());
    }

    @Test
    void testFindLogsByMedicoId() throws Exception {
        // ARRANGE
        LogOperazione log1 = new LogOperazione(medico.getIDUtente(), paziente.getIDUtente(), "VISITA", "Visita di controllo", LocalDateTime.now());
        logOperazioniDAO.createLog(log1);

        // ACT
        List<LogOperazione> result = logOperazioniDAO.findLogsByMedicoId(medico.getIDUtente());

        // ASSERT
        assertEquals(1, result.size());
        assertEquals("VISITA", result.get(0).getTipoOperazione());
    }
}