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


// service layer per la logica di business legata alla gestione delle terapie
// responsabile di assegnare modificare e visualizzare le terapie di un paziente [cite 325 326]
// validandone i dati e interagendo con terapiadao per la persistenza
// [cite_start]traccia ogni operazione tramite logoperazionidao e gestisce gli errori con therapyexception [cite 324]
public class TerapiaService {

    private final TerapiaDAO terapiaDAO;
    private final LogOperazioniDAO logOperazioniDAO;

    public TerapiaService(TerapiaDAO terapiaDAO, LogOperazioniDAO logOperazioniDAO) {
        this.terapiaDAO = terapiaDAO;
        this.logOperazioniDAO = logOperazioniDAO;
    }

    // assegna una nuova teropia
    public void assegnaTerapia(int idPaziente, int idMedicoOperante, String nomeFarmaco, String quantita, int frequenzaGiornaliera, String indicazioni, LocalDate dataInizio, LocalDate dataFine) throws TherapyException {
        if (frequenzaGiornaliera <= 0) {
            throw new TherapyException("la frequenza giornaliera deve essere maggiore di zero");
        }
        try{
            Terapia nuovaTerapia = new Terapia(idPaziente, idMedicoOperante, nomeFarmaco, quantita, frequenzaGiornaliera, indicazioni, dataInizio, dataFine);

            terapiaDAO.assignTherapy(nuovaTerapia);

            String descrizione = "prescritto il farmaco  " + nuovaTerapia.getNomeFarmaco() + " al paziente id " + nuovaTerapia.getIDPaziente();
            registraOperazione(idMedicoOperante, nuovaTerapia.getIDPaziente(), "assegna terapia", descrizione);
        } catch (DataAccessException e) {
            throw new TherapyException("errore durante l'assegnazione della terapia " + e.getMessage(), e);
        }
    }

    // modifica una terapia esistente
    public void modificaTerapia(Terapia terapia, int idMedicoOperante) throws TherapyException {
        try {

            terapiaDAO.updateTherapy(terapia);

            String descrizione = "modificata la terapia per il paziente id " + terapia.getIDPaziente() + " con il farmaco " + terapia.getNomeFarmaco() + "per il paziente id " + terapia.getIDPaziente();
            registraOperazione(idMedicoOperante, terapia.getIDPaziente(), "modifica terapia", descrizione);
        } catch (DataAccessException e) {
            throw new TherapyException("errore durante la modifica della terapia " + e.getMessage(), e);
        }
    }

    public void eliminaTerapia(int idTerapia, int idMedicoOperante, int idPaziente) throws TherapyException {
        try {
            terapiaDAO.delete(idTerapia);
            String descrizione = "eliminata la terapia id " + idTerapia + " per il paziente id " + idPaziente;
            registraOperazione(idMedicoOperante, idPaziente, "elimina terapia", descrizione);
        } catch (DataAccessException e) {
            throw new TherapyException("errore durante l'eliminazione della terapia " + e.getMessage(), e);
        }
    }

    // registra un'operazione nel log delle operazioni
    private void registraOperazione(int idMedico, int idPaziente, String tipoOperazione, String descrizione) throws DataAccessException {
        LogOperazione log = new LogOperazione(idMedico, idPaziente, tipoOperazione, descrizione, LocalDateTime.now());
        logOperazioniDAO.createLog(log);
    }
}

