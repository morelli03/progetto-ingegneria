package org.univr.telemedicina.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univr.telemedicina.dao.*;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.exception.WrongAssumptionException;
import org.univr.telemedicina.model.CondizioniPaziente;
import org.univr.telemedicina.model.RilevazioneGlicemia;
import org.univr.telemedicina.model.Terapia;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PazienteServiceTest {

    @Mock
    private RilevazioneGlicemiaDAO rilevazioneDAO;
    @Mock
    private MonitorService monitorService;
    @Mock
    private AssunzioneFarmaciDAO assunzioneFarmaciDAO;
    @Mock
    private CondizioniPazienteDAO condizioniDAO;

    @Mock
    private UtenteDAO utenteDAO;

    @Mock
    private PazientiDAO pazientiDAO;

    @Mock
    private NotificheService notificheService;

    @InjectMocks
    private PazienteService pazienteService;

    @Test
    void registraRilevazioneGlicemia_Successo() throws DataAccessException {
        // ARRANGE
        int idPaziente = 1;
        int valore = 150;
        LocalDateTime timestamp = LocalDateTime.now();

        // ACT
        pazienteService.registraRilevazioneGlicemia(idPaziente, valore, timestamp, "Note");

        // ASSERT
        verify(rilevazioneDAO, times(1)).create(any(RilevazioneGlicemia.class));
        verify(monitorService, times(1)).checkGlicemia(any(RilevazioneGlicemia.class));
    }

    @Test
    void registraAssunzioneFarmaci_QuantitaCorretta_Successo() throws WrongAssumptionException, DataAccessException {
        // ARRANGE
        Terapia terapia = new Terapia(1, 1, 1, "Farmaco", "10mg", 1, "Indicazioni", LocalDate.now(), LocalDate.now().plusDays(1));
        String quantitaAssunta = "10mg";

        // ACT
        pazienteService.registraAssunzioneFarmaci(terapia, quantitaAssunta, LocalDateTime.now());

        // ASSERT
        // Verifica che il metodo aggiungiAssunzione sia stato chiamato una volta con un oggetto AssunzioneFarmaci
        verify(assunzioneFarmaciDAO, times(1)).aggiungiAssunzione(any());
    }

    @Test
    void registraAssunzioneFarmaci_QuantitaErrata() throws DataAccessException {
        // ARRANGE
        Terapia terapia = new Terapia(1, 1, 1, "Farmaco", "10mg", 1, "Indicazioni", LocalDate.now(), LocalDate.now().plusDays(1));
        String quantitaAssunta = "20mg";
        LocalDateTime timestamp = LocalDateTime.now();
        when(pazientiDAO.findNameById(terapia.getIDPaziente())).thenReturn("NomePaziente");

        // ACT & ASSERT
        assertThrows(WrongAssumptionException.class, () -> {
            pazienteService.registraAssunzioneFarmaci(terapia, quantitaAssunta, timestamp);
        });

        verify(assunzioneFarmaciDAO, times(1)).aggiungiAssunzione(any());
        verify(notificheService, times(1)).send(
                eq(terapia.getIDMedico()),
                eq(2),
                eq("assunzione di farmaci non corretta"),
                anyString(),
                eq("assunzione farmaci")
        );
    }

    @Test
    void segnalaCondizionePaziente_Successo() throws DataAccessException {
        // ARRANGE
        int idPaziente = 1;
        String tipo = "Sintomo";
        String descrizione = "Febbre alta";
        String periodo = "Ultimi 2 giorni";

        // ACT
        pazienteService.segnalaCondizionePaziente(idPaziente, tipo, descrizione, periodo);

        // ASSERT
        // Verifica che il DAO sia stato chiamato per creare la condizione
        // Usiamo un ArgumentCaptor per ispezionare l'oggetto passato al DAO
        ArgumentCaptor<CondizioniPaziente> captor = ArgumentCaptor.forClass(CondizioniPaziente.class);
        verify(condizioniDAO, times(1)).create(captor.capture());

        // Controlla che i dati nell'oggetto catturato siano corretti
        CondizioniPaziente condizioneCatturata = captor.getValue();
        assertEquals(idPaziente, condizioneCatturata.getIDPaziente());
        assertEquals(tipo, condizioneCatturata.getTipo());
        assertEquals(descrizione, condizioneCatturata.getDescrizione());
    }

    @Test
    void segnalaCondizionePaziente_DaoLanciaEccezione_RilanciaEccezione() throws DataAccessException {
        // ARRANGE
        int idPaziente = 1;
        // Configura il mock per lanciare un'eccezione quando viene chiamato
        doThrow(new DataAccessException("Errore DB")).when(condizioniDAO).create(any(CondizioniPaziente.class));

        // ACT & ASSERT
        // Verifica che il servizio rilanci correttamente l'eccezione ricevuta dal DAO
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            pazienteService.segnalaCondizionePaziente(idPaziente, "Sintomo", "Descrizione", "Periodo");
        });

        assertTrue(exception.getMessage().contains("errore durante la registrazione della condizione del paziente"));
    }

    // --- TEST PER inviaEmailMedicoRiferimento ---

    @Test
    void inviaEmailMedicoRiferimento_MedicoNonTrovato_LanciaEccezione() throws DataAccessException, SQLException {
        // ARRANGE
        int idPaziente = 1;
        int idMedico = 10;

        // Simula che l'email del medico non venga trovata
        when(pazientiDAO.findMedByIDPaziente(idPaziente)).thenReturn(idMedico);
        when(utenteDAO.findEmailById(idMedico)).thenReturn(Optional.empty());

        // ACT & ASSERT
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            pazienteService.inviaEmailMedicoRiferimento(idPaziente, "Subject", "Body");
        });

        assertTrue(exception.getMessage().contains("nessun medico di riferimento trovato"));
    }
}