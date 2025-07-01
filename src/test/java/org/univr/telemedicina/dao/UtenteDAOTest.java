// File: src/test/java/org/univr/telemedicina/dao/UtenteDAOTest.java
package org.univr.telemedicina.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.Utente;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UtenteDAOTest {


    // Questo metodo verrà eseguito DOPO ogni test
    @AfterEach
    void tearDown() {
        // Svuotiamo le tabelle per garantire l'isolamento dei test
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // L'ordine è importante per via delle chiavi esterne (foreign keys)!
            // Inizia dalle tabelle che dipendono dalle altre.
            stmt.execute("DELETE FROM AssunzioniFarmaci");
            stmt.execute("DELETE FROM RilevazioniGlicemia");
            stmt.execute("DELETE FROM CondizioniPaziente");
            stmt.execute("DELETE FROM LogOperazioni");
            stmt.execute("DELETE FROM Notifiche");
            stmt.execute("DELETE FROM Terapie");
            stmt.execute("DELETE FROM Pazienti");
            stmt.execute("DELETE FROM Utenti");

        } catch (Exception e) {
            System.err.println("Impossibile pulire il database di test: " + e.getMessage());
        }
    }

    @Test
    void testCreateAndFindByEmail() {
        // --- ARRANGE (Prepara i dati del test) ---
        UtenteDAO utenteDAO = new UtenteDAO();
        Utente nuovoUtente = new Utente(0, "test.medico@email.com", "hashed_password", "Medico", "1", "Medico", LocalDate.of(1990, 1, 1));

        // --- ACT (Esegue l'azione da testare) ---
        Utente utenteCreato = new Utente();
        try {
            // Proviamo a creare un nuovo utente
            utenteCreato = utenteDAO.create(nuovoUtente);
        } catch (DataAccessException e) {
            fail("Errore durante la creazione dell'utente: " + e.getMessage());
        }

        // --- ASSERT (Verifica i risultati) ---
        // 1. Verifichiamo che l'ID sia stato generato e sia maggiore di 0
        assertTrue(utenteCreato.getIDUtente() > 0, "L'ID dell'utente non è stato generato correttamente.");

        // 2. Ora proviamo a ritrovare l'utente per email per essere sicuri che sia stato salvato
        Optional<Utente> utenteTrovatoOpt = Optional.empty();
        try {
            utenteTrovatoOpt = utenteDAO.findByEmail("test.medico@email.com");
        } catch (DataAccessException e) {
            fail("Errore durante la ricerca dell'utente per email: " + e.getMessage());
        }


        // 3. Verifichiamo che l'utente sia stato trovato
        assertTrue(utenteTrovatoOpt.isPresent(), "L'utente creato non è stato trovato nel database.");

        // 4. Verifichiamo che i dati corrispondano
        Utente utenteTrovato = utenteTrovatoOpt.get();
        assertEquals("Medico", utenteTrovato.getNome());
        assertEquals("1", utenteTrovato.getCognome());
        System.out.println("ID utente trovato: " + utenteTrovato.getIDUtente());
        assertEquals(utenteCreato.getIDUtente(), utenteTrovato.getIDUtente());
    }

    @Test
    void testFindByEmail_QuandoUtenteNonEsiste() {
        // --- ARRANGE ---
        UtenteDAO utenteDAO = new UtenteDAO();

        // --- ACT ---
        Optional<Utente> risultato = Optional.empty();
        try {
            risultato = utenteDAO.findByEmail("email.inesistente@email.com");
        } catch (DataAccessException e) {
            fail("Errore durante la ricerca dell'utente per email: " + e.getMessage());
        }

        // --- ASSERT ---
        assertFalse(risultato.isPresent(), "Non dovrebbe essere trovato nessun utente con questa email.");
    }
}