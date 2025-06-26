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
     * fa in modo che le rilevazioni piu recenti siano prime nella lista
     */
    public List<RilevazioneGlicemia> getRilevazioniByPaziente(int idPaziente) {
        List<RilevazioneGlicemia> rilevazioni = new ArrayList<>();
        String sql = "SELECT * FROM RilevazioniGlicemia WHERE IDPaziente = ? ORDER BY Timestamp DESC";

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


}

