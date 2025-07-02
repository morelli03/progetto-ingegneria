package org.univr.telemedicina.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univr.telemedicina.dao.NotificheDAO;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.Notifica;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificheServiceTest {

    @Mock
    private NotificheDAO notificheDAO; // Il DAO finto

    @InjectMocks
    private NotificheService notificheService; // Il servizio reale con il DAO finto iniettato

    @Test
    void send_ChiamaCorrettamenteInserisciNotificaDelDao() throws DataAccessException {
        // ACT
        notificheService.send(1, 2, "Titolo", "Messaggio", "TIPO");

        // ASSERT
        // Verifica che il metodo `inserisciNotifica` del DAO sia stato chiamato una volta
        // con un qualsiasi oggetto di tipo Notifica.
        verify(notificheDAO, times(1)).inserisciNotifica(any(Notifica.class));
    }

    @Test
    void send_SeDaoLanciaEccezione_LaPropagaCorrettamente() throws DataAccessException {
        // ARRANGE
        // Configura il mock per lanciare un'eccezione quando viene chiamato
        doThrow(new DataAccessException("Errore DB")).when(notificheDAO).inserisciNotifica(any(Notifica.class));

        // ACT & ASSERT
        // Verifica che il servizio rilanci la stessa eccezione che ha ricevuto dal DAO
        assertThrows(DataAccessException.class, () -> {
            notificheService.send(1, 2, "Titolo", "Messaggio", "TIPO");
        });
    }

    @Test
    void read_ChiamaCorrettamenteLeggiNotifichePerIdDelDao() throws DataAccessException {
        // ARRANGE
        int idDestinatario = 1;
        // Crea una finta notifica da restituire
        Notifica notificaFinta = new Notifica(1, idDestinatario, 1, "Test", "Msg", "TIPO", 0, LocalDateTime.now());
        // Configura il mock per restituire la lista con la notifica finta
        when(notificheDAO.leggiNotifichePerId(idDestinatario)).thenReturn(Collections.singletonList(notificaFinta));

        // ACT
        List<Notifica> result = notificheService.read(idDestinatario);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).getTitolo());
        // Verifica che il metodo del DAO sia stato chiamato
        verify(notificheDAO, times(1)).leggiNotifichePerId(idDestinatario);
    }

    @Test
    void setNotificaLetta_ChiamaCorrettamenteIlDao() throws DataAccessException {
        // ARRANGE
        int idNotificaDaSegnare = 42;
        // Per i metodi void, non è necessario `when()`. Mockito verifica la chiamata.

        // ACT
        notificheService.setNotificaLetta(idNotificaDaSegnare);

        // ASSERT
        // Verifica che il metodo `setNotificaLetta` sul DAO sia stato chiamato
        // esattamente una volta con l'ID corretto.
        verify(notificheDAO, times(1)).setNotificaLetta(idNotificaDaSegnare);
    }

    @Test
    void setNotificaLetta_SeDaoLanciaEccezione_LaPropaga() throws DataAccessException {
        // ARRANGE
        int idNotifica = 1;
        doThrow(new DataAccessException("Impossibile aggiornare")).when(notificheDAO).setNotificaLetta(idNotifica);

        // ACT & ASSERT
        assertThrows(DataAccessException.class, () -> {
            notificheService.setNotificaLetta(idNotifica);
        });
    }

    @Test
    void cicloDiVitaNotifica_SendReadMarkAsRead() throws DataAccessException {
        // 1. ARRANGE (SEND)
        int idDestinatario = 5;

        // 2. ACT (SEND)
        notificheService.send(idDestinatario, 1, "Notifica di test", "Corpo del messaggio", "CICLO_VITA");

        // 3. ASSERT (SEND)
        verify(notificheDAO).inserisciNotifica(any(Notifica.class));

        // 4. ARRANGE (READ)
        Notifica notificaInviata = new Notifica(101, idDestinatario, 1, "Notifica di test", "Corpo del messaggio", "CICLO_VITA", 0, LocalDateTime.now());
        when(notificheDAO.leggiNotifichePerId(idDestinatario)).thenReturn(Collections.singletonList(notificaInviata));

        // 5. ACT (READ)
        List<Notifica> notificheNonLette = notificheService.read(idDestinatario);

        // 6. ASSERT (READ)
        assertEquals(1, notificheNonLette.size());
        assertEquals(101, notificheNonLette.get(0).getIdNotifica());

        // 7. ACT (MARK AS READ)
        int idNotificaDaLeggere = notificheNonLette.get(0).getIdNotifica();
        notificheService.setNotificaLetta(idNotificaDaLeggere);

        // 8. ASSERT (MARK AS READ)
        verify(notificheDAO).setNotificaLetta(idNotificaDaLeggere);

        // 9. VERIFICA FINALE: se leggo di nuovo le notifiche, la lista dovrebbe essere vuota
        when(notificheDAO.leggiNotifichePerId(idDestinatario)).thenReturn(Collections.emptyList());
        List<Notifica> notificheDopoLettura = notificheService.read(idDestinatario);
        assertTrue(notificheDopoLettura.isEmpty());
    }
}