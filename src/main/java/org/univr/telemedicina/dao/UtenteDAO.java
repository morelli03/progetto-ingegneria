package org.univr.telemedicina.dao;

import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.Utente;

import java.sql.*;
//import java.util.ArrayList;
//import java.util.List;
import java.time.LocalDate;
import java.util.Optional;

public class UtenteDAO {
    // usato per autenticazione degli utenti                       login
    // trova un utente basandosi sulla sua email
    // restituisce un optional per gestire in modo pulito il caso in cui l'utente non esista
    // @param email l'email da cercare
    // @return un optional contenente l'utente se trovato altrimenti un optional vuoto
    public Optional<Utente> findByEmail(String email) throws DataAccessException {
        // query per selezionare l'utente con una specifica email
        String sql = "SELECT * FROM Utenti WHERE Email = ?";

        // try-with-resources per garantire la chiusura automatica delle risorse (connection preparedstatement resultset)
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // imposta il parametro della query (?) per evitare sql injection
            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                // se c'è un risultato...
                if (rs.next()) {
                    // ...crea un oggetto utente e popola utente con la riga trovata
                    Utente utente = new Utente(
                            rs.getInt("IDUtente"),
                            rs.getString("Email"),
                            rs.getString("HashedPassword"),
                            rs.getString("Nome"),
                            rs.getString("Cognome"),
                            rs.getString("Ruolo"),
                            rs.getObject("DataNascita", LocalDate.class) // assicurati che il campo datautente sia presente nella tabella utenti
                    );
                    // ritorna l'utente trovato avvolto in un optional
                    return Optional.of(utente);
                }
            }
        } catch (SQLException e) {
            System.err.println("errore durante la ricerca dell'utente per email " + e.getMessage());
            throw new DataAccessException("errore durante la ricerca dell'utente con email " + email, e);
        }
        // se non viene trovato nessun utente o si verifica un errore ritorna un optional vuoto
        return Optional.empty();
    }

    // salva un nuovo utente nel database genera l'idutente automaticamente e lo salva nell'oggetto utente
    // se si vuole creare un paziente dopo aver creato l'utente si deve creare un oggetto paziente inserire l'id generato
    // in idpaziente e aggiungere l'id del medico di riferimento in idmedicoriferimento poi chiamare il metodo create di pazientidao
    // @param utente l'oggetto utente da salvare con idutente a 0
    // @return utente con idutente aggiornato dopo l'inserimento
    public Utente create(Utente utente) throws DataAccessException {
        String sql = "INSERT INTO Utenti(Email, HashedPassword, Nome, Cognome, Ruolo, DataNascita) VALUES(?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, utente.getEmail());
            pstmt.setString(2, utente.getHashedPassword());
            pstmt.setString(3, utente.getNome());
            pstmt.setString(4, utente.getCognome());
            pstmt.setString(5, utente.getRuolo());
            pstmt.setObject(6, utente.getDataNascita());

            int affectedRows = pstmt.executeUpdate();

            //controllo se ho scritto per ottenere l'idutente           === importante === sarebbe da mettere uuid
            if(affectedRows > 0){
                try(ResultSet generatedKeys = pstmt.getGeneratedKeys()){
                    if(generatedKeys.next()){
                        utente.setIDUtente(generatedKeys.getInt(1)); // imposta l'id generato nell'oggetto utente
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("errore durante la creazione dell'utente " + e.getMessage());
            throw new DataAccessException("errore durante la creazione dell'utente " + utente.getEmail(), e);
        }
        return utente;
    }


    // trova l'email di un utente basandosi sul suo idutente
    // restituisce un optional per gestire in modo pulito il caso in cui l'utente non esista
    // @param idutente l'idutente da cercare
    // @return un optional contenente l'email dell'utente se trovato altrimenti un optional vuoto
    public Optional<String> findEmailById(int idUtente) throws DataAccessException {
        // query per selezionare l'email dell'utente con un idutente specifico
        String sql = "SELECT Email FROM Utenti WHERE IDUtente = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUtente);

            try (ResultSet rs = pstmt.executeQuery()) {
                // se c'è un risultato...
                if (rs.next()) {
                    // ...ritorna l'email dell'utente avvolta in un optional
                    return Optional.of(rs.getString("Email"));
                }
            }
        } catch (SQLException e) {
            System.err.println("errore durante la ricerca dell'email per idutente " + e.getMessage());
            throw new DataAccessException("errore durante la ricerca dell'email per idutente " + idUtente, e);
        }
        // se non viene trovato nessun utente o si verifica un errore ritorna un optional vuoto
        return Optional.empty();
    }
}