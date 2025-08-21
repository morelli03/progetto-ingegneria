package org.univr.telemedicina.dao;

import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.CondizioniPaziente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CondizioniPazienteDAO {
    // usato per trovare le condizioni di un paziente basandosi sul suo idpaziente
    // @param idpaziente l'idpaziente da cercare
    // @return una lista contenente le condizionipaziente se trovate altrimenti una lista vuota
    public List<CondizioniPaziente> listByIDPatId(int IDPaziente) throws DataAccessException {
        List<CondizioniPaziente> condizioni = new ArrayList<>();


        String sql = "SELECT * FROM CondizioniPaziente WHERE IDPaziente = ?";

        // try-with-resources per garantire la chiusura automatica delle risorse (connection preparedstatement resultset)
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // imposta il parametro della query (?) per evitare sql injection
            pstmt.setInt(1, IDPaziente);

            try (ResultSet rs = pstmt.executeQuery()) {
                // se c'è un risultato...
                while (rs.next()) {
                    // ...crea un oggetto condizionipaziente e popola i suoi campi con i dati dal resultset
                    CondizioniPaziente condizione = new CondizioniPaziente(
                            rs.getInt("IDPaziente"),
                            rs.getString("Tipo"),
                            rs.getString("Descrizione"),
                            rs.getString("Periodo"),
                            rs.getObject("DataRegistrazione", java.time.LocalDate.class)
                    );
                    condizione.setIDCondizione(rs.getInt("IDCondizione"));

                    // aggiungi la condiziona alla lista
                    condizioni.add(condizione);
                }
            }
        } catch (SQLException e) {
            System.err.println("errore durante la ricerca delle condizioni per idpaziente " + e.getMessage());
            throw new DataAccessException("errore durante la ricerca delle condizioni per il paziente con id " + IDPaziente, e);
        }

        // se non viene trovato nessuna condizione o si verifica un errore ritorna una lista vuota
        return condizioni;
    }

    // salva una nuova condizione nel database
    // @param condizione l'oggetto condizionipaziente da salvare
    public void create(CondizioniPaziente condizione) throws DataAccessException {
        String sql = "INSERT INTO CondizioniPaziente(IDPaziente, Tipo, Descrizione, Periodo, DataRegistrazione) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, condizione.getIDPaziente());
            pstmt.setString(2, condizione.getTipo());
            pstmt.setString(3, condizione.getDescrizione());
            pstmt.setString(4, condizione.getPeriodo());
            pstmt.setObject(5, condizione.getDataRegistrazione());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("errore durante la creazione della condizione " + e.getMessage());
            throw new DataAccessException("errore durante la creazione della condizione per il paziente con id " + condizione.getIDPaziente(), e);
        }
    }

    // aggiorna una condizione esistente nel database
    // @param condizione l'oggetto condizionipaziente da aggiornare
    public void update(CondizioniPaziente condizione) throws DataAccessException {
        String sql = "UPDATE CondizioniPaziente SET Tipo = ?, Descrizione = ?, Periodo = ?, DataRegistrazione = ? WHERE IDCondizione = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, condizione.getTipo());
            pstmt.setString(2, condizione.getDescrizione());
            pstmt.setString(3, condizione.getPeriodo());
            pstmt.setObject(4, condizione.getDataRegistrazione());
            pstmt.setInt(5, condizione.getIDCondizione()); // <-- la chiave per la clausola where

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                // è buona norma gestire il caso in cui l'id non esista
                throw new DataAccessException("nessuna condizione trovata con id " + condizione.getIDCondizione() + " per l'aggiornamento");
            }

        } catch (SQLException e) {
            throw new DataAccessException("errore durante l'aggiornamento della condizione nel database", e);
        }
    }

    public void delete(int idCondizione) throws DataAccessException {
        String sql = "DELETE FROM CondizioniPaziente WHERE IDCondizione = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCondizione);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DataAccessException("eliminazione condizione fallita non è stata trovata nessuna condizione con id " + idCondizione, null);
            }

        } catch (SQLException e) {
            System.err.println("errore durante l'eliminazione della condizione " + e.getMessage());
            throw new DataAccessException("errore durante l'eliminazione della condizione con id " + idCondizione, e);
        }
    }
}