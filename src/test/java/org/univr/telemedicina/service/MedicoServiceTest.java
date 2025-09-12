package org.univr.telemedicina.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univr.telemedicina.dao.*;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.exception.MedicoServiceException;
import org.univr.telemedicina.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicoServiceTest {

    @Mock
    private PazientiDAO pazientiDAO;
    @Mock
    private RilevazioneGlicemiaDAO rilevazioneGlicemiaDAO;
    @Mock
    private CondizioniPazienteDAO condizioniPazienteDAO;
    @Mock
    private LogOperazioniDAO logOperazioniDAO;
    @Mock
    private TerapiaDAO terapiaDAO;
    @Mock
    private AssunzioneFarmaciDAO assunzioneFarmaciDAO;

    @InjectMocks
    private MedicoService medicoService;

    @Test
    void getPazientiAssegnati_Successo() throws DataAccessException, MedicoServiceException {
        // ARRANGE
        int idMedico = 1;
        Utente paziente = new Utente(2, "paziente@test.com", "pass", "Mario", "Rossi", "Paziente", LocalDate.now());
        when(pazientiDAO.findPazientiByMedId(idMedico)).thenReturn(Collections.singletonList(paziente));

        // ACT
        List<Utente> risultato = medicoService.getPazientiAssegnati(idMedico);
        // Verifica che il metodo del DAO sia stato chiamato con l'ID corretto
        // ASSERT
        assertNotNull(risultato);
        assertEquals(1, risultato.size());
        assertEquals("Mario", risultato.get(0).getNome());
        verify(pazientiDAO, times(1)).findPazientiByMedId(idMedico);
    }

    @Test
    void addCondizioniPaziente_Successo() throws MedicoServiceException, DataAccessException {
        // ARRANGE
        int idMedico = 1;
        int idPaziente = 2;
        String tipo = "patologia";
        String descrizione = "Allergia alle arachidi";

        // ACT
        medicoService.addCondizioniPaziente(idMedico, idPaziente, tipo, descrizione, "pregressa", LocalDate.now());

        // ASSERT
        // Verifica che il DAO sia stato chiamato per creare la condizione e per fare il log
        verify(condizioniPazienteDAO, times(1)).create(any(CondizioniPaziente.class));
        verify(logOperazioniDAO, times(1)).createLog(any(LogOperazione.class));
    }

    @Test
    void addCondizioniPaziente_TipoNonValido_LanciaEccezione() {
        // ARRANGE
        int idMedico = 1;
        int idPaziente = 2;
        String tipoInvalido = "tipo_sbagliato";

        // ACT & ASSERT
        MedicoServiceException exception = assertThrows(MedicoServiceException.class, () -> {
            medicoService.addCondizioniPaziente(idMedico, idPaziente, tipoInvalido, "desc", "periodo", LocalDate.now());
        });

        assertTrue(exception.getMessage().contains("tipo di condizione non valido"));
        verifyNoInteractions(condizioniPazienteDAO); // Nessuna interazione con i DAO se la validazione fallisce
        verifyNoInteractions(logOperazioniDAO);
    }

    // NUOVO TEST: Verifica l'aggiornamento di una condizione esistente
    @Test
    void updateCondizioniPaziente_Successo() throws MedicoServiceException, DataAccessException {
        // ARRANGE
        int idMedicoOperante = 1;
        CondizioniPaziente condizioneDaAggiornare = new CondizioniPaziente();
        condizioneDaAggiornare.setIDCondizione(100); // ID esistente
        condizioneDaAggiornare.setIDPaziente(2);
        condizioneDaAggiornare.setTipo("fattoriRischio");
        condizioneDaAggiornare.setDescrizione("Descrizione aggiornata");

        // ACT
        medicoService.updateCondizioniPaziente(idMedicoOperante, condizioneDaAggiornare);

        // ASSERT
        // Verifica che il DAO sia stato chiamato per l'aggiornamento
        verify(condizioniPazienteDAO, times(1)).update(condizioneDaAggiornare);
        // Verifica che l'operazione sia stata loggata
        verify(logOperazioniDAO, times(1)).createLog(any(LogOperazione.class));
    }

    // NUOVO TEST: Verifica che il servizio gestisca correttamente dati non validi per l'aggiornamento
    @Test
    void updateCondizioniPaziente_IdNonValido_LanciaEccezione() {
        // ARRANGE
        int idMedicoOperante = 1;
        CondizioniPaziente condizioneSenzaId = new CondizioniPaziente();
        condizioneSenzaId.setIDCondizione(0); // ID non valido
        condizioneSenzaId.setTipo("patologia");

        // ACT & ASSERT
        MedicoServiceException exception = assertThrows(MedicoServiceException.class, () -> {
            medicoService.updateCondizioniPaziente(idMedicoOperante, condizioneSenzaId);
        });

        assertTrue(exception.getMessage().contains("id della condizione non valido"));
        verifyNoInteractions(condizioniPazienteDAO, logOperazioniDAO);
    }

    // NUOVO TEST: Verifica che la dashboard del paziente venga popolata correttamente
    @Test
    void getDatiPazienteDashboard_Successo() throws MedicoServiceException, DataAccessException {
        // ARRANGE
        Utente paziente = new Utente(2, "paziente@test.com", "pass", "Mario", "Rossi", "Paziente", LocalDate.now());
        int idPaziente = paziente.getIDUtente();

        // Prepara le liste di dati finti che i DAO dovrebbero restituire
        List<RilevazioneGlicemia> rilevazioni = Collections.singletonList(new RilevazioneGlicemia(idPaziente, 120, LocalDateTime.now(), ""));
        List<Terapia> terapie = Collections.singletonList(new Terapia(idPaziente, 1, "Farmaco", "10mg", 1, "", LocalDate.now(), null));
        List<CondizioniPaziente> condizioni = Collections.singletonList(new CondizioniPaziente(idPaziente, "Sintomo", "Tosse", "", LocalDate.now()));
        List<AssunzioneFarmaci> assunzioni = Collections.singletonList(new AssunzioneFarmaci(1, idPaziente, LocalDateTime.now(), "10mg"));

        // Configura il comportamento dei mock DAO
        when(rilevazioneGlicemiaDAO.getRilevazioniByPaziente(idPaziente)).thenReturn(rilevazioni);
        when(terapiaDAO.listTherapiesByPatId(idPaziente)).thenReturn(terapie);
        when(condizioniPazienteDAO.listByIDPatId(idPaziente)).thenReturn(condizioni);
        when(assunzioneFarmaciDAO.leggiAssunzioniFarmaci(idPaziente)).thenReturn(assunzioni);

        // ACT
        PazienteDashboard dashboard = medicoService.getDatiPazienteDasboard(paziente);

        // ASSERT
        // Verifica che l'oggetto dashboard non sia nullo
        assertNotNull(dashboard);

        // Verifica che tutti i dati dell'utente e le liste siano stati popolati correttamente
        assertEquals(paziente, dashboard.getDatiUtente());
        assertEquals(1, dashboard.getElencoRilevazioni().size());
        assertEquals(120, dashboard.getElencoRilevazioni().get(0).getValore());
        assertEquals(1, dashboard.getElencoTerapie().size());
        assertEquals("Farmaco", dashboard.getElencoTerapie().get(0).getNomeFarmaco());
        assertEquals(1, dashboard.getElencoCondizioni().size());
        assertEquals("Tosse", dashboard.getElencoCondizioni().get(0).getDescrizione());
        assertEquals(1, dashboard.getElencoAssunzioni().size());
        assertEquals("10mg", dashboard.getElencoAssunzioni().get(0).getQuantitaAssunta());

        // Verifica che tutti i DAO siano stati chiamati una volta
        verify(rilevazioneGlicemiaDAO, times(1)).getRilevazioniByPaziente(idPaziente);
        verify(terapiaDAO, times(1)).listTherapiesByPatId(idPaziente);
        verify(condizioniPazienteDAO, times(1)).listByIDPatId(idPaziente);
        verify(assunzioneFarmaciDAO, times(1)).leggiAssunzioniFarmaci(idPaziente);
    }
}