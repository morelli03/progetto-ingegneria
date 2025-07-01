package org.univr.telemedicina.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.univr.telemedicina.model.AssunzioneFarmaci;
import org.univr.telemedicina.model.Terapia;
import org.univr.telemedicina.model.Utente;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AssunzioneFarmaciDAOTest {

    private AssunzioneFarmaciDAO assunzioneFarmaciDAO;
    private UtenteDAO utenteDAO;
    private TerapiaDAO terapiaDAO;
    private PazientiDAO pazientiDAO;
    private Utente paziente;
    private Terapia terapia;

    @BeforeEach
    void setUp() throws Exception {
        assunzioneFarmaciDAO = new AssunzioneFarmaciDAO();
        utenteDAO = new UtenteDAO();
        terapiaDAO = new TerapiaDAO();
        pazientiDAO = new PazientiDAO();

        LocalDate dataNascita = LocalDate.of(1980, 1, 1);
        Utente medico = utenteDAO.create(new Utente(0, "medico.test@email.com", "pass", "Medico", "Test", "Medico", dataNascita));
        paziente = utenteDAO.create(new Utente(0, "paziente.test.assunzione@email.com", "pass", "Paziente", "Test", "Paziente", dataNascita));
        pazientiDAO.create(new org.univr.telemedicina.model.Paziente(paziente.getIDUtente(), medico.getIDUtente()));

        terapia = new Terapia(paziente.getIDUtente(), medico.getIDUtente(), "TestFarmaco", "10mg", 2, "Assumere a stomaco pieno", LocalDate.now(), LocalDate.now().plusDays(10));
        terapiaDAO.assignTherapy(terapia);
        
        List<Terapia> terapie = terapiaDAO.listTherapiesByPatId(paziente.getIDUtente());
        terapia.setIDTerapia(terapie.get(0).getIDTerapia());
    }

    @AfterEach
    void tearDown() {
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM AssunzioniFarmaci");
            stmt.execute("DELETE FROM Terapie");
            stmt.execute("DELETE FROM Pazienti");
            stmt.execute("DELETE FROM Utenti");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testAggiungiAndLeggiAssunzioniFarmaci() throws Exception {
        // ARRANGE
        AssunzioneFarmaci assunzione = new AssunzioneFarmaci(terapia.getIDTerapia(), paziente.getIDUtente(), LocalDateTime.now(), "10mg");

        // ACT
        assunzioneFarmaciDAO.aggiungiAssunzione(assunzione);
        List<AssunzioneFarmaci> result = assunzioneFarmaciDAO.leggiAssunzioniFarmaci(paziente.getIDUtente());

        // ASSERT
        assertFalse(result.isEmpty(), "La lista delle assunzioni non dovrebbe essere vuota.");
        assertEquals(1, result.size());
        assertEquals(terapia.getIDTerapia(), result.get(0).getIDTerapia());
    }

    @Test
    void testLeggiAssunzioniGiorno() throws Exception {
        // ARRANGE
        LocalDateTime oggi = LocalDateTime.now();
        LocalDateTime ieri = oggi.minusDays(1);
        AssunzioneFarmaci assunzioneOggi = new AssunzioneFarmaci(terapia.getIDTerapia(), paziente.getIDUtente(), oggi, "10mg");
        AssunzioneFarmaci assunzioneIeri = new AssunzioneFarmaci(terapia.getIDTerapia(), paziente.getIDUtente(), ieri, "10mg");

        assunzioneFarmaciDAO.aggiungiAssunzione(assunzioneOggi);
        assunzioneFarmaciDAO.aggiungiAssunzione(assunzioneIeri);

        // ACT
        List<AssunzioneFarmaci> result = assunzioneFarmaciDAO.leggiAssunzioniGiorno(paziente.getIDUtente(), LocalDate.now());

        // ASSERT
        assertEquals(1, result.size(), "Dovrebbe esserci solo un'assunzione per oggi.");
        assertEquals(oggi.toLocalDate(), result.get(0).getTimestampAssunzione().toLocalDate());
    }

    @Test
    void testGetConteggioAssunzioniGiornoPerPazienti() throws Exception {
        // ARRANGE
        AssunzioneFarmaci assunzione1 = new AssunzioneFarmaci(terapia.getIDTerapia(), paziente.getIDUtente(), LocalDateTime.now(), "10mg");
        AssunzioneFarmaci assunzione2 = new AssunzioneFarmaci(terapia.getIDTerapia(), paziente.getIDUtente(), LocalDateTime.now().minusHours(2), "10mg");
        assunzioneFarmaciDAO.aggiungiAssunzione(assunzione1);
        assunzioneFarmaciDAO.aggiungiAssunzione(assunzione2);

        // ACT
        Map<Integer, Integer> conteggio = assunzioneFarmaciDAO.getConteggioAssunzioniGiornoPerPazienti(Collections.singletonList(paziente.getIDUtente()), LocalDate.now());

        // ASSERT
        assertNotNull(conteggio);
        assertEquals(2, conteggio.get(paziente.getIDUtente()).intValue(), "Il conteggio delle assunzioni per il paziente dovrebbe essere 2.");
    }
}