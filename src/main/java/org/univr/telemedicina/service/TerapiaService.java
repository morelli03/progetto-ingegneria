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

public class TerapiaService {

    private final TerapiaDAO terapiaDAO;
    private final LogOperazioniDAO logOperazioniDAO;

    public TerapiaService(TerapiaDAO terapiaDAO, LogOperazioniDAO logOperazioniDAO) {
        this.terapiaDAO = terapiaDAO;
        this.logOperazioniDAO = logOperazioniDAO;
    }

    /**
     * assegnare una nuova terapia a un paziente
     */
    //manca la throw per il metodo
    public void assegnaTerapia(Terapia terapia, int idMedico) throws DataAccessException {

        if (terapia.getFrequenzaGiornaliera() <= 0) {
            throw new IllegalArgumentException("La frequenza giornaliera deve essere maggiore di zero.");
        }

        //salvo la terapia
        terapiaDAO.assignTherapy(terapia);

        //registro l'operazione
        String desczione = "Precritta nuova terpaia " + terapia.getNomeFarmaco() + " al paziente ID " + terapia.getIDPaziente();
        registraOperazione(terapia.getIDMedico(), terapia.getIDPaziente(), "prescrizione terapia", desczione);

    }

    /**
     * modificare una terapia esistente
     */

    //chiama metodo updateTherapy non ancora implementato in TerapiaDAO
    public void modificaTerapia(Terapia terapia){


        //aggiorno la terapia
        terapiaDAO.updateTherapy(terapia);

        //registro l'operazione
        String desczione = "Modificata terapia " + terapia.getNomeFarmaco() + " al paziente ID " + terapia.getIDPaziente();
        registraOperazione(terapia.getIDMedico(), terapia.getIDPaziente(), "modifica terapia", desczione);

    }

    /**
     * ritorna la lista di terapie associate a un paziente
     */
    //anche questa dovrebbe lanciare una eccezzione
    public List<Terapia> getTerapieByPaziente(int idPaziente) throws DataAccessException {
        return terapiaDAO.listTherapiesByPatId(idPaziente);
    }

    /**
     * metodo per creare e salvare log di un'operazione
     */

    private void registraOperazione(int idMedico, int idPaziente, String tipoOperazione, String descrizione) {
        LogOperazione log = new LogOperazione();
        log.setIDMedicoOperante(idMedico);
        log.setIDPazienteInteressato(idPaziente);
        log.setTipoOperazione(tipoOperazione);
        log.setDescrizioneOperazione(descrizione);
        log.setTimestamp(LocalDateTime.now());
    }
}
