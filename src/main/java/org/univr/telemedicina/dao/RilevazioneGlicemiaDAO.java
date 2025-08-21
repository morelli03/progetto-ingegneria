package org.univr.telemedicina.dao;

import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.RilevazioneGlicemia;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RilevazioneGlicemiaDAO {
    // salva nel database una rilevazione di glicemia
    public void create(RilevazioneGlicemia rilevazione) throws DataAccessException {
        String sql = "INSERT INTO RilevazioniGlicemia (IDPaziente, Valore, Timestamp, Note) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, rilevazione.getIdPaziente());
            pstmt.setInt(2, rilevazione.getValore());
            pstmt.setObject(3, rilevazione.getTimestamp());
            pstmt.setString(4, rilevazione.getNote());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("errore durante il salvataggio della rilevazione di glicemia " + e.getMessage());
            throw new DataAccessException("errore durante il salvataggio della rilevazione di glicemia per il paziente con id " + rilevazione.getIdPaziente(), e);
        }
    }

    // trova tutte le rilevazioni di glicemia per un paziente specifico
    // fa in modo che le rilevazioni piu recenti siano prime nella lista
    public List<RilevazioneGlicemia> getRilevazioniByPaziente(int idPaziente) throws DataAccessException {
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
                            rs.getObject("Timestamp", LocalDateTime.class),
                            rs.getString("Note")
                    );
                    rilevazioni.add(rilevazione);
                }
            }
        } catch (SQLException e) {
            System.err.println("errore durante la ricerca delle rilevazioni di glicemia " + e.getMessage());
            throw new DataAccessException("errore durante la ricerca delle rilevazioni di glicemia per il paziente con id " + idPaziente, e);
        }
        return rilevazioni;
    }
}