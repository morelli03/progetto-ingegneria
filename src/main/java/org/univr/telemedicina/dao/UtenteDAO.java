package org.univr.telemedicina.dao;

import org.univr.telemedicina.model.Utente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UtenteDAO {
    /**
     * Usato per autenticazione degli utenti.                       LOGIN
     * Trova un utente basandosi sulla sua email.
     * Restituisce un Optional per gestire in modo pulito il caso in cui l'utente non esista.
     *
     * @param email L'email da cercare.
     * @return Un Optional contenente l'Utente se trovato, altrimenti un Optional vuoto.
     */
    public Optional<Utente> findByEmail(String email) {
        // Query per selezionare l'utente con una specifica email
        String sql = "SELECT * FROM Utenti WHERE Email = ?";

        // try-with-resources per garantire la chiusura automatica delle risorse (Connection, PreparedStatement, ResultSet)
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Imposta il parametro della query (?) per evitare SQL Injection
            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                // Se c'è un risultato...
                if (rs.next()) {
                    // ...crea un oggetto Utente e popola utente con la riga trovata
                    Utente utente = new Utente(
                            rs.getInt("IDUtente"),
                            rs.getString("Email"),
                            rs.getString("HashedPassword"),
                            rs.getString("Nome"),
                            rs.getString("Cognome"),
                            rs.getString("Ruolo"),
                            rs.getDate("DataNascita") // Assicurati che il campo DataUtente sia presente nella tabella Utenti
                    );
                    // Ritorna l'utente trovato, avvolto in un Optional
                    return Optional.of(utente);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la ricerca dell'utente per email: " + e.getMessage());
        }
        // Se non viene trovato nessun utente o si verifica un errore, ritorna un Optional vuoto
        return Optional.empty();
    }

    /**
     * Trova un utente associato al medico basandosi sul suo id.        OTTIENI LISTA PAZIENTI
     * Restituisce una lista di pazienti associati a un medico specifico.
     *
     * @param IDUtente L'ID del medico di riferimento.
     * @return Un Optional contenente l'Utente se trovato, altrimenti un Optional vuoto.
     */                                     //id medico
    public List<Utente> listPatientsByMedId(int IDUtente) {
        //fai un hashset di pazienti
        List<Utente> pazienti = new ArrayList<>();


        //String sql = "SELECT * FROM Pazienti WHERE IDMedicoRiferimento = ?";
        String sql = "SELECT u.* " +
                     "FROM Pazienti p " +
                     "JOIN Utenti u ON p.IDPaziente = u.IDUtente " +
                     "WHERE p.IDMedicoRiferimento = ?";

        // try-with-resources per garantire la chiusura automatica delle risorse (Connection, PreparedStatement, ResultSet)
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Imposta il parametro della query (?) per evitare SQL Injection
            pstmt.setInt(1, IDUtente);

            try (ResultSet rs = pstmt.executeQuery()) {
                // Se c'è un risultato...
                while (rs.next()) {
                    // ...crea un oggetto Utente e popola i suoi campi con i dati dal ResultSet
                    // Cambio la funzionalità di IDUtente, non viene più base64, invece mi prendo direttamente tutte le info con la lista
                    Utente paziente = new Utente(
                            rs.getInt("IDUtente"),
                            rs.getString("Email"),
                            rs.getString("HashedPassword"),
                            rs.getString("Nome"),
                            rs.getString("Cognome"),
                            rs.getString("Ruolo"),
                            rs.getDate("DataNascita")
                    );

                    // Ritorna l'utente trovato, avvolto in un Optional
                    pazienti.add(paziente);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la ricerca dell'utente per medico: " + e.getMessage());
        }
        // Se non viene trovato nessun utente o si verifica un errore, ritorna un Optional vuoto
        return pazienti;
    }

    /**
     * Salva un nuovo utente nel database.
     *
     * @param utente L'oggetto Utente da salvare (IMPORTANTE implementare creazione ID - UUID).
     */
    public void create(Utente utente) {
        String sql = "INSERT INTO Utenti(Email, HashedPassword, Nome, Cognome, Ruolo, DataNascita) VALUES(?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, utente.getEmail());
            pstmt.setString(2, utente.getHashedPassword());
            pstmt.setString(3, utente.getNome());
            pstmt.setString(4, utente.getCognome());
            pstmt.setString(5, utente.getRuolo());
            pstmt.setDate(6, utente.getDataNascita());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore durante la creazione dell'utente: " + e.getMessage());
        }
    }

    // Aggiungi qui altri metodi se necessario, come:
    // public Optional<Utente> findById(int id) { ... }
    // public void update(Utente utente) { ... }
    // public void delete(int id) { ... }
}