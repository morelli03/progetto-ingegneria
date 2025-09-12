package org.univr.telemedicina.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.Notifica;
import org.univr.telemedicina.model.Utente;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificheDAOTest {
    private NotificheDAO notificheDAO;
    private UtenteDAO utenteDAO;
    private Utente utente1;
    private Utente utente2;

    @BeforeEach
    void setUp() throws Exception {
        notificheDAO = new NotificheDAO();
        utenteDAO = new UtenteDAO();

        LocalDate dataNascita = LocalDate.of(2000, 1, 1);
        utente1 = utenteDAO.create(new Utente(0, "utente1.notifiche@email.com", "pass", "UtenteUno", "Test", "Paziente", dataNascita));
        utente2 = utenteDAO.create(new Utente(0, "utente2.notifiche@email.com", "pass", "UtenteDue", "Test", "Paziente", dataNascita));
    }

    @AfterEach
    void tearDown() {
        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {
            // L'ordine è importante per via delle chiavi esterne (foreign keys)!
            stmt.execute("DELETE FROM Notifiche");
            stmt.execute("DELETE FROM Utenti");
        } catch (Exception e) {
            System.err.println("Impossibile pulire il database di test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test che simula il ciclo di vita completo di una notifica:
     * 1. Creazione di notifiche per utenti diversi.
     * 2. Lettura delle notifiche per un utente specifico.
     * 3. Aggiornamento dello stato a "letta".
     * 4. Verifica che le notifiche siano state marcate come lette.
     */
    @Test
    void testLeggiEImpostaComeLette() throws DataAccessException {
        // --- ARRANGE ---
        // Crea 2 notifiche per utente1 e 1 per utente2
        notificheDAO.inserisciNotifica(new Notifica(utente1.getIDUtente(), 2, "Glicemia Alta", "Valore: 180", "GLICEMIA", LocalDateTime.now()));
        notificheDAO.inserisciNotifica(new Notifica(utente1.getIDUtente(), 1, "Promemoria", "Ricorda farmaco", "TERAPIA", LocalDateTime.now().minusHours(1)));
        notificheDAO.inserisciNotifica(new Notifica(utente2.getIDUtente(), 3, "Benvenuto!", "Benvenuto nel sistema", "SISTEMA", LocalDateTime.now()));

        // --- ACT 1: Leggi le notifiche per utente1 ---
        List<Notifica> notificheUtente1 = notificheDAO.leggiNotifichePerId(utente1.getIDUtente());

        // --- ASSERT 1 ---
        assertNotNull(notificheUtente1);
        assertEquals(2, notificheUtente1.size(), "Utente1 dovrebbe avere 2 notifiche non lette.");
        // Verifica l'ordinamento (priorità più alta prima)
        assertEquals("Glicemia Alta", notificheUtente1.get(0).getTitolo());

        // --- ACT 2: Imposta le notifiche di utente1 come lette ---
        for (Notifica n : notificheUtente1) {
            notificheDAO.setNotificaLetta(n.getIdNotifica());
        }

        // --- ASSERT 2: Verifica che le notifiche di utente1 siano state segnate come lette ---
        List<Notifica> notificheLetteUtente1 = notificheDAO.leggiNotifichePerId(utente1.getIDUtente());
        assertEquals(2, notificheLetteUtente1.size(), "Dovrebbero esserci ancora 2 notifiche per utente1, ma segnate come lette.");
        for (Notifica n : notificheLetteUtente1) {
            assertEquals(1, n.getLetta(), "La notifica con ID " + n.getIdNotifica() + " dovrebbe essere segnata come letta.");
        }

        // --- ASSERT 3: Verifica che le notifiche di utente2 siano ancora presenti ---
        List<Notifica> notificheUtente2 = notificheDAO.leggiNotifichePerId(utente2.getIDUtente());
        assertEquals(1, notificheUtente2.size(), "Le notifiche di Utente2 non dovevano essere modificate.");
        assertEquals("Benvenuto!", notificheUtente2.get(0).getTitolo());
    }
}