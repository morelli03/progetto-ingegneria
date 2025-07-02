package org.univr.telemedicina.scheduler; // o dove preferisci metterla

import org.univr.telemedicina.dao.AssunzioneFarmaciDAO;
import org.univr.telemedicina.dao.NotificheDAO;
import org.univr.telemedicina.dao.PazientiDAO;
import org.univr.telemedicina.dao.TerapiaDAO;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.service.MonitorService;
import org.univr.telemedicina.service.NotificheService;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ApplicationScheduler {

    public static void main(String[] args) {
        // Crea un pool di thread per eseguire i task in background. 1 thread è sufficiente qui.
        try (ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1)) {
            MonitorService monitorService = new MonitorService(new TerapiaDAO(), new AssunzioneFarmaciDAO(), new NotificheService(new NotificheDAO()), new PazientiDAO());

            System.out.println("Avvio dello scheduler dei task di monitoraggio...");

            // --- Task 1: Schedula checkFarmaciDaily() ogni 10 minuti ---
            // Il Runnable è il pezzo di codice che verrà eseguito.
            Runnable checkAdherenceTask = () -> {
                try {
                    System.out.println("Esecuzione task: checkFarmaciDaily...");
                    monitorService.checkFarmaciDaily();
                } catch (DataAccessException e) {
                    // Gestisci l'errore qui, così un fallimento non blocca lo scheduler!
                    System.err.println("ERRORE CRITICO in checkFarmaciDaily: " + e.getMessage());
                }
            };

            // Lo scheduliamo per partire subito (initialDelay 0) e ripetersi ogni 10 minuti.
            scheduler.scheduleAtFixedRate(checkAdherenceTask, 0, 10, TimeUnit.MINUTES);
            System.out.println("Task 'checkFarmaciDaily' schedulato ogni 10 minuti.");


            // --- Task 2: Schedula checkFarmaci3Daily() ogni giorno alle 18:00 ---
            Runnable check3DayAdherenceTask = () -> {
                try {
                    System.out.println("Esecuzione task: checkFarmaci3Daily...");
                    monitorService.checkFarmaci3Daily();
                } catch (DataAccessException e) {
                    System.err.println("ERRORE CRITICO in checkFarmaci3Daily: " + e.getMessage());
                }
            };

            // Calcoliamo il ritardo fino alle prossime 18:00
            ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
            ZonedDateTime nextRun = now.withHour(18).withMinute(0).withSecond(0);
            if (now.compareTo(nextRun) > 0) {
                // Se sono già passate le 18, schedula per domani
                nextRun = nextRun.plusDays(1);
            }

            Duration duration = Duration.between(now, nextRun);
            long initialDelay = duration.getSeconds();

            // Lo scheduliamo per partire dopo 'initialDelay' secondi e ripetersi ogni 24 ore.
            scheduler.scheduleAtFixedRate(check3DayAdherenceTask, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
        }
        System.out.println("Task 'checkFarmaci3Daily' schedulato per le 18:00.");

    }
}