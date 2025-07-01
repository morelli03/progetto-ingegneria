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

            pstmt.setInt(1, notifica.getPriorita());
            pstmt.setString(2, notifica.getTitolo());
            pstmt.setString(3, notifica.getMessaggio());
            pstmt.setString(4, notifica.getTipo());
            pstmt.setInt(5, notifica.getLetta());
            pstmt.setObject(6, notifica.getTimestamp());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Errore durante l'inserimento della notifica: " + e.getMessage(), e);
        }
    }

    /**
     * Legge le notifiche lette = 0 per un determinato destinatario.
     * Le ordina per priorità e timestamp in ordine decrescente.
     * @param idDestinatario L'ID del destinatario per cui leggere le notifiche.
     * @return Una lista di notifiche non lette per il destinatario specificato.
     */
    public List<Notifica> leggiNotifichePerId(int idDestinatario) throws DataAccessException {
        List<Notifica> notifiche = new ArrayList<>();

        // Query che seleziona IDPaziente univoci dove la data odierna
        // rientra nel range della terapia. La funzione date('now') è specifica di SQLite.
        String sql = "SELECT * FROM Notifiche " +
                "WHERE Letta = 0 AND IDDestinatario = ?" +
                "ORDER BY Priorita DESC, Timestamp DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

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
