package org.univr.telemedicina.dao;

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
    public List<CondizioniPaziente> listByIDPatId(int IDPaziente) {
        //
        List<CondizioniPaziente> condizioni = new ArrayList<>();


        String sql = "SELECT * FROM Terapie WHERE IDPaziente = ?";

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
                            rs.getInt("IDCondizione"),
                            rs.getInt("IDPaziente"),
                            rs.getString("Tipo"),
                            rs.getString("Descrizione"),
                            rs.getString("Periodo"),
                            rs.getDate("DataRegistrazione")
                    );

                    // Aggiungi la condiziona alla lista
                    condizioni.add(condizione);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la ricerca delle condizioni per IDPaziente: " + e.getMessage());
        }

        // Se non viene trovato nessuna condizione o si verifica un errore, ritorna una lista vuota
        return condizioni;
    }

    /**
     * Salva una nuova condizione nel database
     *
     * @param condizione L'oggetto CondizioniPaziente da salvare
     */
    public void create(CondizioniPaziente condizione) {
        String sql = "INSERT INTO CondizioniPaziente(IDCondizione, IDPaziente, Tipo, Descrizione, Periodo, DataRegistrazione) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, condizione.getIDCondizione());
            pstmt.setInt(2, condizione.getIDPaziente());
            pstmt.setString(3, condizione.getTipo());
            pstmt.setString(4, condizione.getDescrizione());
            pstmt.setString(5, condizione.getPeriodo());
            pstmt.setDate(6, condizione.getDataRegistrazione());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore durante la creazione della condizione: " + e.getMessage());
        }
    }
}