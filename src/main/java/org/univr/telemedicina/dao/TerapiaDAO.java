package org.univr.telemedicina.dao;

import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.Terapia;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;


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
                            rs.getObject("DataInizio", LocalDate.class),
                            rs.getObject("DataFine", LocalDate.class)
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
     * Calcola la somma delle frequenze giornaliere di assunzioni farmaci per una lista di pazienti con una singola query.
     * Usato per checkFarmaciDaily
     * @param patientIds La lista degli ID dei pazienti.
     * @return Una mappa dove la chiave è l'ID del paziente e il valore è la sua frequenza totale richiesta.
     * @throws DataAccessException Se si verifica un errore di accesso ai dati.
     */
    public Map<Integer, Integer> getFrequenzeGiornalierePerPazienti(List<Integer> patientIds) throws DataAccessException {
        if (patientIds == null || patientIds.isEmpty()) {
            return Collections.emptyMap(); // Ritorna una mappa vuota se non ci sono pazienti
        }

        //inizializziamo la mappa per memorizzare le frequenze totali
        Map<Integer, Integer> mapFrequenze = new HashMap<>();

        // creiamo una stringa di placeholder (?,?,?) per la clausola IN, sarà tipo: "531,532,533"
        // serve perche non sappiamo quanti pazienti ci sono, quindi non possiamo usare un PreparedStatement con un numero fisso di parametri
        String placeholders = String.join(",", Collections.nCopies(patientIds.size(), "?"));

        // la query usa SUM e GROUP BY per fare il lavoro di aggregazione direttamente nel DB
        String sql = "SELECT IDPaziente, SUM(FrequenzaGiornaliera) as FrequenzaTotale " +
                "FROM Terapie WHERE IDPaziente IN (" + placeholders + ") GROUP BY IDPaziente";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // impostiamo i valori per ogni placeholder
            int index = 1;
            for (Integer id : patientIds) {
                pstmt.setInt(index++, id);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    mapFrequenze.put(rs.getInt("IDPaziente"), rs.getInt("FrequenzaTotale"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Errore nel recupero massivo delle frequenze giornaliere.", e);
        }
        // ritorna la mappa con le frequenze totali per ogni paziente
        return mapFrequenze;
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
            pstmt.setObject(7, terapia.getDataInizio());
            pstmt.setObject(8, terapia.getDataFine());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore durante l'assegnazione della terapia: " + e.getMessage());
            throw new DataAccessException("Errore durante l'assegnazione della terapia per il paziente con ID " + terapia.getIDPaziente(), e);
        }
    }
    /**
     * Aggiorna una terapia esistente nel database. (Attore: Medico)
     *
     * (modificaTerapia ed esitoModificaTerapia in Specifica / Modifica Terapia SD)
     *
     * @param terapia L'oggetto Terapia da aggiornare nel database.
     */
    public void updateTherapy(Terapia terapia) throws DataAccessException {
        String sql = "UPDATE Terapie SET IDPaziente = ?, IDMedico = ?, NomeFarmaco = ?, Quantita = ?, FrequenzaGiornaliera = ?, Indicazioni = ?, DataInizio = ?, DataFine = ? WHERE IDTerapia = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, terapia.getIDPaziente());
            pstmt.setInt(2, terapia.getIDMedico());
            pstmt.setString(3, terapia.getNomeFarmaco());
            pstmt.setString(4, terapia.getQuantita());
            pstmt.setInt(5, terapia.getFrequenzaGiornaliera());
            pstmt.setString(6, terapia.getIndicazioni());
            pstmt.setObject(7, terapia.getDataInizio());
            pstmt.setObject(8, terapia.getDataFine());
            pstmt.setInt(9, terapia.getIDTerapia());

            int affectedRows = pstmt.executeUpdate();

            //verifa che la terapia sia stata effettivamente aggiornata
            if(affectedRows == 0){
                throw new DataAccessException("Modifica terapia fallita, non è stata trovata nessuna terapia con ID " + terapia.getIDTerapia(), null);
            }

        } catch (SQLException e) {
            System.err.println("Errore durante l'aggiornamento della terapia: " + e.getMessage());
            throw new DataAccessException("Errore durante l'aggiornamento della terapia con ID " + terapia.getIDTerapia(), e);
        }
    }

    public void delete(int idTerapia) throws DataAccessException {
        String sql = "DELETE FROM Terapie WHERE IDTerapia = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idTerapia);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DataAccessException("Eliminazione terapia fallita, non è stata trovata nessuna terapia con ID " + idTerapia, null);
            }

        } catch (SQLException e) {
            System.err.println("Errore durante l'eliminazione della terapia: " + e.getMessage());
            throw new DataAccessException("Errore durante l'eliminazione della terapia con ID " + idTerapia, e);
        }
    }
}

