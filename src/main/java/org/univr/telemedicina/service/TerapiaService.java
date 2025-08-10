package org.univr.telemedicina.service;
import org.univr.telemedicina.dao.TerapiaDAO;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.exception.TherapyException;
import org.univr.telemedicina.model.Terapia;
import org.univr.telemedicina.model.LogOperazione;
import org.univr.telemedicina.dao.LogOperazioniDAO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


/**
 * Service layer per la logica di business legata alla gestione delle terapie.
 * Responsabile di assegnare, modificare e visualizzare le terapie di un paziente, [cite: 325, 326]
 * validandone i dati e interagendo con TerapiaDAO per la persistenza.
 * [cite_start]Traccia ogni operazione tramite LogOperazioniDAO e gestisce gli errori con TherapyException. [cite: 324]
 */

public class TerapiaService {

    private final TerapiaDAO terapiaDAO;
    private final LogOperazioniDAO logOperazioniDAO;

    public TerapiaService(TerapiaDAO terapiaDAO, LogOperazioniDAO logOperazioniDAO) {
        this.terapiaDAO = terapiaDAO;
        this.logOperazioniDAO = logOperazioniDAO;
    }

    /**
     * assegna una nuova teropia
     */
    public void assegnaTerapia(int idPaziente, int idMedicoOperante, String nomeFarmaco, String quantita, int frequenzaGiornaliera, String indicazioni, LocalDate dataInizio, LocalDate dataFine) throws TherapyException {
        if (frequenzaGiornaliera <= 0) {
            throw new TherapyException("La frequenza giornaliera deve essere maggiore di zero.");
        }
        try{
            Terapia nuovaTerapia = new Terapia(idPaziente, idMedicoOperante, nomeFarmaco, quantita, frequenzaGiornaliera, indicazioni, dataInizio, dataFine);

            terapiaDAO.assignTherapy(nuovaTerapia);

            String descrizione = "Prescritto il farmaco  " + nuovaTerapia.getNomeFarmaco() + " al paziente ID " + nuovaTerapia.getIDPaziente();
            registraOperazione(idMedicoOperante, nuovaTerapia.getIDPaziente(), "assegna terapia", descrizione);
        } catch (DataAccessException e) {
            throw new TherapyException("Errore durante l'assegnazione della terapia: " + e.getMessage(), e);
        }
    }

    /**
     * modifica una terapia esistente
     */
    public void modificaTerapia(Terapia terapia, int idMedicoOperante) throws TherapyException {
        try {

            terapiaDAO.updateTherapy(terapia);

            String descrizione = "Modificata la terapia per il paziente ID " + terapia.getIDPaziente() + " con il farmaco " + terapia.getNomeFarmaco() + "per il paziente ID " + terapia.getIDPaziente();
            registraOperazione(idMedicoOperante, terapia.getIDPaziente(), "modifica terapia", descrizione);
        } catch (DataAccessException e) {
            throw new TherapyException("Errore durante la modifica della terapia: " + e.getMessage(), e);
        }
    }

    public void eliminaTerapia(int idTerapia, int idMedicoOperante, int idPaziente) throws TherapyException {
        try {
            terapiaDAO.delete(idTerapia);
            String descrizione = "Eliminata la terapia ID " + idTerapia + " per il paziente ID " + idPaziente;
            registraOperazione(idMedicoOperante, idPaziente, "elimina terapia", descrizione);
        } catch (DataAccessException e) {
            throw new TherapyException("Errore durante l'eliminazione della terapia: " + e.getMessage(), e);
        }
    }

    /**
     * registra un'operazione nel log delle operazioni
     */
    private void registraOperazione(int idMedico, int idPaziente, String tipoOperazione, String descrizione) throws DataAccessException {
        LogOperazione log = new LogOperazione(idMedico, idPaziente, tipoOperazione, descrizione, LocalDateTime.now());
        logOperazioniDAO.createLog(log);
    }
}

