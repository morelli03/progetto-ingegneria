// File: src/test/java/org/univr/telemedicina/dao/UtenteDAOTest.java
package org.univr.telemedicina.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.univr.telemedicina.model.Utente;

import java.io.File;
import java.sql.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UtenteDAOTest {


    @Test
    void testCreateAndFindByEmail() {
        // --- ARRANGE (Prepara i dati del test) ---
        UtenteDAO utenteDAO = new UtenteDAO();
        Utente nuovoUtente = new Utente(0, "test.utente@email.com", "hashed_password", "Test", "User", "Paziente", new Date(System.currentTimeMillis()));

        // --- ACT (Esegui l'azione da testare) ---
        Utente utenteCreato = utenteDAO.create(nuovoUtente);

        // --- ASSERT (Verifica i risultati) ---
        // 1. Verifichiamo che l'ID sia stato generato e sia maggiore di 0
        assertTrue(utenteCreato.getIDUtente() > 0, "L'ID dell'utente non è stato generato correttamente.");

        // 2. Ora proviamo a ritrovare l'utente per email per essere sicuri che sia stato salvato
        Optional<Utente> utenteTrovatoOpt = utenteDAO.findByEmail("test.utente@email.com");

        // 3. Verifichiamo che l'utente sia stato trovato
        assertTrue(utenteTrovatoOpt.isPresent(), "L'utente creato non è stato trovato nel database.");

        // 4. Verifichiamo che i dati corrispondano
        Utente utenteTrovato = utenteTrovatoOpt.get();
        assertEquals("Test", utenteTrovato.getNome());
        assertEquals("User", utenteTrovato.getCognome());
        System.out.println("ID utente trovato: " + utenteTrovato.getIDUtente());
        assertEquals(utenteCreato.getIDUtente(), utenteTrovato.getIDUtente());
    }

    @Test
    void testFindByEmail_QuandoUtenteNonEsiste() {
        // --- ARRANGE ---
        UtenteDAO utenteDAO = new UtenteDAO();

        // --- ACT ---
        Optional<Utente> risultato = utenteDAO.findByEmail("email.inesistente@email.com");

        // --- ASSERT ---
        assertFalse(risultato.isPresent(), "Non dovrebbe essere trovato nessun utente con questa email.");
    }
}