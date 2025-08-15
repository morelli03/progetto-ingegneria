package org.univr.telemedicina.dao;

import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.Notifica;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe per la gestione delle operazioni di accesso al database relative alle notifiche.
 * Questa classe contiene metodi per inserire e recuperare notifiche.
 */
public class NotificheDAO {

    /**
     * Inserisce una nuova notifica nel database.
     * @param notifica L'oggetto Notifiche da inserire.
     */
    public void inserisciNotifica(Notifica notifica) throws DataAccessException {
        String sql = "INSERT INTO Notifiche (IDDestinatario, Priorita, Titolo, Messaggio, Tipo, Letta, Timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, notifica.getIdDestinatario());
            pstmt.setInt(2, notifica.getPriorita());
            pstmt.setString(3, notifica.getTitolo());
            pstmt.setString(4, notifica.getMessaggio());
            pstmt.setString(5, notifica.getTipo());
            pstmt.setInt(6, notifica.getLetta());
            pstmt.setObject(7, notifica.getTimestamp());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante l'inserimento della notifica: " + e.getMessage(), e);
        }
    }

    /**
     * Legge le notifiche per un determinato destinatario.
     * Le ordina per priorità e timestamp in ordine decrescente.
     * @param idDestinatario L'ID del destinatario per cui leggere le notifiche.
     * @return Una lista di notifiche non lette per il destinatario specificato.
     */
    public List<Notifica> leggiNotifichePerId(int idDestinatario) throws DataAccessException {
        List<Notifica> notifiche = new ArrayList<>();

        // La query SQL per recuperare le notifiche non lette per un determinato destinatario.
        String sql = "SELECT * FROM Notifiche " +
                "WHERE IDDestinatario = ?" +
                "ORDER BY Letta ASC, Timestamp DESC";



        // Utilizza un PreparedStatement per la sicurezza e la corretta gestione dei parametri.
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Imposta il valore del parametro (?) nella query.
            // Il primo parametro ha indice 1.
            pstmt.setInt(1, idDestinatario);

            // Esegui la query e ottieni i risultati.
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Notifica notifica = new Notifica(
                            rs.getInt("IDNotifica"),
                            rs.getInt("IDDestinatario"),
                            rs.getInt("Priorita"),
                            rs.getString("Titolo"),
                            rs.getString("Messaggio"),
                            rs.getString("Tipo"),
                            rs.getInt("Letta"),
                            rs.getObject("Timestamp", java.time.LocalDateTime.class)
                    );
                    notifiche.add(notifica);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il recupero delle notifiche per IDDestinatario: " + e.getMessage());
            throw new DataAccessException("Errore durante il recupero delle notifiche per il destinatario con ID " + idDestinatario, e);
        }
        return notifiche;
    }

    /**
     * Segna una notifica come letta.
     * @param idNotifica L'ID della notifica da segnare come letta.
     */
    public void setNotificaLetta(int idNotifica) throws DataAccessException {
        String sql = "UPDATE Notifiche SET Letta = 1 WHERE IDNotifica = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idNotifica);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante l'aggiornamento della lettura della notifica: " + e.getMessage(), e);
        }
    }

}
