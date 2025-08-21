package org.univr.telemedicina.dao;

import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.Terapia;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;


public class TerapiaDAO {

    // trova le terapie associate a un paziente basandosi sul suo id
    // (visualizzaterapieprescritte ed esitovisualizzaterapieprescritte in visualizza terapia prescritta sd)
    // @param idutente l'idutente da cercare
    // @return un optional contenente l'utente se trovato altrimenti un optional vuoto
    // idpaziente
    public List<Terapia> listTherapiesByPatId(int IDUtente) throws DataAccessException {
        //
        List<Terapia> terapie = new ArrayList<>();


        String sql = "SELECT * FROM Terapie WHERE IDPaziente = ?";

        // try-with-resources per garantire la chiusura automatica delle risorse (connection preparedstatement resultset)
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // imposta il parametro della query (?) per evitare sql injection
            pstmt.setInt(1, IDUtente);

            try (ResultSet rs = pstmt.executeQuery()) {
                // se c'è un risultato...
                while (rs.next()) {
                    // ...crea un oggetto terapia e popola i suoi campi con i dati dal resultset
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

                    // aggiungi la terapia alla lista
                    terapie.add(terapia);
                }
            }
        } catch (SQLException e) {
            System.err.println("errore durante la ricerca della terapia per idpaziente " + e.getMessage());
            throw new DataAccessException("errore durante la ricerca delle terapie per il paziente con id " + IDUtente, e);
        }

        // se non viene trovato nessun utente o si verifica un errore ritorna una lista vuota
        return terapie;
    }

    // restituisce una lista di id univoci di tutti i pazienti che hanno almeno una terapia in corso
    // una terapia si considera in corso se la data odierna è compresa tra la datainizio e la datafine
    // gestisce anche il caso in cui la datafine non sia specificata (terapia a tempo indeterminato)
    // @return una lista di integer contenente gli id dei pazienti attivi
    public List<Integer> getActivePatientIds() throws DataAccessException {
        List<Integer> patientsIds = new ArrayList<>();

        // query che seleziona idpaziente univoci dove la data odierna
        // rientra nel range della terapia la funzione date('now') è specifica di sqlite
        String sql = "SELECT DISTINCT IDPaziente FROM Terapie " +
                "WHERE date('now') >= DataInizio AND (date('now') <= DataFine OR DataFine IS NULL)";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                patientsIds.add(rs.getInt("IDPaziente"));
            }
        } catch (SQLException e) {
            System.err.println("errore durante il recupero degli id dei pazienti con terapie in corso " + e.getMessage());
            throw new DataAccessException("errore durante il recupero degli id dei pazienti con terapie in corso", e);
        }
        return patientsIds;
    }

    // calcola la somma delle frequenze giornaliere di assunzioni farmaci per una lista di pazienti con una singola query
    // usato per checkfarmacidaily
    // @param patientids la lista degli id dei pazienti
    // @return una mappa dove la chiave è l'id del paziente e il valore è la sua frequenza totale richiesta
    // @throws dataaccessexception se si verifica un errore di accesso ai dati
    public Map<Integer, Integer> getFrequenzeGiornalierePerPazienti(List<Integer> patientIds) throws DataAccessException {
        if (patientIds == null || patientIds.isEmpty()) {
            return Collections.emptyMap(); // ritorna una mappa vuota se non ci sono pazienti
        }

        //inizializziamo la mappa per memorizzare le frequenze totali
        Map<Integer, Integer> mapFrequenze = new HashMap<>();

        // creiamo una stringa di placeholder (?,?,?) per la clausola in sarà tipo "531,532,533"
        // serve perche non sappiamo quanti pazienti ci sono quindi non possiamo usare un preparedstatement con un numero fisso di parametri
        String placeholders = String.join(",", Collections.nCopies(patientIds.size(), "?"));

        // la query usa sum e group by per fare il lavoro di aggregazione direttamente nel db
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
            throw new DataAccessException("errore nel recupero massivo delle frequenze giornaliere", e);
        }
        // ritorna la mappa con le frequenze totali per ogni paziente
        return mapFrequenze;
    }


    // salva una nuova terapia nel database (attore medico)
    // (assegnaterapia ed esitoassegnaterapia in specifica / modifica terapia sd)
    // @param terapia l'oggetto terapia da salvare nel database
    public void assignTherapy(Terapia terapia) throws DataAccessException {
        String sql = "INSERT INTO Terapie (IDPaziente, IDMedico, NomeFarmaco, Quantita, FrequenzaGiornaliera, Indicazioni, DataInizio, DataFine) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            //rimosso idterapia perché è auto-incrementato nel database
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
            System.err.println("errore durante l'assegnazione della terapia " + e.getMessage());
            throw new DataAccessException("errore durante l'assegnazione della terapia per il paziente con id " + terapia.getIDPaziente(), e);
        }
    }
    // aggiorna una terapia esistente nel database (attore medico)
    // (modificaterapia ed esitomodificaterapia in specifica / modifica terapia sd)
    // @param terapia l'oggetto terapia da aggiornare nel database
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
                throw new DataAccessException("modifica terapia fallita non è stata trovata nessuna terapia con id " + terapia.getIDTerapia(), null);
            }

        } catch (SQLException e) {
            System.err.println("errore durante l'aggiornamento della terapia " + e.getMessage());
            throw new DataAccessException("errore durante l'aggiornamento della terapia con id " + terapia.getIDTerapia(), e);
        }
    }

    public void delete(int idTerapia) throws DataAccessException {
        String sql = "DELETE FROM Terapie WHERE IDTerapia = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idTerapia);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DataAccessException("eliminazione terapia fallita non è stata trovata nessuna terapia con id " + idTerapia, null);
            }

        } catch (SQLException e) {
            System.err.println("errore durante l'eliminazione della terapia " + e.getMessage());
            throw new DataAccessException("errore durante l'eliminazione della terapia con id " + idTerapia, e);
        }
    }
}

