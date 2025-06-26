package org.univr.telemedicina.dao;

import org.univr.telemedicina.model.RilevazioneGlicemia;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RilevazioneGlicemiaDAO {
    /**
     * Salva nel database una rilevazione di glicemia.
     */
    public void create(RilevazioneGlicemia rilevazione) {
        String sql = "INSERT INTO RilevazioniGlicemia (IDPaziente, Valore, Timestamp, Note) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, rilevazione.getIdPaziente());
            pstmt.setInt(2, rilevazione.getValore());
            pstmt.setTimestamp(3, rilevazione.getTimestamp());
            pstmt.setString(4, rilevazione.getNote());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore durante il salvataggio della rilevazione di glicemia: " + e.getMessage());
        }
    }

    /**
     * trova una rilevazione di glicemia per IDrilevazione.
     */
    public Optional<RilevazioneGlicemia> findById(int idRilevazione) {
        String sql = "SELECT * FROM RilevazioniGlicemia WHERE IDRilevazione = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idRilevazione);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    RilevazioneGlicemia rilevazione = new RilevazioneGlicemia(
                            rs.getInt("IDRilevazione"),
                            rs.getInt("IDPaziente"),
                            rs.getInt("Valore"),
                            rs.getTimestamp("Timestamp"),
                            rs.getString("Note")
                    );
                    return Optional.of(rilevazione);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la ricerca della rilevazione di glicemia: " + e.getMessage());
        }
        return Optional.empty();
    }
    /**
     * trova tutte le rilevazioni di glicemia per un paziente specifico.
     */
    public List<RilevazioneGlicemia> getRilevazioniByPaziente(int idPaziente) {
        List<RilevazioneGlicemia> rilevazioni = new ArrayList<>();
        String sql = "SELECT IDRilevazione, IDPaziente, Valore, Timestamp, Note FROM RilevazioniGlicemia WHERE IDPaziente = ? ORDER BY Timestamp DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idPaziente);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    RilevazioneGlicemia rilevazione = new RilevazioneGlicemia(
                            rs.getInt("IDRilevazione"),
                            rs.getInt("IDPaziente"),
                            rs.getInt("Valore"),
                            rs.getTimestamp("Timestamp"),
                            rs.getString("Note")
                    );
                    rilevazioni.add(rilevazione);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la ricerca delle rilevazioni di glicemia: " + e.getMessage());
        }
        return rilevazioni;
    }

    /**
     * aggiorna una rilevazione di glicemia esistente nel database.
     */
    public void update(RilevazioneGlicemia rilevazione) {
        String sql = "UPDATE RilevazioniGlicemia SET IDPaziente = ?, Valore = ?, Timestamp = ?, Note = ? WHERE IDRilevazione = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, rilevazione.getIdPaziente());
            pstmt.setInt(2, rilevazione.getValore());
            pstmt.setTimestamp(3, rilevazione.getTimestamp());
            pstmt.setString(4, rilevazione.getNote());
            pstmt.setInt(5, rilevazione.getIdRilevazione());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore durante l'aggiornamento della rilevazione di glicemia: " + e.getMessage());
        }
    }

    /**
     * elimina una rilevazione di glicemia dal database.
     */
    public void delete(int idRilevazione) {
        String sql = "DELETE FROM RilevazioniGlicemia WHERE IDRilevazione = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idRilevazione);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore durante l'eliminazione della rilevazione di glicemia: " + e.getMessage());
        }
    }
}
