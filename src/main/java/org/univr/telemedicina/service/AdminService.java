package org.univr.telemedicina.service;

import org.univr.telemedicina.dao.PazientiDAO;
import org.univr.telemedicina.dao.UtenteDAO;
import org.univr.telemedicina.exception.AuthServiceException;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.Paziente;
import org.univr.telemedicina.model.Utente;

import java.sql.Date;
import java.util.Optional;
import java.util.Scanner;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AdminService {

    private final UtenteDAO utenteDAO;
    private final PazientiDAO pazientiDAO;

    public AdminService() {
        this.utenteDAO = new UtenteDAO();
        this.pazientiDAO = new PazientiDAO();
    }

    /**
     * Guides the user through the process of creating a new user in the system.
     * This method is now stateless and more robust.
     */
    public void creaUtente() {
        System.out.println("\n--- Creazione di un nuovo utente ---");

        // Utilizziamo un try-with-resources per chiudere automaticamente lo Scanner
        try (Scanner sc = new Scanner(System.in)) {

            // Acquisizione e validazione degli input
            String email = leggiEmailValida(sc);
            String hashedPassword = leggiPasswordHash(sc);
            String nome = leggiStringaNonVuota(sc, "Inserisci il nome dell'utente:", "Errore: il nome non può essere vuoto. Riprova.");
            String cognome = leggiStringaNonVuota(sc, "Inserisci il cognome dell'utente:", "Errore: il cognome non può essere vuoto. Riprova.");
            String ruolo = leggiRuoloValido(sc);
            Date dataNascita = leggiDataValida(sc);

            // Crea e salva l'utente
            Utente nuovoUtente = new Utente(0, email, hashedPassword, nome, cognome, ruolo, dataNascita);
            Utente utenteCreato = utenteDAO.create(nuovoUtente); // Variabile locale, non di istanza

            if (utenteCreato != null) {
                System.out.println("Utente creato con successo. ID Utente: " + utenteCreato.getIDUtente());

                // Se l'utente è un paziente, associa un medico
                if (utenteCreato.getRuolo().equalsIgnoreCase("Paziente")) {
                    int idMedico = leggiIdMedicoDiRiferimento(sc);

                    // Crea e salva l'associazione paziente-medico
                    Paziente paziente = new Paziente(utenteCreato.getIDUtente(), idMedico);
                    pazientiDAO.create(paziente);
                    System.out.println("Associazione medico-paziente creata con successo.");
                }
            } else {
                System.out.println("Errore durante la creazione dell'utente. Il DAO ha restituito null.");
            }

        }
        catch (DataAccessException | AuthServiceException e) {
            System.err.println("Si è verificato un errore inaspettato: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Metodi di supporto per la validazione (rimasti invariati) ---

    private String leggiEmailValida(Scanner sc) {
        String email;
        while (true) {
            System.out.println("Inserisci l'email dell'utente:");
            email = sc.nextLine();
            if (!email.trim().isEmpty() && email.contains("@")) {
                return email;
            } else {
                System.out.println("Errore: l'email non può essere vuota e deve contenere '@'. Riprova.");
            }
        }
    }

    private String leggiStringaNonVuota(Scanner sc, String prompt, String errorMessage) {
        String input;
        while (true) {
            System.out.println(prompt);
            input = sc.nextLine();
            if (!input.trim().isEmpty()) {
                return input;
            } else {
                System.out.println(errorMessage);
            }
        }
    }

    private String leggiRuoloValido(Scanner sc) {
        String ruolo;
        while (true) {
            System.out.println("Inserisci il ruolo dell'utente (Medico/Paziente) ():");
            ruolo = sc.nextLine();
            if (ruolo.equals("Medico") || ruolo.equals("Paziente")) {
                return ruolo;
            } else {
                System.out.println("Errore: il ruolo deve essere 'Medico' o 'Paziente'. Riprova.");
            }
        }
    }

    private Date leggiDataValida(Scanner sc) {
        Date dataNascita = null;
        while (dataNascita == null) {
            System.out.println("Inserisci la data di nascita dell'utente (YYYY-MM-DD):");
            String dataNascitaInput = sc.nextLine();
            try {
                dataNascita = Date.valueOf(dataNascitaInput);
            } catch (IllegalArgumentException e) {
                System.out.println("Errore: il formato della data non è valido. Utilizza il formato `YYYY-MM-DD`. Riprova.");
            }
        }
        return dataNascita;
    }

    private int leggiIdMedicoDiRiferimento(Scanner sc) throws DataAccessException {
        int idMedico = -1;
        while (true) {
            System.out.println("Inserisci l'email del medico di riferimento:");
            String emailMedico = sc.nextLine();

            // Cerca il medico nel database usando il DAO iniettato
            Optional<Utente> medicoOptional = utenteDAO.findByEmail(emailMedico);

            if (medicoOptional.isPresent()) {
                Utente medico = medicoOptional.get();
                if (medico.getRuolo().equalsIgnoreCase("Medico")) {
                    return medico.getIDUtente();
                } else {
                    System.out.println("Errore: l'email inserita non corrisponde a un utente con ruolo 'Medico'. Riprova.");
                }
            } else {
                System.out.println("Errore: nessun utente trovato con questa email. Riprova.");
            }
        }
    }

    /**
     * Legge una password dall'input dell'utente e la converte in un hash sicuro.
     * Utilizza BCrypt per l'hashing della password.
     *
     * @param sc lo Scanner per leggere l'input dell'utente
     * @return l'hash della password
     * @throws AuthServiceException se si verifica un errore durante la lettura della password
     */
    private String leggiPasswordHash(Scanner sc) throws AuthServiceException {
        String in = leggiStringaNonVuota(sc, "Inserisci la password dell'utente:", "Errore: la password non può essere vuota. Riprova.");

        // Utilizza BCrypt per generare un hash della password
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(in);

    }
}