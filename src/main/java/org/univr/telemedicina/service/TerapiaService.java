package org.univr.telemedicina.service;
import org.univr.telemedicina.dao.TerapiaDAO;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.exception.TherapyException;
import org.univr.telemedicina.model.Terapia;
import org.univr.telemedicina.model.LogOperazione;
import org.univr.telemedicina.dao.LogOperazioniDAO;

import java.time.LocalDateTime;
import java.util.List;


//modificare TerapiaDAO - non contiene un metodo per aggiornare la terapia
//gestisce le eccezioni legate alle terapie
//

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
    public void assegnaTerapia(Terapia terapia, int idMedicoOperante) throws TherapyException {
        if (terapia.getFrequenzaGiornaliera() <= 0) {
            throw new TherapyException("La frequenza giornaliera deve essere maggiore di zero.");
        }
        try{
            terapia.setIDMedico(idMedicoOperante);

            terapiaDAO.assignTherapy(terapia);

            String descrizione = "Prescritto il farmaco  " + terapia.getNomeFarmaco() + " al paziente ID " + terapia.getIDPaziente();
            registraOperazione(idMedicoOperante, terapia.getIDPaziente(), "assegna terapia", descrizione);
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

            String descrizione = "Modificata la terapia per il paziente ID " + terapia.getIDPaziente();
            registraOperazione(idMedicoOperante, terapia.getIDPaziente(), "modifica terapia", descrizione);
        } catch (DataAccessException e) {
            throw new TherapyException("Errore durante la modifica della terapia: " + e.getMessage(), e);
        }
    }

    /**
     *Lista di tuttel le terapie assegnate a un paziente
     */
    public List<Terapia> getTerapieByPazienteId(int idPaziente) throws TherapyException {
        try {
            return terapiaDAO.listTherapiesByPatId(idPaziente);
        } catch (DataAccessException e) {
            throw new TherapyException("Errore durante il recupero delle terapie: " + e.getMessage(), e);
        }
    }

    /**
     * registra un'operazione nel log delle operazioni
     */
    private void registraOperazione(int idMedico, int idPaziente, String tipoOperazione, String descrizione) throws DataAccessException {
        LogOperazione log = new LogOperazione(idMedico, tipoOperazione, descrizione, LocalDateTime.now());
        log.setIDPazienteInteressato(idPaziente);
        logOperazioniDAO.createLog(log);
    }
}

