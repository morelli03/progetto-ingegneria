package org.univr.telemedicina.service;

import org.univr.telemedicina.dao.PazientiDAO;
import org.univr.telemedicina.dao.UtenteDAO;
import org.univr.telemedicina.exception.AuthServiceException;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.exception.MedicoNotFound;
import org.univr.telemedicina.model.Paziente;
import org.univr.telemedicina.model.Utente;


import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Scanner;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AdminService {

    private final UtenteDAO utenteDAO;
    private final PazientiDAO pazientiDAO;

    public AdminService(UtenteDAO utenteDao, PazientiDAO pazientiDao) {
        this.utenteDAO = utenteDao;
        this.pazientiDAO = pazientiDao;
    }

    // guida l'utente attraverso il processo di creazione di un nuovo utente nel sistema
    // questo metodo è ora stateless e più robusto
    public void creaUtente() {
        System.out.println("\n--- creazione di un nuovo utente ---");

        // utilizziamo un try-with-resources per chiudere automaticamente lo scanner
        try (Scanner sc = new Scanner(System.in)) {

            // acquisizione e validazione degli input
            String email = leggiEmailValida(sc);
            String hashedPassword = leggiPasswordHash(sc);
            String nome = leggiStringaNonVuota(sc, "inserisci il nome dell'utente:", "errore il nome non può essere vuoto riprova");
            String cognome = leggiStringaNonVuota(sc, "inserisci il cognome dell'utente:", "errore il cognome non può essere vuoto riprova");
            String ruolo = leggiRuoloValido(sc);
            LocalDate dataNascita = leggiDataValida(sc).toLocalDate(); // ho modificato per incapsulare in localdate non come si comporta la funzione ho preferito non toccarla

            //se è un paziente
            if(ruolo.equalsIgnoreCase("Paziente")) {
                try{
                    int idMedico = leggiIdMedicoDiRiferimento(sc);

                    // crea e salva l'utente
                    Utente nuovoUtente = new Utente(0, email, hashedPassword, nome, cognome, ruolo, dataNascita);
                    Utente utenteCreato = utenteDAO.create(nuovoUtente); // variabile locale non di istanza

                    System.out.println("utente creato con successo id utente " + utenteCreato.getIDUtente());

                    // crea e salva l'associazione paziente-medico
                    Paziente paziente = new Paziente(utenteCreato.getIDUtente(), idMedico);
                    pazientiDAO.create(paziente);
                    System.out.println("associazione medico-paziente creata con successo");// chiede l'id del medico di riferimento e lo salva in idmedico
                } catch (DataAccessException e) {
                    System.err.println("si è verificato un errore inaspettato " + e.getMessage());
                } catch (MedicoNotFound e) {
                    System.err.println("errore " + e.getMessage());
                }
            } else { // se è un medico
                // crea e salva l'utente
                Utente nuovoUtente = new Utente(0, email, hashedPassword, nome, cognome, ruolo, dataNascita);
                Utente utenteCreato = utenteDAO.create(nuovoUtente); // variabile locale non di istanza

                System.out.println("utente creato con successo id utente " + utenteCreato.getIDUtente());
            }


        }
        catch (DataAccessException | AuthServiceException e) {
            System.err.println("si è verificato un errore inaspettato " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- metodi di supporto per la validazione (rimasti invariati) ---

    private String leggiEmailValida(Scanner sc) {
        String email;
        while (true) {
            System.out.println("inserisci l'email dell'utente:");
            email = sc.nextLine();
            if (!email.trim().isEmpty() && email.contains("@")) {
                return email;
            } else {
                System.out.println("errore l'email non può essere vuota e deve contenere '@' riprova");
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
            System.out.println("inserisci il ruolo dell'utente (Medico/Paziente) ():");
            ruolo = sc.nextLine();
            if (ruolo.equals("Medico") || ruolo.equals("Paziente")) {
                return ruolo;
            } else {
                System.out.println("errore il ruolo deve essere 'medico' o 'paziente' riprova");
            }
        }
    }

    private Date leggiDataValida(Scanner sc) {
        Date dataNascita = null;
        while (dataNascita == null) {
            System.out.println("inserisci la data di nascita dell'utente (yyyy-mm-dd):");
            String dataNascitaInput = sc.nextLine();
            try {
                dataNascita = Date.valueOf(dataNascitaInput);
            } catch (IllegalArgumentException e) {
                System.out.println("errore il formato della data non è valido utilizza il formato `yyyy-mm-dd` riprova");
            }
        }
        return dataNascita;
    }

    // legge l'id del medico di riferimento dall'input dell'utente
    // questo metodo richiede che l'utente inserisca un'email valida di un medico esistente
    // @param sc lo scanner per leggere l'input dell'utente
    // @return l'id del medico di riferimento
    // @throws dataaccessexception se si verifica un errore durante l'accesso ai dati
    // @throws mediconotfound se il medico non viene trovato nel database
    private int leggiIdMedicoDiRiferimento(Scanner sc) throws DataAccessException, MedicoNotFound {
        while (true) {
            System.out.println("inserisci l'email del medico di riferimento:");
            String emailMedico = sc.nextLine();

            // cerca il medico nel database usando il dao iniettato
            Optional<Utente> medicoOptional = utenteDAO.findByEmail(emailMedico);

            if (medicoOptional.isPresent()) {
                Utente medico = medicoOptional.get();
                if (medico.getRuolo().equalsIgnoreCase("Medico")) {
                    return medico.getIDUtente();
                } else {
                    System.out.println("errore l'email inserita non corrisponde a un utente con ruolo 'medico' riprova");
                }
            } else { // se il medico non esiste
                //system.err.println("errore nessun utente trovato con questa email riprova");
                throw new MedicoNotFound("nessun medico trovato con l'email " + emailMedico);
            }
        }
    }

    // legge una password dall'input dell'utente e la converte in un hash sicuro
    // utilizza bcrypt per l'hashing della password
    // @param sc lo scanner per leggere l'input dell'utente
    // @return l'hash della password
    // @throws authserviceexception se si verifica un errore durante la lettura della password
    private String leggiPasswordHash(Scanner sc) throws AuthServiceException {
        String in = leggiStringaNonVuota(sc, "inserisci la password dell'utente:", "errore la password non può essere vuota riprova");

        // utilizza bcrypt per generare un hash della password
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(in);

    }
}