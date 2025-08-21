package org.univr.telemedicina.dao;

import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.AssunzioneFarmaci;

import java.sql.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

// classe dao per gestire le operazioni di accesso ai dati relativi alle assunzioni di farmaci
// deve aggiungere una assunzione da parte di una paziente e legge tutte assunzioni di farmaci per paziente
// e assunzioni di un determinato giorno
public class AssunzioneFarmaciDAO {

    // legge tute le assunzioni di farmaci per un paziente specifico
    // @param idPaziente l'id del paziente
    // @return una list contenente le assunzionifarmaci per il paziente specificato
    public List<AssunzioneFarmaci> leggiAssunzioniFarmaci(int IDPaziente) throws DataAccessException {

        // lista per memorizzare le assunzioni di farmaci
        List<AssunzioneFarmaci> assunzioni = new ArrayList<>();

        String sql = "SELECT * FROM AssunzioniFarmaci WHERE IDPaziente = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

            // imposto il parametro per evitare sql injection
            pstmt.setInt(1, IDPaziente);

            try(ResultSet rs = pstmt.executeQuery()) {

                while(rs.next()){
                    AssunzioneFarmaci as = new AssunzioneFarmaci(
                            rs.getInt("IDAssunzione"),
                            rs.getInt("IDTerapia"),
                            rs.getInt("IDPaziente"),
                            rs.getObject("TimestampAssunzione", LocalDateTime.class),
                            rs.getString("QuantitaAssunta")
                    );
                    assunzioni.add(as);
                }
            }
        } catch(SQLException e){
            System.err.println("errore durante la lettura dei farmaci assunti " + e.getMessage());
            throw new DataAccessException("errore durante la ricerca delle assunzioni per il paziente con id " + IDPaziente, e);
        }
        // se non viene trovata nessuna assunzione o si verifica un errore ritorna una lista vuota
        return assunzioni;
    }

    // legge le assunzioni di farmaci in un giorno per un paziente specifico
    // @param idPaziente l'id del paziente
    // @param data la data per cui si vogliono leggere le assunzioni
    // @return una list contenente le assunzionifarmaci per il paziente specificato
     public List<AssunzioneFarmaci> leggiAssunzioniGiorno(int IDPaziente, LocalDate data) throws DataAccessException {
         List<AssunzioneFarmaci> assunzioni = new ArrayList<>();

         // query sql per leggere le assunzioni di farmaci in un giorno specifico
         String sql =   "SELECT * FROM AssunzioniFarmaci WHERE IDPaziente = ? " +
                        "AND TimestampAssunzione >= ?" +
                        " AND TimestampAssunzione < ?";

         // definisce l'intervallo di tempo per l'intera giornata
         LocalDateTime inizioGiorno = data.atStartOfDay(); // es 2025-06-11t00:00:00
         LocalDateTime inizioGiornoSuccessivo = data.plusDays(1).atStartOfDay(); // es 2025-06-12t00:00:00

         try (Connection conn = DatabaseManager.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)){

             // imposto il parametro per evitare sql injection
             pstmt.setInt(1, IDPaziente);
             pstmt.setObject(2, inizioGiorno);
                pstmt.setObject(3, inizioGiornoSuccessivo);

             try(ResultSet rs = pstmt.executeQuery()) {
                 while(rs.next()){
                     AssunzioneFarmaci as = new AssunzioneFarmaci(
                             rs.getInt("IDAssunzione"),
                             rs.getInt("IDTerapia"),
                             rs.getInt("IDPaziente"),
                             rs.getObject("TimestampAssunzione", LocalDateTime.class),
                             rs.getString("QuantitaAssunta")
                     );
                     assunzioni.add(as);
                 }
             }
         } catch(SQLException e){
             System.err.println("errore durante la lettura dei farmaci assunti " + e.getMessage());
             // lancio l'eccezione personalizzata dataaccessexception
             throw new DataAccessException("errore durante la ricerca delle assunzioni per il giorno " + data, e);
         }
         // se non viene trovata nessuna assunzione o si verifica un errore ritorna una lista vuota
         return assunzioni;
     }


    // conta le assunzioni di farmaci effettuate in un dato giorno per una lista di pazienti con una singola query
    // usato per checkfarmacidaily
    // @param patientids la lista degli id dei pazienti
    // @param data il giorno da controllare
    // @return una mappa dove la chiave è l'id del paziente e il valore è il numero di assunzioni effettuate
    // @throws dataaccessexception se si verifica un errore di accesso ai dati
    public Map<Integer, Integer> getConteggioAssunzioniGiornoPerPazienti(List<Integer> patientIds, LocalDate data) throws DataAccessException {
        if (patientIds == null || patientIds.isEmpty()) {
            return Collections.emptyMap(); // ritorna una mappa vuota se non ci sono pazienti
        }

        // inizializza la mappa per memorizzare il conteggio delle assunzioni
        Map<Integer, Integer> mapConteggio = new HashMap<>();
        // crea un placeholder per ogni id paziente nella query
        String placeholders = String.join(",", Collections.nCopies(patientIds.size(), "?"));

        // la query usa count e group by
        String sql = "SELECT IDPaziente, COUNT(*) as ConteggioAssunzioni " +
                "FROM AssunzioniFarmaci WHERE IDPaziente IN (" + placeholders + ") " +
                "AND TimestampAssunzione >= ? AND TimestampAssunzione < ? " +
                "GROUP BY IDPaziente";

        // definisce l'intervallo di tempo per l'intera giornata
        LocalDateTime inizioGiorno = data.atStartOfDay();
        LocalDateTime inizioGiornoSuccessivo = data.plusDays(1).atStartOfDay();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // imposta i parametri per la query
            int index = 1;
            for (Integer id : patientIds) {
                pstmt.setInt(index++, id);
            }
            // imposta i parametri per l'intervallo di tempo
            pstmt.setObject(index++, inizioGiorno);
            pstmt.setObject(index, inizioGiornoSuccessivo);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    mapConteggio.put(rs.getInt("IDPaziente"), rs.getInt("ConteggioAssunzioni"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("errore nel recupero del conteggio assunzioni", e);
        }
        return mapConteggio;
    }


    // aggiunge una nuova assunzione di farmaci per un paziente
    // @param assunzione l'oggetto assunzionefarmaci da aggiungere
    public void aggiungiAssunzione(AssunzioneFarmaci assunzione) throws DataAccessException {
        String sql = "INSERT INTO AssunzioniFarmaci(IDTerapia, IDPaziente, TimestampAssunzione, QuantitaAssunta) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // imposta i parametri della query
            pstmt.setInt(1, assunzione.getIDTerapia());
            pstmt.setInt(2, assunzione.getIDPaziente());
            pstmt.setObject(3, assunzione.getTimestampAssunzione());
            pstmt.setString(4, assunzione.getQuantitaAssunta());

            // esegue l'inserimento
            pstmt.executeUpdate();

        } catch (SQLException e){
            System.err.println("errore durante l'aggiunta di assunzione di farmaci " + e.getMessage());
            throw new DataAccessException("errore durante l'aggiunta dell'assunzione di farmaci per il paziente con id " + assunzione.getIDPaziente(), e);
        }
    }
}
