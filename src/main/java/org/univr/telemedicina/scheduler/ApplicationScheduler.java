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

// classe principale per avviare lo scheduler dei task di monitoraggio
// questa classe contiene i task che vengono eseguiti periodicamente per monitorare l'aderenza ai farmaci
public class ApplicationScheduler {

    public static void main(String[] args) {
        // crea un pool di thread per eseguire i task in background 1 thread è sufficiente qui
        try (ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1)) {
            MonitorService monitorService = new MonitorService(new TerapiaDAO(), new AssunzioneFarmaciDAO(), new NotificheService(new NotificheDAO()), new PazientiDAO());

            System.out.println("avvio dello scheduler dei task di monitoraggio...");

            // --- task 1 schedula checkfarmacidaily() ogni 10 minuti ---
            // il runnable è il pezzo di codice che verrà eseguito
            Runnable checkAdherenceTask = () -> {
                try {
                    System.out.println("esecuzione task checkfarmacidaily...");
                    monitorService.checkFarmaciDaily();
                } catch (DataAccessException e) {
                    // gestisci l'errore qui così un fallimento non blocca lo scheduler!
                    System.err.println("errore critico in checkfarmacidaily " + e.getMessage());
                }
            };


            //MODIFICA - LO RUNNO OGNI 1 ORE
            // lo scheduliamo per partire subito (initialdelay 0) e ripetersi ogni 1 ore
            scheduler.scheduleAtFixedRate(checkAdherenceTask, 0, 1, TimeUnit.HOURS);
            System.out.println("task 'checkfarmacidaily' schedulato ogni 10 minuti");


            // --- task 2 schedula checkfarmaci3daily() ogni giorno alle 18:00 ---
            Runnable check3DayAdherenceTask = () -> {
                try {
                    System.out.println("esecuzione task checkfarmaci3daily...");
                    monitorService.checkFarmaci3Daily();
                } catch (DataAccessException e) {
                    System.err.println("errore critico in checkfarmaci3daily " + e.getMessage());
                }
            };

            // calcoliamo il ritardo fino alle prossime 18:00
            ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
            ZonedDateTime nextRun = now.withHour(18).withMinute(0).withSecond(0);
            if (now.compareTo(nextRun) > 0) {
                // se sono già passate le 18 schedula per domani
                nextRun = nextRun.plusDays(1);
            }

            Duration duration = Duration.between(now, nextRun);
            long initialDelay = duration.getSeconds();

            // lo scheduliamo per partire dopo 'initialdelay' secondi e ripetersi ogni 24 ore
            scheduler.scheduleAtFixedRate(check3DayAdherenceTask, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
        }
        System.out.println("task 'checkfarmaci3daily' schedulato per le 18:00");

    }
}