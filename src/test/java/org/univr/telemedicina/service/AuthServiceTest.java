// File: src/test/java/org/univr/telemedicina/service/AuthServiceTest.java

package org.univr.telemedicina.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder; // Importa l'interfaccia
import org.univr.telemedicina.dao.UtenteDAO;
import org.univr.telemedicina.model.Utente;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock // Crea un mock finto per UtenteDAO
    private UtenteDAO utenteDao;

    @Mock // Crea un mock finto anche per PasswordEncoder
    private PasswordEncoder passwordEncoder;

    @InjectMocks // Crea AuthService e gli passa i due mock di sopra
    private AuthService authService;

    @Test
    void verificaPassword_conCredenzialiCorrette_restituisceUtente() throws Exception {
        // --- ARRANGE ---
        String email = "utente@test.com";
        String passwordInChiaro = "password123";
        String passwordHashata = "un_hash_a_caso"; // Non importa il valore, basta che sia consistente

        Utente utenteFinto = new Utente();
        utenteFinto.setEmail(email);
        utenteFinto.setHashedPassword(passwordHashata);

        // Istruisci i mock su come comportarsi
        // 1. Quando il DAO cerca l'utente, fagli trovare l'utente finto
        when(utenteDao.findByEmail(email)).thenReturn(Optional.of(utenteFinto));

        // 2. Quando l'encoder confronta le password, digli che sono uguali
        when(passwordEncoder.matches(passwordInChiaro, passwordHashata)).thenReturn(true);

        // --- ACT ---
        Optional<Utente> risultato = authService.verificaPassword(email, passwordInChiaro);

        // --- ASSERT ---
        assertTrue(risultato.isPresent(), "L'utente avrebbe dovuto essere autenticato con successo.");
        assertEquals(email, risultato.get().getEmail());
    }

    @Test
    void verificaPassword_conPasswordSbagliata_restituisceOptionalVuoto() throws Exception {
        // --- ARRANGE ---
        String email = "utente@test.com";
        String passwordInChiaro = "password_sbagliata";
        String passwordHashata = "hash_finto_per_il_test";

        Utente utenteFinto = new Utente();
        utenteFinto.setEmail(email);
        utenteFinto.setHashedPassword(passwordHashata);

        // Istruisci i mock
        when(utenteDao.findByEmail(email)).thenReturn(Optional.of(utenteFinto));
        // Stavolta, di' al password encoder che le password NON corrispondono
        when(passwordEncoder.matches(passwordInChiaro, passwordHashata)).thenReturn(false);

        // --- ACT ---
        Optional<Utente> risultato = authService.verificaPassword(email, passwordInChiaro);

        // --- ASSERT ---
        assertFalse(risultato.isPresent());
    }

    @Test
    void verificaPassword_conEmailNonEsistente_restituisceOptionalVuoto() throws Exception {
        // --- ARRANGE ---
        String email = "utente@test.com";
        String passwordInChiaro = "password123";
        // Istruisci il mock del DAO a restituire un Optional vuoto
        when(utenteDao.findByEmail(email)).thenReturn(Optional.empty());

        // Non serve neanche il password encoder, perché non troverà l'utente
        // --- ACT ---
        Optional<Utente> risultato = authService.verificaPassword(email, passwordInChiaro);
        // --- ASSERT ---
        assertFalse(risultato.isPresent(), "Se l'email non esiste, il risultato dovrebbe essere vuoto.");
    }
}