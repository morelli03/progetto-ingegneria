package org.univr.telemedicina.dao;

import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.LogOperazione;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// classe dao per gestire le operazioni di accesso ai dati relativi ai log delle operazioni
// - crea un log di operazione nel database
// - legge tutti i log di operazioni dal database ordinati dal più recente al più vecchio
// - recupera la cronologia di tutte le operazioni effettuate su un paziente specifico ordinata dal più recente al più vecchio
// - recupera la cronologia di tutte le operazioni effettuate su un medico specifico ordinata dal più recente al più vecchio
// metodo helper per evitare la duplicazione del codice nella lettura dei log
public class LogOperazioniDAO {

    // scrive un log di operazione nel database
    // @param log l'oggetto logoperazioni da inserire nel database
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
            System.err.println("errore durante l'inserimento del log " + e.getMessage());
            throw new DataAccessException("errore durante l'inserimento del log di operazione", e);
        }
    }


    // legge tutti i log di operazioni dal database
    // dal più recente al più vecchio
    // @return una lista di logoperazioni contenente i log di operazioni
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
            System.err.println("errore durante la lettura dei log per paziente " + e.getMessage());
            throw new DataAccessException("errore durante la lettura dei log di operazioni", e);
        }
        return logs;
    }

    // recupera la cronologia di tutte le operazioni effettuate su un paziente specifico
    // dal più recente al più vecchio
    // @param idpaziente l'id del paziente di cui si vuole la cronologia
    // @return una lista di oggetti logoperazione
    public List<LogOperazione> findLogsByPazienteId(int IDPaziente) throws DataAccessException {
        List<LogOperazione> logs = new ArrayList<>();
        String sql = "SELECT * FROM LogOperazioni WHERE IDPazienteInteressato = ? ORDER BY Timestamp DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, IDPaziente);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // chiamo il metodo helper per mappare il resultset a logoperazione
                    logs.add(mapResultSetToLog(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("errore durante la lettura dei log per paziente " + e.getMessage());
            throw new DataAccessException("errore durante la lettura dei log per il paziente con id " + IDPaziente, e);
        }
        return logs;
    }

    // recupera la cronologia di tutte le operazioni effettuate su un medico specifico
    // dal più recente al più vecchio
    // @param idmedico l'id del paziente di cui si vuole la cronologia
    // @return una lista di oggetti logoperazione
    public List<LogOperazione> findLogsByMedicoId(int IDMedico) throws DataAccessException {
        List<LogOperazione> logs = new ArrayList<>();
        String sql = "SELECT * FROM LogOperazioni WHERE IDMedicoOperante = ? ORDER BY Timestamp DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, IDMedico);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // chiamo il metodo helper per mappare il resultset a logoperazione
                    logs.add(mapResultSetToLog(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("errore durante la lettura dei log per medico " + e.getMessage());
            throw new DataAccessException("errore durante la lettura dei log per il medico con id " + IDMedico, e);
        }
        return logs;
    }


    // metodo helper per mappare una riga di resultset a un oggetto logoperazione
    // @param rs il resultset posizionato sulla riga corretta
    // @return un oggetto logoperazione popolato
    // @throws sqlexception in caso di errore di accesso ai dati del resultset
    private LogOperazione mapResultSetToLog(ResultSet rs) throws SQLException {
        LogOperazione log = new LogOperazione();
        log.setIDLog(rs.getInt("IDLog"));
        log.setIDMedicoOperante(rs.getInt("IDMedicoOperante"));
        log.setIDPazienteInteressato(rs.getInt("IDPazienteInteressato"));
        log.setTipoOperazione(rs.getString("TipoOperazione"));
        log.setDescrizioneOperazione(rs.getString("DescrizioneOperazione"));
        //uso getobject per recuperare localdatetime
        log.setTimestamp(rs.getObject("Timestamp", LocalDateTime.class));
        return log;
    }
}
