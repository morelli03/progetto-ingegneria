package org.univr.telemedicina.dao;

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
    public List<Terapia> listTherapiesByPatId(int IDUtente) {
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
        }
        // Se non viene trovato nessun utente o si verifica un errore, ritorna un Optional vuoto
        return terapie;
    }

    /**
     * Salva una nuova terapia nel database. (Attore: Medico)
     *
     * (assegnaTerapia ed esitoAssegnaTerapia in Specifica / Modifica Terapia SD)
     *
     * @param terapia L'oggetto Terapia da salvare
     */
    public void assignTherapy(Terapia terapia) {
        String sql = "INSERT INTO Terapie (IDPaziente, IDMedico, NomeFarmaco, Quantita, FrequenzaGiornaliera, Indicazioni, DataInizio, DataFine) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, terapia.getIDTerapia());
            pstmt.setInt(2, terapia.getIDPaziente());
            pstmt.setInt(3, terapia.getIDMedico());
            pstmt.setString(4, terapia.getNomeFarmaco());
            pstmt.setString(5, terapia.getQuantita());
            pstmt.setInt(6, terapia.getFrequenzaGiornaliera());
            pstmt.setString(7, terapia.getIndicazioni());
            pstmt.setDate(8, new java.sql.Date(terapia.getDataInizio().getTime()));
            pstmt.setDate(9, new java.sql.Date(terapia.getDataFine().getTime()));

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore durante l'assegnazione della terapia: " + e.getMessage());
        }
    }
}