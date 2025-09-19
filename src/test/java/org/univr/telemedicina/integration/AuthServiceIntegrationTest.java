package org.univr.telemedicina.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.univr.telemedicina.dao.DatabaseManager;
import org.univr.telemedicina.dao.UtenteDAO;
import org.univr.telemedicina.exception.AuthServiceException;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.Utente;
import org.univr.telemedicina.service.AuthService;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceIntegrationTest {

    private AuthService authService;
    private UtenteDAO utenteDAO;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseManager.setURL("jdbc:sqlite:test.sqlite");
        utenteDAO = new UtenteDAO();
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(utenteDAO, passwordEncoder);

        // Creazione utente per il test
        Utente utente = new Utente(0, "test@example.com", passwordEncoder.encode("password123"), "Test", "User", "Paziente", LocalDate.now());
        utenteDAO.create(utente);
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM Utenti");
        }
    }

    @Test
    void testVerificaPasswordCorretta() throws AuthServiceException {
        Optional<Utente> result = authService.verificaPassword("test@example.com", "password123");
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void testVerificaPasswordSbagliata() throws AuthServiceException {
        Optional<Utente> result = authService.verificaPassword("test@example.com", "wrongpassword");
        assertFalse(result.isPresent());
    }

    @Test
    void testVerificaPasswordUtenteNonEsistente() throws AuthServiceException {
        Optional<Utente> result = authService.verificaPassword("nonexistent@example.com", "password123");
        assertFalse(result.isPresent());
    }
}
