package org.univr.telemedicina.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.univr.telemedicina.model.RilevazioneGlicemia;
import org.univr.telemedicina.model.Utente;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RilevazioneGlicemiaDAOTest {
    private RilevazioneGlicemiaDAO rilevazioneGlicemiaDAO;
    private UtenteDAO utenteDAO;
    private Utente paziente;

    @BeforeEach
    void setUp() throws Exception {
        rilevazioneGlicemiaDAO = new RilevazioneGlicemiaDAO();
        utenteDAO = new UtenteDAO();
        LocalDate dataNascita = LocalDate.of(1995, 8, 25);
        paziente = utenteDAO.create(new Utente(0, "paziente.glicemia@email.com", "pass", "Paziente", "Glicemia", "Paziente", dataNascita));
    }

    @AfterEach
    void tearDown() {
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM RilevazioniGlicemia");
            stmt.execute("DELETE FROM Utenti");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCreateAndGetRilevazioniByPaziente() throws Exception {
        // ARRANGE
        LocalDateTime now = LocalDateTime.now();
        RilevazioneGlicemia rilevazione = new RilevazioneGlicemia(paziente.getIDUtente(), 120, now, "Dopo pranzo");
        RilevazioneGlicemia rilevazione2 = new RilevazioneGlicemia(paziente.getIDUtente(), 130, now.minusHours(1), "Prima colazione");
        RilevazioneGlicemia rilevazione3 = new RilevazioneGlicemia(paziente.getIDUtente(), 110, now.minusHours(2), "Prima cena");
        RilevazioneGlicemia rilevazione4 = new RilevazioneGlicemia(paziente.getIDUtente(), 140, now.minusHours(3), "Dopo cena");

        // ACT
        rilevazioneGlicemiaDAO.create(rilevazione);
        rilevazioneGlicemiaDAO.create(rilevazione2);
        rilevazioneGlicemiaDAO.create(rilevazione3);
        rilevazioneGlicemiaDAO.create(rilevazione4);
        List<RilevazioneGlicemia> result = rilevazioneGlicemiaDAO.getRilevazioniByPaziente(paziente.getIDUtente());

        //stampa tutte le rilevazioni trovate
        for (RilevazioneGlicemia r : result) {
            System.out.println("ID: " + r.getIdRilevazione() + ", Valore: " + r.getValore() + " mg/dL, Timestamp: " + r.getTimestamp() + ", Note: " + r.getNote());
        }

        // ASSERT
        assertEquals(4, result.size());
        assertEquals(120, result.get(0).getValore());
        // Confronta ignorando i nanosecondi per evitare problemi di precisione del DB
        assertEquals(now.withNano(0), result.get(0).getTimestamp().withNano(0));
    }
}