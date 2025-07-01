package org.univr.telemedicina.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.Paziente;
import org.univr.telemedicina.model.Terapia;
import org.univr.telemedicina.model.Utente;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TerapiaDAOTest {
    private TerapiaDAO terapiaDAO;
    private UtenteDAO utenteDAO;
    private PazientiDAO pazientiDAO;
    private Utente medico;
    private Utente paziente1;
    private Utente paziente2;

    @BeforeEach
    void setUp() throws Exception {
        // Inizializza i DAO necessari
        terapiaDAO = new TerapiaDAO();
        utenteDAO = new UtenteDAO();
        pazientiDAO = new PazientiDAO();

        // Crea utenti di test (medico e pazienti)
        LocalDate dataNascita = LocalDate.of(1988, 4, 12);
        medico = utenteDAO.create(new Utente(0, "medico.terapia@email.com", "pass", "Medico", "Terapia", "Medico", dataNascita));
        paziente1 = utenteDAO.create(new Utente(0, "paziente1.terapia@email.com", "pass", "PazienteUno", "Terapia", "Paziente", dataNascita));
        paziente2 = utenteDAO.create(new Utente(0, "paziente2.terapia@email.com", "pass", "PazienteDue", "Terapia", "Paziente", dataNascita));

        // Associa i pazienti al medico (se necessario per altri test, ma buona pratica)
        pazientiDAO.create(new Paziente(paziente1.getIDUtente(), medico.getIDUtente()));
        pazientiDAO.create(new Paziente(paziente2.getIDUtente(), medico.getIDUtente()));
    }

    @AfterEach
    void tearDown() {
        // Pulisce il DB dopo ogni test per garantire l'isolamento
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM Terapie");
            stmt.execute("DELETE FROM Pazienti");
            stmt.execute("DELETE FROM Utenti");
        } catch (Exception e) {
            System.err.println("Errore durante la pulizia del database di test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    void testAssignAndListTherapies() throws DataAccessException {
        // ARRANGE
        Terapia terapia = new Terapia(paziente1.getIDUtente(), medico.getIDUtente(), "Metformina", "500mg", 2, "Dopo i pasti", LocalDate.now(), LocalDate.now().plusDays(30));

        // ACT
        terapiaDAO.assignTherapy(terapia);
        List<Terapia> result = terapiaDAO.listTherapiesByPatId(paziente1.getIDUtente());

        // ASSERT
        assertEquals(1, result.size(), "Dovrebbe esserci una terapia per il paziente 1");
        assertEquals("Metformina", result.get(0).getNomeFarmaco());
        assertEquals(paziente1.getIDUtente(), result.get(0).getIDPaziente());
    }

    @Test
    void testUpdateTherapy() throws DataAccessException {
        // ARRANGE
        Terapia terapia = new Terapia(paziente1.getIDUtente(), medico.getIDUtente(), "Insulina", "10 unità", 1, "Prima di dormire", LocalDate.now(), LocalDate.now().plusDays(30));
        terapiaDAO.assignTherapy(terapia);

        // Recupera la terapia appena inserita per ottenere l'ID
        List<Terapia> terapieInserite = terapiaDAO.listTherapiesByPatId(paziente1.getIDUtente());
        Terapia daAggiornare = terapieInserite.get(0);
        daAggiornare.setQuantita("12 unità");
        daAggiornare.setIndicazioni("Indicazioni aggiornate");

        // ACT
        terapiaDAO.updateTherapy(daAggiornare);
        List<Terapia> result = terapiaDAO.listTherapiesByPatId(paziente1.getIDUtente());

        //stampa
        for (Terapia t : result) {
            System.out.println("ID: " + t.getIDTerapia() + ", Farmaco: " + t.getNomeFarmaco() + ", Quantità: " + t.getQuantita() + ", Indicazioni: " + t.getIndicazioni());
        }

        // ASSERT
        assertEquals(1, result.size());
        assertEquals("12 unità", result.get(0).getQuantita(), "La quantità avrebbe dovuto essere aggiornata");
        assertEquals("Indicazioni aggiornate", result.get(0).getIndicazioni(), "Le indicazioni avrebbero dovuto essere aggiornate");
    }

    @Test
    void testGetActivePatientIds() throws DataAccessException {
        // ARRANGE
        LocalDate oggi = LocalDate.now();
        // Terapia attiva per paziente1
        terapiaDAO.assignTherapy(new Terapia(paziente1.getIDUtente(), medico.getIDUtente(), "Attiva", "1", 1, "", oggi.minusDays(5), oggi.plusDays(5)));
        // Terapia scaduta per paziente2
        terapiaDAO.assignTherapy(new Terapia(paziente2.getIDUtente(), medico.getIDUtente(), "Scaduta", "1", 1, "", oggi.minusDays(10), oggi.minusDays(1)));
        // Terapia futura per un altro paziente (non dovrebbe essere trovata)
        Utente paziente3 = utenteDAO.create(new Utente(0, "paziente3.terapia@email.com", "pass", "PazienteTre", "Terapia", "Paziente", LocalDate.now()));
        terapiaDAO.assignTherapy(new Terapia(paziente3.getIDUtente(), medico.getIDUtente(), "Futura", "1", 1, "", oggi.plusDays(1), oggi.plusDays(10)));


        // ACT
        List<Integer> activeIds = terapiaDAO.getActivePatientIds();

        // ASSERT
        assertNotNull(activeIds);
        assertEquals(1, activeIds.size(), "Dovrebbe esserci solo un paziente con terapie attive");
        assertTrue(activeIds.contains(paziente1.getIDUtente()), "L'ID del paziente 1 dovrebbe essere nella lista degli attivi");
        assertFalse(activeIds.contains(paziente2.getIDUtente()), "L'ID del paziente 2 non dovrebbe essere incluso (terapia scaduta)");
        assertFalse(activeIds.contains(paziente3.getIDUtente()), "L'ID del paziente 3 non dovrebbe essere incluso (terapia futura)");
    }

    @Test
    void testGetFrequenzeGiornalierePerPazienti() throws DataAccessException {
        // ARRANGE
        // Assegna due terapie diverse allo stesso paziente
        terapiaDAO.assignTherapy(new Terapia(paziente1.getIDUtente(), medico.getIDUtente(), "FarmacoA", "1", 2, "test", LocalDate.now(), LocalDate.now().plusDays(1)));
        terapiaDAO.assignTherapy(new Terapia(paziente1.getIDUtente(), medico.getIDUtente(), "FarmacoB", "1", 3, "test", LocalDate.now(), LocalDate.now().plusDays(1)));

        // ACT
        Map<Integer, Integer> frequenze = terapiaDAO.getFrequenzeGiornalierePerPazienti(Collections.singletonList(paziente1.getIDUtente()));

        // ASSERT
        assertNotNull(frequenze);
        assertEquals(1, frequenze.size(), "La mappa dovrebbe contenere un solo paziente");
        assertTrue(frequenze.containsKey(paziente1.getIDUtente()), "La mappa dovrebbe contenere la chiave per il paziente 1");
        // La frequenza totale dovrebbe essere la somma delle frequenze giornaliere (2 + 3)
        assertEquals(5, frequenze.get(paziente1.getIDUtente()).intValue(), "La frequenza totale giornaliera dovrebbe essere 5");
    }

    @Test
    void testGetFrequenzeGiornaliereConListaVuota() throws DataAccessException {
        // ARRANGE
        // Nessuna terapia inserita

        // ACT
        Map<Integer, Integer> frequenze = terapiaDAO.getFrequenzeGiornalierePerPazienti(Collections.emptyList());

        // ASSERT
        assertNotNull(frequenze);
        assertTrue(frequenze.isEmpty(), "La mappa delle frequenze dovrebbe essere vuota se la lista di input è vuota");
    }
}