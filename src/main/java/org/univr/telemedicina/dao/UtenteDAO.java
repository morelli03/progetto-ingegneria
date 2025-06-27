package org.univr.telemedicina.dao;

import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.Utente;

import java.sql.*;
//import java.util.ArrayList;
//import java.util.List;
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
    public Optional<Utente> findByEmail(String email) throws DataAccessException {
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
            throw new DataAccessException("Errore durante la ricerca dell'utente con email " + email, e);
        }
        // Se non viene trovato nessun utente o si verifica un errore, ritorna un Optional vuoto
        return Optional.empty();
    }

    /**
     * Salva un nuovo utente nel database. Genera l'IDUtente automaticamente e lo salva nell'oggetto Utente.ù
     * Se si vuole creare un paziente, dopo aver creato l'utente, si deve creare un oggetto paziente, inserire l'id generato
     * in IDPaziente e aggiungere l'id del medico di riferimento in IDMedicoRiferimento. Poi chiamare il metodo create di PazientiDAO.
     *
     * @param utente L'oggetto Utente da salvare con IDUtente a 0
     * @return utente con IDUtente aggiornato dopo l'inserimento.
     */
    public Utente create(Utente utente) throws DataAccessException {
        String sql = "INSERT INTO Utenti(Email, HashedPassword, Nome, Cognome, Ruolo, DataNascita) VALUES(?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, utente.getEmail());
            pstmt.setString(2, utente.getHashedPassword());
            pstmt.setString(3, utente.getNome());
            pstmt.setString(4, utente.getCognome());
            pstmt.setString(5, utente.getRuolo());
            pstmt.setDate(6, utente.getDataNascita());

            int affectedRows = pstmt.executeUpdate();

            //controllo se ho scritto per ottenere l'IDUtente           === IMPORTANTE === sarebbe da mettere UUID
            if(affectedRows > 0){
                try(ResultSet generatedKeys = pstmt.getGeneratedKeys()){
                    if(generatedKeys.next()){
                        utente.setIDUtente(generatedKeys.getInt(1)); // Imposta l'ID generato nell'oggetto Utente
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore durante la creazione dell'utente: " + e.getMessage());
            throw new DataAccessException("Errore durante la creazione dell'utente con email " + utente.getEmail(), e);
        }
        return utente;
    }
}