package org.univr.telemedicina.dao;

import org.univr.telemedicina.model.CondizioniPaziente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CondizioniPazienteDAO {
    /**
     * Usato per trovare le condizioni di un paziente basandosi sul suo IDPaziente.
     *
     * @param IDPaziente L'IDPaziente da cercare.
     * @return Un Optional contenente le CondizioniPaziente se trovate, altrimenti un Optional vuoto.
     */
    public Optional<CondizioniPaziente> findByIDPatId(int IDPaziente) {
        // Query per selezionare l'utente con una specifica email
        String sql = "SELECT * FROM CondizioniPaziente WHERE IDPaziente = ?";

        // try-with-resources per garantire la chiusura automatica delle risorse (Connection, PreparedStatement, ResultSet)
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Imposta il parametro della query (?) per evitare SQL Injection
            pstmt.setInt(1, IDPaziente);

            try (ResultSet rs = pstmt.executeQuery()) {
                // Se c'è un risultato...
                if (rs.next()) {
                    // ...crea un oggetto Utente e popola utente con la riga trovata
                    CondizioniPaziente condizioni = new CondizioniPaziente(
                            rs.getInt("IDCondizione"),
                            rs.getInt("IDPaziente"),
                            rs.getString("Tipo"),
                            rs.getString("Descrizione"),
                            rs.getString("Periodo"),
                            rs.getDate("DataRegistrazione")
                    );
                    // Ritorna l'utente trovato, avvolto in un Optional
                    return Optional.of(condizioni);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la ricerca del paziente per IDPaziente: " + e.getMessage());
        }
        // Se non viene trovato nessun utente o si verifica un errore, ritorna un Optional vuoto
        return Optional.empty();
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