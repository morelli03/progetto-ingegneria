package org.univr.telemedicina.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univr.telemedicina.dao.LogOperazioniDAO;
import org.univr.telemedicina.dao.TerapiaDAO;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.exception.TherapyException;
import org.univr.telemedicina.model.LogOperazione;
import org.univr.telemedicina.model.Terapia;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TerapiaServiceTest {

    @Mock
    private TerapiaDAO terapiaDAO;
    @Mock
    private LogOperazioniDAO logOperazioniDAO;

    @InjectMocks
    private TerapiaService terapiaService;

    @Test
    void assegnaTerapia_Successo() throws TherapyException, DataAccessException {
        // ARRANGE
        int idPaziente = 1;
        int idMedico = 2;
        String nomeFarmaco = "Aspirina";

        // ACT
        terapiaService.assegnaTerapia(idPaziente, idMedico, nomeFarmaco, "100mg", 1, "Indicazioni", LocalDate.now(), LocalDate.now().plusDays(10));

        // ASSERT
        verify(terapiaDAO, times(1)).assignTherapy(any(Terapia.class));
        verify(logOperazioniDAO, times(1)).createLog(any(LogOperazione.class));
    }

    @Test
    void assegnaTerapia_FrequenzaNonValida_LanciaEccezione() {
        // ARRANGE
        int frequenzaInvalida = 0;

        // ACT & ASSERT
        TherapyException exception = assertThrows(TherapyException.class, () -> {
            terapiaService.assegnaTerapia(1, 2, "Farmaco", "100mg", frequenzaInvalida, "Indicazioni", LocalDate.now(), LocalDate.now().plusDays(10));
        });

        assertTrue(exception.getMessage().contains("frequenza giornaliera deve essere maggiore di zero"));
        verifyNoInteractions(terapiaDAO, logOperazioniDAO);
    }

    @Test
    void modificaTerapia_Successo() throws TherapyException, DataAccessException {
        // ARRANGE
        // Creiamo una terapia esistente da modificare
        Terapia terapiaDaModificare = new Terapia(1, 1, 2, "Aspirina", "100mg", 1, "Vecchie indicazioni", LocalDate.now(), LocalDate.now().plusDays(5));
        int idMedicoOperante = 2;

        // Non è necessario usare when() per i metodi void, Mockito li gestisce di default.
        // Mockito si assicurerà che i metodi vengano chiamati.

        // ACT
        terapiaService.modificaTerapia(terapiaDaModificare, idMedicoOperante);

        // ASSERT
        // 1. Verifica che il metodo di aggiornamento sul DAO sia stato chiamato esattamente una volta.
        verify(terapiaDAO, times(1)).updateTherapy(any(Terapia.class));

        // 2. Verifica che l'operazione sia stata registrata nel log esattamente una volta.
        ArgumentCaptor<LogOperazione> logCaptor = ArgumentCaptor.forClass(LogOperazione.class);
        verify(logOperazioniDAO, times(1)).createLog(logCaptor.capture());

        // 3. Controlla che i dettagli del log siano corretti.
        LogOperazione logCreato = logCaptor.getValue();
        assertEquals(idMedicoOperante, logCreato.getIDMedicoOperante());
        assertEquals(terapiaDaModificare.getIDPaziente(), logCreato.getIDPazienteInteressato());
        assertEquals("modifica terapia", logCreato.getTipoOperazione());
    }

    @Test
    void modificaTerapia_DaoLanciaEccezione_RilanciaEccezione() throws DataAccessException {
        // ARRANGE
        Terapia terapiaDaModificare = new Terapia(1, 1, 2, "Farmaco", "50mg", 1, "Indicazioni", LocalDate.now(), LocalDate.now().plusDays(10));
        int idMedicoOperante = 2;

        // Configura il mock del DAO per lanciare un'eccezione quando si tenta di aggiornare
        doThrow(new DataAccessException("Errore di connessione al DB")).when(terapiaDAO).updateTherapy(any(Terapia.class));

        // ACT & ASSERT
        // Verifica che il servizio catturi la DataAccessException e la converta in una TherapyException
        TherapyException exception = assertThrows(TherapyException.class, () -> {
            terapiaService.modificaTerapia(terapiaDaModificare, idMedicoOperante);
        });

        // Controlla il messaggio dell'eccezione
        assertTrue(exception.getMessage().contains("Errore durante la modifica della terapia"));

        // Assicurati che, in caso di fallimento, non venga creato nessun log
        verify(logOperazioniDAO, never()).createLog(any(LogOperazione.class));
    }
}