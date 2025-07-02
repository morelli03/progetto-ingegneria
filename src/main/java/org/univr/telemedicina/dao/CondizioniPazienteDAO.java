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
    /**
     * Usato per trovare le condizioni di un paziente basandosi sul suo IDPaziente.
     *
     * @param IDPaziente L'IDPaziente da cercare.
     * @return Una lista contenente le CondizioniPaziente se trovate, altrimenti una lista vuoto.
     */
    public List<CondizioniPaziente> listByIDPatId(int IDPaziente) throws DataAccessException {
        List<CondizioniPaziente> condizioni = new ArrayList<>();


        String sql = "SELECT * FROM CondizioniPaziente WHERE IDPaziente = ?";

        // try-with-resources per garantire la chiusura automatica delle risorse (Connection, PreparedStatement, ResultSet)
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Imposta il parametro della query (?) per evitare SQL Injection
            pstmt.setInt(1, IDPaziente);

            try (ResultSet rs = pstmt.executeQuery()) {
                // Se c'è un risultato...
                while (rs.next()) {
                    // ...crea un oggetto CondizioniPaziente e popola i suoi campi con i dati dal ResultSet
                    CondizioniPaziente condizione = new CondizioniPaziente(
                            rs.getInt("IDPaziente"),
                            rs.getString("Tipo"),
                            rs.getString("Descrizione"),
                            rs.getString("Periodo"),
                            rs.getObject("DataRegistrazione", java.time.LocalDate.class)
                    );
                    condizione.setIDCondizione(rs.getInt("IDCondizione"));

                    // Aggiungi la condiziona alla lista
                    condizioni.add(condizione);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la ricerca delle condizioni per IDPaziente: " + e.getMessage());
            throw new DataAccessException("Errore durante la ricerca delle condizioni per il paziente con ID " + IDPaziente, e);
        }

        // Se non viene trovato nessuna condizione o si verifica un errore, ritorna una lista vuota
        return condizioni;
    }

    /**
     * Salva una nuova condizione nel database
     *
     * @param condizione L'oggetto CondizioniPaziente da salvare
     */
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
            System.err.println("Errore durante la creazione della condizione: " + e.getMessage());
            throw new DataAccessException("Errore durante la creazione della condizione per il paziente con ID " + condizione.getIDPaziente(), e);
        }
    }

    /**
     * Aggiorna una condizione esistente nel database
     *
     * @param condizione L'oggetto CondizioniPaziente da aggiornare
     */
    public void update(CondizioniPaziente condizione) throws DataAccessException {
        String sql = "UPDATE CondizioniPaziente SET Tipo = ?, Descrizione = ?, Periodo = ?, DataRegistrazione = ? WHERE IDCondizione = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, condizione.getTipo());
            pstmt.setString(2, condizione.getDescrizione());
            pstmt.setString(3, condizione.getPeriodo());
            pstmt.setObject(4, condizione.getDataRegistrazione());
            pstmt.setInt(5, condizione.getIDCondizione()); // <-- La chiave per la clausola WHERE

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                // È buona norma gestire il caso in cui l'ID non esista
                throw new DataAccessException("Nessuna condizione trovata con ID: " + condizione.getIDCondizione() + " per l'aggiornamento.");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Errore durante l'aggiornamento della condizione nel database", e);
        }
    }
}