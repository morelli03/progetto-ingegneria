package org.univr.telemedicina.dao;

import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.Terapia;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TerapiaDAO {

    /**
     * Trova le terapie associate a un paziente basandosi sul suo ID.
     *
     * (visualizzaTerapiePrescritte ed esitoVisualizzaTerapiePrescritte in Visualizza terapia prescritta SD)
     *
     * @param IDUtente L'IDUtente da cercare.
     * @return Un Optional contenente l'Utente se trovato, altrimenti un Optional vuoto.
     */

    // IDPaziente
    public List<Terapia> listTherapiesByPatId(int IDUtente) throws DataAccessException {
        //
        List<Terapia> terapie = new ArrayList<>();


        String sql = "SELECT * FROM Terapie WHERE IDPaziente = ?";

        // try-with-resources per garantire la chiusura automatica delle risorse (Connection, PreparedStatement, ResultSet)
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Imposta il parametro della query (?) per evitare SQL Injection
            pstmt.setInt(1, IDUtente);

            try (ResultSet rs = pstmt.executeQuery()) {
                // Se c'è un risultato...
                while (rs.next()) {
                    // ...crea un oggetto Terapia e popola i suoi campi con i dati dal ResultSet
                    Terapia terapia = new Terapia(
                            rs.getInt("IDTerapia"),
                            rs.getInt("IDPaziente"),
                            rs.getInt("IDMedico"),
                            rs.getString("NomeFarmaco"),
                            rs.getString("Quantita"),
                            rs.getInt("FrequenzaGiornaliera"),
                            rs.getString("Indicazioni"),
                            rs.getDate("DataInizio"),
                            rs.getDate("DataFine")
                    );

                    // Aggiungi la terapia alla lista
                    terapie.add(terapia);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la ricerca della terapia per IDPaziente: " + e.getMessage());
            throw new DataAccessException("Errore durante la ricerca delle terapie per il paziente con ID " + IDUtente, e);
        }

        // Se non viene trovato nessun utente o si verifica un errore, ritorna una lista vuota
        return terapie;
    }

    /**
     * Restituisce una lista di ID univoci di tutti i pazienti che hanno almeno una terapia in corso.
     * Una terapia si considera in corso se la data odierna è compresa tra la DataInizio e la DataFine.
     * Gestisce anche il caso in cui la DataFine non sia specificata (terapia a tempo indeterminato).
     * @return Una lista di Integer contenente gli ID dei pazienti attivi.
     */
    public List<Integer> getActivePatientIds() throws DataAccessException {
        List<Integer> patientsIds = new ArrayList<>();

        // Query che seleziona IDPaziente univoci dove la data odierna
        // rientra nel range della terapia. La funzione date('now') è specifica di SQLite.
        String sql = "SELECT DISTINCT IDPaziente FROM Terapie " +
                "WHERE date('now') >= DataInizio AND (date('now') <= DataFine OR DataFine IS NULL)";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                patientsIds.add(rs.getInt("IDPaziente"));
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il recupero degli ID dei pazienti con terapie in corso: " + e.getMessage());
            throw new DataAccessException("Errore durante il recupero degli ID dei pazienti con terapie in corso", e);
        }
        return patientsIds;
    }


    /**
     * Salva una nuova terapia nel database. (Attore: Medico)
     *
     * (assegnaTerapia ed esitoAssegnaTerapia in Specifica / Modifica Terapia SD)
     *
     * @param terapia L'oggetto Terapia da salvare nel database.
     */
    public void assignTherapy(Terapia terapia) throws DataAccessException {
        String sql = "INSERT INTO Terapie (IDPaziente, IDMedico, NomeFarmaco, Quantita, FrequenzaGiornaliera, Indicazioni, DataInizio, DataFine) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            //rimosso IDTerapia perché è auto-incrementato nel database
            pstmt.setInt(1, terapia.getIDPaziente()); // reimpostato indexes
            pstmt.setInt(2, terapia.getIDMedico());
            pstmt.setString(3, terapia.getNomeFarmaco());
            pstmt.setString(4, terapia.getQuantita());
            pstmt.setInt(5, terapia.getFrequenzaGiornaliera());
            pstmt.setString(6, terapia.getIndicazioni());
            pstmt.setDate(7, terapia.getDataInizio());
            pstmt.setDate(8, terapia.getDataFine());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore durante l'assegnazione della terapia: " + e.getMessage());
            throw new DataAccessException("Errore durante l'assegnazione della terapia per il paziente con ID " + terapia.getIDPaziente(), e);
        }
    }
}