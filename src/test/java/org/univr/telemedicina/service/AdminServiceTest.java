package org.univr.telemedicina.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.univr.telemedicina.dao.PazientiDAO;
import org.univr.telemedicina.dao.UtenteDAO;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.Paziente;
import org.univr.telemedicina.model.Utente;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    // I DAO vengono "falsificati" (mockati) per isolare il test alla sola logica di AdminService
    @Mock
    private UtenteDAO utenteDAO;

    @Mock
    private PazientiDAO pazientiDAO;

    // @InjectMocks crea un'istanza di AdminService e vi "inietta" i mock sopra definiti
    @InjectMocks
    private AdminService adminService;

    // Stream per catturare l'output su console e simulare l'input
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    /**
     * Questo metodo viene eseguito PRIMA di ogni test.
     * Redirige l'output della console (System.out) a uno stream che possiamo controllare
     * per verificare che i messaggi corretti vengano stampati.
     */
    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    /**
     * Questo metodo viene eseguito DOPO ogni test.
     * Ripristina gli stream originali di input e output per evitare effetti collaterali tra i test.
     */
    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    /**
     * Simula l'input dell'utente da console.
     * @param data La stringa che simula l'input dell'utente, con ogni input separato da una nuova riga.
     */
    private void provideInput(String data) {
        ByteArrayInputStream testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);
    }

    // Test per la creazione di un utente con ruolo Paziente
    @Test
    void creaUtente_SuccessoCreazionePaziente() throws DataAccessException {
        // --- ARRANGE ---
        // 1. Simula l'input dell'utente per creare un PAZIENTE
        String input = "paziente.test@email.com\n" + // email
                       "password123\n" +             // password
                       "Mario\n" +                   // nome
                       "Rossi\n" +                   // cognome
                       "Paziente\n" +                // ruolo
                       "1990-01-01\n" +              // data di nascita
                       "medico.ref@email.com\n";     // email del medico di riferimento
        provideInput(input);

        // 2. Definisci il comportamento atteso dei MOCK
        Utente pazienteCreato = new Utente(1, "paziente.test@email.com", "hashedPass", "Mario", "Rossi", "Paziente", LocalDate.of(1990, 1, 1));
        Utente medicoRiferimento = new Utente(10, "medico.ref@email.com", "hashedPassDoc", "Dottore", "Bianchi", "Medico", LocalDate.of(1975, 5, 5));

        // Quando il DAO crea l'utente, deve restituire il nostro utente finto
        when(utenteDAO.create(any(Utente.class))).thenReturn(pazienteCreato);
        // Quando il DAO cerca il medico, deve trovarlo
        when(utenteDAO.findByEmail("medico.ref@email.com")).thenReturn(Optional.of(medicoRiferimento));

        // --- ACT ---
        adminService.creaUtente();

        // --- ASSERT ---
        // 3. Verifica i risultati
        String output = outContent.toString();
        assertTrue(output.contains("Utente creato con successo. ID Utente: 1"));
        assertTrue(output.contains("Associazione medico-paziente creata con successo."));

        // Verifica che i metodi dei DAO siano stati chiamati correttamente
        verify(utenteDAO, times(1)).create(any(Utente.class));
        verify(utenteDAO, times(1)).findByEmail("medico.ref@email.com");
        verify(pazientiDAO, times(1)).create(any(Paziente.class)); // Verifica che l'associazione sia stata creata
    }

    // Test per la creazione di un utente con ruolo Medico
    @Test
    void creaUtente_SuccessoCreazioneMedico() throws DataAccessException {
        // --- ARRANGE ---
        String input = "medico.test@email.com\n" +
                       "password123\n" +
                       "Dottor\n" +
                       "Verdi\n" +
                       "Medico\n" +                // Ruolo Medico
                       "1980-02-02\n";
        provideInput(input);

        Utente medicoCreato = new Utente(2, "medico.test@email.com", "hashedPass", "Dottor", "Verdi", "Medico", LocalDate.of(1980, 2, 2));
        when(utenteDAO.create(any(Utente.class))).thenReturn(medicoCreato);

        // --- ACT ---
        adminService.creaUtente();

        // --- ASSERT ---
        String output = outContent.toString();
        assertTrue(output.contains("Utente creato con successo. ID Utente: 2"));
        // L'altro messaggio non deve esserci perché non è un paziente
        assertTrue(!output.contains("Associazione medico-paziente creata con successo."));

        // Il DAO dei pazienti non deve essere MAI chiamato se il ruolo è Medico
        verify(pazientiDAO, never()).create(any(Paziente.class));
    }

    // Test per la creazione di un medico con dati non validi
    @Test
    void creaMedico_FallimentoCreazioneUtenteDAO() throws DataAccessException {
        // --- ARRANGE ---
        String input = "utente.fallito@email.com\n" +
                       "password123\n" +
                       "Utente\n" +
                       "Fallito\n" +
                       "Medico\n" +
                       "2000-01-01\n";
        provideInput(input);

        // Simula che il DAO restituisca null durante la creazione
        when(utenteDAO.create(any(Utente.class))).thenThrow(new DataAccessException("Errore durante la creazione dell'utente" + "utente.fallito@email.com"));

        // --- ACT ---
        adminService.creaUtente();

        // --- ASSERT ---
        String output = errContent.toString();
        assertTrue(output.contains("Si è verificato un errore inaspettato: "));

        // Nessuna interazione successiva con i DAO
        verify(utenteDAO, times(1)).create(any(Utente.class));
        verifyNoMoreInteractions(pazientiDAO);
    }

    // Test per la creazione di un paziente con dati non validi
    @Test
    void creaPaziente_FallimentoCreazioneUtenteDAO() throws DataAccessException {
        // --- ARRANGE ---
        String input = "utente.fallito@email.com\n" +
                "password123\n" +
                "Utente\n" +
                "Fallito\n" +
                "Paziente\n" +
                "2000-01-01\n" +
                "medico@email.com\n";
        provideInput(input);

        // Simula che il DAO restituisca null durante la creazione
        when(utenteDAO.create(any(Utente.class))).thenThrow(new DataAccessException("Errore durante la creazione dell'utente" + "utente.fallito@email.com"));

        // Simula che il DAO trovi un medico con l'email specificata
        when(utenteDAO.findByEmail("medico@email.com")).thenReturn(Optional.of(new Utente(2, "medico.test@email.com", "hashedPass", "Dottor", "Verdi", "Medico", LocalDate.of(1980, 2, 2))));

        // --- ACT ---
        adminService.creaUtente();

        // --- ASSERT ---
        String output = errContent.toString();
        assertTrue(output.contains("Si è verificato un errore inaspettato: "));

        // Nessuna interazione successiva con i DAO
        verify(utenteDAO, times(1)).create(any(Utente.class));
        verifyNoMoreInteractions(pazientiDAO);
    }


    /**
     * Test corretto per il caso in cui si crea un paziente
     * ma l'email del medico di riferimento non esiste.
     */
    @Test
    void creaPaziente_QuandoMedicoRiferimentoNonEsiste() throws DataAccessException {
        // --- ARRANGE ---
        // 1. Simula l'input dell'utente con un'email di un medico che non esiste
        String input = "paziente.test@email.com\n" + // email
                "password123\n" +             // password
                "Mario\n" +                   // nome
                "Rossi\n" +                   // cognome
                "Paziente\n" +                // ruolo
                "1990-01-01\n" +              // data di nascita
                "medico.inesistente@email.com\n"; // email del medico NON ESISTENTE
        provideInput(input);

        // 2. Definisci il comportamento dei MOCK
        when(utenteDAO.findByEmail("medico.inesistente@email.com")).thenReturn(Optional.empty());

        // --- ACT ---
        adminService.creaUtente();

        // --- ASSERT ---
        // 3. Verifica i risultati

        String output = errContent.toString();

        // Verifica che l'utente sia stato creato prima del fallimento
        //assertTrue(output.contains("Utente creato con successo. ID Utente: 1"));
        // Verifica che sia stato stampato il messaggio di errore proveniente dall'eccezione

        //stampa l'output per debug
        System.out.println(output);

        assertTrue(output.contains("Nessun medico trovato con l'email: "));
        // Verifica che l'associazione paziente-medico NON sia stata creata
        verify(pazientiDAO, never()).create(any(Paziente.class));
        // Verifica che la ricerca del medico sia stata tentata
        verify(utenteDAO, times(1)).findByEmail("medico.inesistente@email.com");
    }
}