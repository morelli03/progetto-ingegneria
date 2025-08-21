package org.univr.telemedicina.service;

import org.univr.telemedicina.dao.NotificheDAO;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.Notifica;

import java.time.LocalDateTime;
import java.util.List;

// servizio che gestisce le notifiche
// scrive nel db legge dal db aggiorna lo stato di lettura delle notifiche
public class NotificheService {

    private final NotificheDAO notificheDAO;

    public NotificheService(NotificheDAO notificheDAO) {
        this.notificheDAO = notificheDAO;
    }

    // invia una notifica al destinatario specificato
    // @param iddestinatario l'id del destinatario della notifica
    // @param priorita       la priorità della notifica
    // @param titolo         il titolo della notifica
    // @param messaggio      il messaggio della notifica
    // @param tipo           il tipo di notifica
    public void send(int IdDestinatario, int priorita, String titolo, String messaggio, String tipo) throws DataAccessException {
        // implementazione per inviare una notifica

        // inizializza una nuova notifica con i parametri forniti
        Notifica notifica = new Notifica(IdDestinatario, priorita, titolo, messaggio, tipo, LocalDateTime.now());

        //manda la notifica al database
        notificheDAO.inserisciNotifica(notifica);

    }

    // legge le notifiche per un destinatario specifico
    // @param iddestinatario l'id del destinatario delle notifiche
    // @return una lista di notifiche per il destinatario specificato
    public List<Notifica> read(int idDestinatario) throws DataAccessException {
        // implementazione per leggere una notifica
        return notificheDAO.leggiNotifichePerId(idDestinatario);
    }

    // segna una notifica specifica come letta
    // @param idnotifica l'id della notifica da aggiornare
    // @throws dataaccessexception se si verifica un errore durante l'aggiornamento
    public void setNotificaLetta(int idNotifica) throws DataAccessException {
        // chiama direttamente il metodo corrispondente nel dao
        notificheDAO.setNotificaLetta(idNotifica);
    }
}
