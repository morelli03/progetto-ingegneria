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

// classe dao per trovare tutti i pazienti associati ad un medico e creare l'associazione utente-paziente
// quando si crea un utente paziente viene associato un medico di riferimento non viceversa
public class PazientiDAO {
    // crea l'associazione tra un utente e il ruolo di paziente
    // questo metodo va chiamato dopo aver creato un utente con successo e dopo aver creato l'oggetto paziente
    // con l'idpaziente preso dalla creazione dell'utente
    // va popolato anche idmedicoriferimento con l'id del medico di riferimento(viene passato quando si crea l'admin crea l'utente)
    // @param paziente oggetto paziente
    public void create(Paziente paziente) throws DataAccessException {
        String sql = "INSERT INTO Pazienti(IDPaziente, IDMedicoRiferimento) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, paziente.getIDPaziente());
            pstmt.setInt(2, paziente.getIDMedicoRiferimento());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("errore durante la creazione del paziente " + e.getMessage());
            throw new DataAccessException("errore durante la creazione del paziente con id " + paziente.getIDPaziente(), e);
        }
    }

    // restituisce una lista di pazienti associati a un medico specifico
    // @param idmedico l'id del medico di riferimento
    // @return una lista di pazienti associati al medico specificato
    public List<Utente> findPazientiByMedId(int IDMedico) throws DataAccessException {
        List<Utente> pazienti = new ArrayList<>();

        // query join per ottenere gli utenti associati a un medico
        String sql = "SELECT u.* FROM Utenti u " +
                "JOIN Pazienti p ON u.IDUtente = p.IDPaziente " +
                "WHERE p.IDMedicoRiferimento = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // imposta il parametro della query (?) per evitare sql injection
            pstmt.setInt(1, IDMedico);

            try(ResultSet rs = pstmt.executeQuery()){
                // se c'è un risultato...
                while (rs.next()) {
                    // ...crea un oggetto utente e popola i suoi campi con i dati dal resultset
                    Utente paziente = new Utente(
                            rs.getInt("IDUtente"),
                            rs.getString("Email"),
                            rs.getString("HashedPassword"),
                            rs.getString("Nome"),
                            rs.getString("Cognome"),
                            rs.getString("Ruolo"),
                            rs.getObject("DataNascita", LocalDate.class) // assicurati che il campo datanascita sia presente nella tabella utenti
                    );

                    // aggiungi il paziente alla lista
                    pazienti.add(paziente);
                }
            }
        } catch (SQLException e) {
            System.err.println("errore durante la ricerca dei pazienti per idmedico " + e.getMessage());
            throw new DataAccessException("errore durante la ricerca dei pazienti per il medico con id " + IDMedico, e);
        }
        return pazienti;
    }

    // restituisce l'id del medico di riferimento per un paziente specifico
    // @param idpaziente l'id del paziente di cui si vuole conoscere il medico di riferimento
    // @return l'id del medico di riferimento per il paziente specificato
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
            System.err.println("errore durante la ricerca del medico di riferimento per il paziente con id " + IDPaziente + " " + e.getMessage());
            throw new DataAccessException("errore durante la ricerca del medico di riferimento per il paziente con id " + IDPaziente, e);
        }
        return Optional.empty();
    }


    // restituisce una stringa con il nome del paziente dato il suo idpaziente
    // @param idpaziente l'id del paziente di cui si vuole ottenere il nome
    // @return una stringa contenente il nome e cognome del paziente o null se non trovato
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
            System.err.println("errore durante la ricerca del nome del paziente con id " + IDPaziente + " " + e.getMessage());
            throw new DataAccessException("errore durante la ricerca del nome del paziente con id " + IDPaziente, e);
        }
        return null;
    }


    // trova un idmedico da un idpaziente
    // @param idpaziente l'id del paziente di cui si vuole trovare il medico di riferimento
    // @return l'id del medico di riferimento associato al paziente
    // @throws dataaccessexception se si verifica un errore durante l'accesso ai dati
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
            System.err.println("errore durante la ricerca del medico per il paziente con id " + IDPaziente + " " + e.getMessage());
            throw new DataAccessException("errore durante la ricerca del medico per il paziente con id " + IDPaziente, e);
            }
        }
        throw new DataAccessException("nessun medico trovato per il paziente con id " + IDPaziente);

    }
}
