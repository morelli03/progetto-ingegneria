package org.univr.telemedicina.service;

import org.univr.telemedicina.dao.NotificheDAO;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.Notifica;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servizio che gestisce le notifiche.
 * Scrive nel db, legge dal db, aggiorna lo stato di lettura delle notifiche.
 */
public class NotificheService {

    NotificheDAO notificheDAO = new NotificheDAO();

    public void send(int IdDestinatario, int priorita, String titolo, String messaggio, String tipo) {
        // Implementazione per inviare una notifica

        // inizializza una nuova notifica con i parametri forniti
        Notifica notifica = new Notifica(IdDestinatario, priorita, titolo, messaggio, tipo, LocalDateTime.now());

        //manda la notifica al database
        try {
            notificheDAO.inserisciNotifica(notifica);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Notifica> read(int idDestinatario) {
        // Implementazione per leggere una notifica
        try {
            return notificheDAO.leggiNotifichePerId(idDestinatario);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
