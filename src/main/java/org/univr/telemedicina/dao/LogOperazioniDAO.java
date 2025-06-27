package org.univr.telemedicina.dao;

import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.LogOperazione;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe DAO per gestire le operazioni di accesso ai dati relativi ai log delle operazioni.
 * - Crea un log di operazione nel database.
 * - Legge tutti i log di operazioni dal database, ordinati dal più recente al più vecchio.
 * - Recupera la cronologia di tutte le operazioni effettuate su un paziente specifico, ordinata dal più recente al più vecchio.
 * - Recupera la cronologia di tutte le operazioni effettuate su un medico specifico, ordinata dal più recente al più vecchio.
 * Metodo helper per evitare la duplicazione del codice nella lettura dei log.
 */
public class LogOperazioniDAO {

    /**
     * Scrive un log di operazione nel database.
     * @param log L'oggetto LogOperazioni da inserire nel database.
     */
    public void createLog(LogOperazione log) throws DataAccessException {
        String sql = "INSERT INTO LogOperazioni (IDMedicoOperante, IDPazienteInteressato, TipoOperazione, DescrizioneOperazione, Timestamp) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, log.getIDMedicoOperante());
            pstmt.setInt(2, log.getIDPazienteInteressato());
            pstmt.setString(3, log.getTipoOperazione());
            pstmt.setString(4, log.getDescrizioneOperazione());
            pstmt.setObject(5, log.getTimestamp());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore durante l'inserimento del log: " + e.getMessage());
            throw new DataAccessException("Errore durante l'inserimento del log di operazione", e);
        }
    }


    /**
     * Legge tutti i log di operazioni dal database.
     * Dal più recente al più vecchio.
     * @return Una lista di LogOperazioni contenente i log di operazioni.
     */
    public List<LogOperazione> getAllLog() throws DataAccessException {
        List<LogOperazione> logs = new ArrayList<>();
        String sql = "SELECT * FROM LogOperazioni ORDER BY Timestamp DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                LogOperazione log = new LogOperazione(
                        rs.getInt("IDLog"),
                        rs.getInt("IDMedicoOperante"),
                        rs.getInt("IDPazienteInteressato"),
                        rs.getString("TipoOperazione"),
                        rs.getString("DescrizioneOperazione"),
                        rs.getObject("Timestamp", LocalDateTime.class)
                );
                logs.add(log);
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la lettura dei log per paziente: " + e.getMessage());
            throw new DataAccessException("Errore durante la lettura dei log di operazioni", e);
        }
        return logs;
    }

    /**
     * Recupera la cronologia di tutte le operazioni effettuate su un paziente specifico.
     * Dal più recente al più vecchio.
     *
     * @param IDPaziente L'ID del paziente di cui si vuole la cronologia.
     * @return Una lista di oggetti LogOperazione.
     */
    public List<LogOperazione> findLogsByPazienteId(int IDPaziente) throws DataAccessException {
        List<LogOperazione> logs = new ArrayList<>();
        String sql = "SELECT * FROM LogOperazioni WHERE IDPazienteInteressato = ? ORDER BY Timestamp DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, IDPaziente);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // chiamo il metodo helper per mappare il ResultSet a LogOperazione
                    logs.add(mapResultSetToLog(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la lettura dei log per paziente: " + e.getMessage());
            throw new DataAccessException("Errore durante la lettura dei log per il paziente con ID " + IDPaziente, e);
        }
        return logs;
    }

    /**
     * Recupera la cronologia di tutte le operazioni effettuate su un medico specifico.
     * Dal più recente al più vecchio.
     *
     * @param IDMedico L'ID del paziente di cui si vuole la cronologia.
     * @return Una lista di oggetti LogOperazione.
     */
    public List<LogOperazione> findLogsByMedicoId(int IDMedico) throws DataAccessException {
        List<LogOperazione> logs = new ArrayList<>();
        String sql = "SELECT * FROM LogOperazioni WHERE IDMedicoOperante = ? ORDER BY Timestamp DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, IDMedico);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // chiamo il metodo helper per mappare il ResultSet a LogOperazione
                    logs.add(mapResultSetToLog(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la lettura dei log per medico: " + e.getMessage());
            throw new DataAccessException("Errore durante la lettura dei log per il medico con ID " + IDMedico, e);
        }
        return logs;
    }


    /**
     * Metodo helper per mappare una riga di ResultSet a un oggetto LogOperazione.
     * @param rs Il ResultSet posizionato sulla riga corretta.
     * @return Un oggetto LogOperazione popolato.
     * @throws SQLException In caso di errore di accesso ai dati del ResultSet.
     */
    private LogOperazione mapResultSetToLog(ResultSet rs) throws SQLException {
        LogOperazione log = new LogOperazione();
        log.setIDLog(rs.getInt("IDLog"));
        log.setIDMedicoOperante(rs.getInt("IDMedicoOperante"));
        log.setIDPazienteInteressato(rs.getInt("IDPazienteInteressato"));
        log.setTipoOperazione(rs.getString("TipoOperazione"));
        log.setDescrizioneOperazione(rs.getString("DescrizioneOperazione"));
        //Uso getObject per recuperare LocalDateTime
        log.setTimestamp(rs.getObject("Timestamp", LocalDateTime.class));
        return log;
    }
}
