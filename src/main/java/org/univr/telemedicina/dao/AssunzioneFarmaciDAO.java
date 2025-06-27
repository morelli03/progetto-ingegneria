package org.univr.telemedicina.dao;

import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.AssunzioneFarmaci;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * Classe DAO per gestire le operazioni di accesso ai dati relativi alle assunzioni di farmaci.
 * Deve aggiungere una assunzione da parte di una paziente, e legge tutte assunzioni di farmaci per paziente,
 * e assunzioni di un determinato giorno.
 */
public class AssunzioneFarmaciDAO {

    /**
     * Legge tute le assunzioni di farmaci per un paziente specifico.
     * @param IDPaziente L'ID del paziente
     * @return Una List contenente le AssunzioniFarmaci per il paziente specificato.
     */
    public List<AssunzioneFarmaci> leggiAssunzioniFarmaci(int IDPaziente) throws DataAccessException {

        // Lista per memorizzare le assunzioni di farmaci
        List<AssunzioneFarmaci> assunzioni = new ArrayList<>();

        String sql = "SELECT * FROM AssunzioniFarmaci WHERE IDPaziente = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

            //imposto il parametro ? per evitare sql injection
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
            System.err.println("Errore durante la lettura dei farmaci assunti: " + e.getMessage());
            throw new DataAccessException("Errore durante la ricerca delle assunzioni per il paziente con ID " + IDPaziente, e);
        }
        // Se non viene trovata nessuna assunzione o si verifica un errore, ritorna una lista vuota
        return assunzioni;
    }

     /**
      * Legge le assunzioni di farmaci in un giorno per un paziente specifico.
      * @param IDPaziente L'ID del paziente
      * @param data La data per cui si vogliono leggere le assunzioni
      * @return Una List contenente le AssunzioniFarmaci per il paziente specificato.
      */
     public List<AssunzioneFarmaci> leggiAssunzioniGiorno(int IDPaziente, LocalDate data) throws DataAccessException {
         List<AssunzioneFarmaci> assunzioni = new ArrayList<>();

         // Query SQL per leggere le assunzioni di farmaci in un giorno specifico
         String sql =   "SELECT * FROM AssunzioniFarmaci WHERE IDPaziente = ? " +
                        "AND TimestampAssunzione >= ?" +
                        " AND TimestampAssunzione < ?";

         // Definisce l'intervallo di tempo per l'intera giornata
         LocalDateTime inizioGiorno = data.atStartOfDay(); // Es. 2025-06-11T00:00:00
         LocalDateTime inizioGiornoSuccessivo = data.plusDays(1).atStartOfDay(); // Es. 2025-06-12T00:00:00

         try (Connection conn = DatabaseManager.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)){

             //imposto il parametro ? per evitare sql injection
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
             System.err.println("Errore durante la lettura dei farmaci assunti: " + e.getMessage());
             //lancio l'eccezione personalizzata DataAccessException
             throw new DataAccessException("Errore durante la ricerca delle assunzioni per il giorno " + data, e);
         }
         // Se non viene trovata nessuna assunzione o si verifica un errore, ritorna una lista vuota
         return assunzioni;
     }

    /**
     * Aggiunge una nuova assunzione di farmaci per un paziente.
     * @param assunzione L'oggetto AssunzioneFarmaci da aggiungere
     */
    public void aggiungiAssunzione(AssunzioneFarmaci assunzione) throws DataAccessException {
        String sql = "INSERT INTO AssunzioniFarmaci(IDTerapia, IDPaziente, TimestampAssunzione, QuantitaAssunta) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Imposta i parametri della query
            pstmt.setInt(1, assunzione.getIDTerapia());
            pstmt.setInt(2, assunzione.getIDPaziente());
            pstmt.setObject(3, assunzione.getTimestampAssunzione());
            pstmt.setString(4, assunzione.getQuantitaAssunta());

            // Esegue l'inserimento
            pstmt.executeUpdate();

        } catch (SQLException e){
            System.err.println("Errore durante l'aggiunta di assunzione di farmaci: " + e.getMessage());
            throw new DataAccessException("Errore durante l'aggiunta dell'assunzione di farmaci per il paziente con ID " + assunzione.getIDPaziente(), e);
        }
    }
}
