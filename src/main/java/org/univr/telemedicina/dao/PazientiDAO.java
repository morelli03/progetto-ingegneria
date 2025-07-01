package org.univr.telemedicina.dao;

import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.Paziente;
import org.univr.telemedicina.model.Utente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Classe DAO per trovare tutti i pazienti associati ad un medico e creare l'associazione utente-paziente.
 * Quando si crea un utente paziente, viene associato un medico di riferimento. non viceversa.
 */
public class PazientiDAO {
    /**
     * Crea l'associazione tra un Utente e il ruolo di Paziente.
     * Questo metodo va chiamato DOPO aver creato un Utente con successo e dopo aver creato l'oggetto paziente
     * con l'IDPaziente preso dalla creazione dell'utente.
     * Va popolato anche IDMedicoRiferimento con l'ID del medico di riferimento.(viene passato quando si crea l'admin crea l'utente)
     * @param paziente oggetto paziente
     */
    public void create(Paziente paziente) throws DataAccessException {
        String sql = "INSERT INTO Pazienti(IDPaziente, IDMedicoRiferimento) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, paziente.getIDPaziente());
            pstmt.setInt(2, paziente.getIDMedicoRiferimento());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore durante la creazione del paziente: " + e.getMessage());
            throw new DataAccessException("Errore durante la creazione del paziente con ID " + paziente.getIDPaziente(), e);
        }
    }

    /**
     * Restituisce una lista di pazienti associati a un medico specifico.
     * @param IDMedico L'ID del medico di riferimento.
     * @return Una lista di Pazienti associati al medico specificato.
     */
    public List<Utente> findPazientiByMedId(int IDMedico) throws DataAccessException {
        List<Utente> pazienti = new ArrayList<>();

        // Query JOIN per ottenere gli utenti associati a un medico
        String sql = "SELECT u.* FROM Utenti u " +
                "JOIN Pazienti p ON u.IDUtente = p.IDPaziente " +
                "WHERE p.IDMedicoRiferimento = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Imposta il parametro della query (?) per evitare SQL Injection
            pstmt.setInt(1, IDMedico);

            try(ResultSet rs = pstmt.executeQuery()){
                // Se c'è un risultato...
                while (rs.next()) {
                    // ...crea un oggetto Utente e popola i suoi campi con i dati dal ResultSet
                    Utente paziente = new Utente(
                            rs.getInt("IDUtente"),
                            rs.getString("Email"),
                            rs.getString("HashedPassword"),
                            rs.getString("Nome"),
                            rs.getString("Cognome"),
                            rs.getString("Ruolo"),
                            rs.getObject("DataNascita", LocalDate.class) // Assicurati che il campo DataNascita sia presente nella tabella Utenti
                    );

                    // Aggiungi il paziente alla lista
                    pazienti.add(paziente);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la ricerca dei pazienti per IDMedico: " + e.getMessage());
            throw new DataAccessException("Errore durante la ricerca dei pazienti per il medico con ID " + IDMedico, e);
        }
        return pazienti;
    }

    /**
     * Restituisce l'id del medico di riferimento per un paziente specifico.
     * @param IDPaziente L'ID del paziente di cui si vuole conoscere il medico di riferimento.
     * @return L'ID del medico di riferimento per il paziente specificato.
     */
    public Optional<Integer> getMedicoRiferimentoByPazienteId(int IDPaziente) throws DataAccessException {
        String sql = "SELECT IDMedicoRiferimento FROM Pazienti WHERE IDPaziente = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, IDPaziente);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(rs.getInt("IDMedicoRiferimento"));
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la ricerca del medico di riferimento per il paziente con ID " + IDPaziente + ": " + e.getMessage());
            throw new DataAccessException("Errore durante la ricerca del medico di riferimento per il paziente con ID " + IDPaziente, e);
        }
        return Optional.empty();
    }


    /**
     * Restituisce una stringa con il nome del paziente dato il suo IDPaziente.
     * @param IDPaziente L'ID del paziente di cui si vuole ottenere il nome.
     * @return Una stringa contenente il nome e cognome del paziente, o null se non trovato.
     */
    public String findNameById(int IDPaziente) throws DataAccessException {
        String sql = "SELECT Nome, Cognome FROM Utenti WHERE IDUtente = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, IDPaziente);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("Nome") + " " + rs.getString("Cognome");
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la ricerca del nome del paziente con ID " + IDPaziente + ": " + e.getMessage());
            throw new DataAccessException("Errore durante la ricerca del nome del paziente con ID " + IDPaziente, e);
        }
        return null;
    }


     /**
     * Trova un IDMedico da un IDPaziente
     * @param IDPaziente L'ID del paziente di cui si vuole trovare il medico di riferimento
     * @return L'ID del medico di riferimento associato al paziente
     * @throws DataAccessException Se si verifica un errore durante l'accesso ai dati
     */
    public int findMedByIDPaziente(int IDPaziente) throws DataAccessException, SQLException {
        String sql = "SELECT IDMedicoRiferimento FROM Pazienti WHERE IDPaziente = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, IDPaziente);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("IDMedicoRiferimento");
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la ricerca del medico per il paziente con ID " + IDPaziente + ": " + e.getMessage());
            throw new DataAccessException("Errore durante la ricerca del medico per il paziente con ID " + IDPaziente, e);
            }
        }
        throw new DataAccessException("Nessun medico trovato per il paziente con ID " + IDPaziente);

    }
}
