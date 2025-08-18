package org.univr.telemedicina.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univr.telemedicina.dao.AssunzioneFarmaciDAO;
import org.univr.telemedicina.dao.PazientiDAO;
import org.univr.telemedicina.dao.TerapiaDAO;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.RilevazioneGlicemia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitorServiceTest {

    @Mock
    private TerapiaDAO terapiaDAO;
    @Mock
    private AssunzioneFarmaciDAO assunzioneFarmaciDAO;
    @Mock
    private NotificheService notificheService;
    @Mock
    private PazientiDAO pazientiDAO;

    @InjectMocks
    private MonitorService monitorService;

    // --- TEST PER checkGlicemia ---

    @Test
    void checkGlicemia_ValoreAnormaleAltoDopoPasto_InviaNotifica() throws DataAccessException {
        // ARRANGE
        // Valore alto (>= 180) in un orario post-pasto (es. 9:00 del mattino)
        LocalDateTime orarioPostPranzo = LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 30));
        RilevazioneGlicemia rilevazione = new RilevazioneGlicemia(1, 1, 200, orarioPostPranzo, "Post-colazione");
        when(pazientiDAO.getMedicoRiferimentoByPazienteId(1)).thenReturn(Optional.of(10));
        when(pazientiDAO.findNameById(1)).thenReturn("Mario Rossi");

        // ACT
        monitorService.checkGlicemia(rilevazione);

        // ASSERT
        // Verifica che venga inviata una notifica al medico con priorità 3
        verify(notificheService, times(1)).send(eq(10), eq(3), contains("Glicemia Anormale"), contains("dopo i pasti"), eq("Glicemia"));
    }

    @Test
    void checkGlicemia_ValoreAnormaleBassoPrimaPasto_InviaNotifica() throws DataAccessException {
        // ARRANGE
        // Valore basso (<= 80) in un orario pre-pasto (es. 12:00)
        LocalDateTime orarioPrePranzo = LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 5));
        RilevazioneGlicemia rilevazione = new RilevazioneGlicemia(1, 1, 70, orarioPrePranzo, "Prima pranzo");
        when(pazientiDAO.getMedicoRiferimentoByPazienteId(1)).thenReturn(Optional.of(10));
        when(pazientiDAO.findNameById(1)).thenReturn("Mario Rossi");

        // ACT
        monitorService.checkGlicemia(rilevazione);

        // ASSERT
        verify(notificheService, times(1)).send(eq(10), eq(3), contains("Glicemia Anormale"), contains("prima dei pasti"), eq("Glicemia"));
    }

    @Test
    void checkGlicemia_ValoreNormale_NonInviaNotifica() throws DataAccessException {
        // ARRANGE
        // Valore normale (es. 120) dopo un pasto
        RilevazioneGlicemia rilevazione = new RilevazioneGlicemia(1, 1, 120, LocalDateTime.of(LocalDate.now(), LocalTime.of(13, 0)), "Post-pranzo");

        // ACT
        monitorService.checkGlicemia(rilevazione);

        // ASSERT
        verifyNoInteractions(notificheService); // Nessuna notifica deve essere inviata
    }

    @Test
    void checkGlicemia_MedicoNonTrovato_NonInviaNotificaELanciaEccezione() throws DataAccessException {
        // ARRANGE
        RilevazioneGlicemia rilevazione = new RilevazioneGlicemia(1, 1, 200, LocalDateTime.now(), "Note");
        // Simula che il medico di riferimento non esista
        when(pazientiDAO.getMedicoRiferimentoByPazienteId(1)).thenReturn(Optional.empty());

        // ACT & ASSERT
        // Il metodo orElseThrow() nel service lancerà un'eccezione, che è il comportamento atteso
        assertThrows(Exception.class, () -> monitorService.checkGlicemia(rilevazione));

        // In questo caso, nessuna notifica deve essere inviata
        verifyNoInteractions(notificheService);
    }

    // --- TEST PER checkFarmaciDaily ---

    @Test
    void checkFarmaciDaily_NessunaAssunzioneNelPomeriggio_InviaNotifica() throws DataAccessException {
        // ARRANGE
        int idPaziente = 1;
        // Simula il tempo per essere nel pomeriggio (es. 14:00)
        // Questo test è dipendente dal tempo, in un'app reale si userebbe un Clock iniettato
        if (LocalDateTime.now().getHour() < 12) {
            System.out.println("Skipping test checkFarmaciDaily_NessunaAssunzioneNelPomeriggio_InviaNotifica: orario non adatto.");
            return; // Salta il test se è troppo presto
        }

        when(terapiaDAO.getActivePatientIds()).thenReturn(Collections.singletonList(idPaziente));
        when(terapiaDAO.getFrequenzeGiornalierePerPazienti(anyList())).thenReturn(Map.of(idPaziente, 2)); // 2 assunzioni richieste
        when(assunzioneFarmaciDAO.getConteggioAssunzioniGiornoPerPazienti(anyList(), any(LocalDate.class))).thenReturn(Map.of(idPaziente, 0)); // 0 assunzioni fatte

        // ACT
        monitorService.checkFarmaciDaily();

        // ASSERT
        verify(notificheService, times(1)).send(eq(idPaziente), eq(1), contains("Assunzioni Farmaci Incompleta"), contains("dimenticato"), eq("Assunzioni Farmaci"));
    }

    @Test
    void checkFarmaciDaily_AssunzioniParzialiDiSera_InviaNotifica() throws DataAccessException {
        // ARRANGE
        int idPaziente = 1;
        if (LocalDateTime.now().getHour() < 18) {
            System.out.println("Skipping test checkFarmaciDaily_AssunzioniParzialiDiSera_InviaNotifica: orario non adatto.");
            return; // Salta il test se è troppo presto
        }

        when(terapiaDAO.getActivePatientIds()).thenReturn(Collections.singletonList(idPaziente));
        when(terapiaDAO.getFrequenzeGiornalierePerPazienti(anyList())).thenReturn(Map.of(idPaziente, 3)); // 3 assunzioni richieste
        when(assunzioneFarmaciDAO.getConteggioAssunzioniGiornoPerPazienti(anyList(), any(LocalDate.class))).thenReturn(Map.of(idPaziente, 1)); // 1 sola assunzione fatta

        // ACT
        monitorService.checkFarmaciDaily();

        // ASSERT
        verify(notificheService, times(1)).send(eq(idPaziente), eq(1), contains("Assunzioni Farmaci Incompleta"), contains("completare la terapia"), eq("Assunzioni Farmaci"));
    }

    @Test
    void checkFarmaciDaily_TerapiaCompletata_NonInviaNotifica() throws DataAccessException {
        // ARRANGE
        int idPaziente = 1;
        when(terapiaDAO.getActivePatientIds()).thenReturn(Collections.singletonList(idPaziente));
        when(terapiaDAO.getFrequenzeGiornalierePerPazienti(anyList())).thenReturn(Map.of(idPaziente, 2)); // 2 assunzioni richieste
        when(assunzioneFarmaciDAO.getConteggioAssunzioniGiornoPerPazienti(anyList(), any(LocalDate.class))).thenReturn(Map.of(idPaziente, 2)); // 2 assunzioni fatte

        // ACT
        monitorService.checkFarmaciDaily();

        // ASSERT
        verifyNoInteractions(notificheService);
    }

    @Test
    void checkFarmaciDaily_NessunPazienteAttivo_NonFaNulla() throws DataAccessException {
        // ARRANGE
        when(terapiaDAO.getActivePatientIds()).thenReturn(Collections.emptyList());

        // ACT
        monitorService.checkFarmaciDaily();

        // ASSERT
        verifyNoInteractions(assunzioneFarmaciDAO);
        verifyNoInteractions(notificheService);
    }

    // --- TEST PER checkFarmaci3Daily ---

    @Test
    void checkFarmaci3Daily_MancataAderenza_InviaNotificaAlMedico() throws DataAccessException {
        // ARRANGE
        int idPaziente = 1;
        int idMedico = 10;

        // Setup dei DAO
        when(terapiaDAO.getActivePatientIds()).thenReturn(Collections.singletonList(idPaziente));
        when(terapiaDAO.getFrequenzeGiornalierePerPazienti(anyList())).thenReturn(Map.of(idPaziente, 2)); // 2 al giorno richieste (tot 6 in 3gg)
        when(pazientiDAO.getMedicoRiferimentoByPazienteId(idPaziente)).thenReturn(Optional.of(idMedico));
        when(pazientiDAO.findNameById(idPaziente)).thenReturn("Mario Rossi");

        // Simula che il paziente abbia saltato le assunzioni
        // Il DAO viene chiamato 3 volte nel ciclo, simuliamo 0 assunzioni ogni volta
        when(assunzioneFarmaciDAO.getConteggioAssunzioniGiornoPerPazienti(anyList(), any(LocalDate.class)))
                .thenReturn(Collections.emptyMap()); // 0 assunzioni ogni giorno

        // ACT
        monitorService.checkFarmaci3Daily();

        // ASSERT
        // Verifica che sia stata inviata una notifica al MEDICO (ID 10)
        verify(notificheService, times(1)).send(eq(idMedico), eq(2), contains("Mancata aderenza alla terapia"), anyString(), eq("Assunzioni Farmaci"));
        // Verifica che il DAO delle assunzioni sia stato chiamato 3 volte (una per ogni giorno nel ciclo)
        verify(assunzioneFarmaciDAO, times(3)).getConteggioAssunzioniGiornoPerPazienti(anyList(), any(LocalDate.class));
    }
}