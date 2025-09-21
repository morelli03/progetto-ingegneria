package org.univr.telemedicina.scheduler;

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
        // NON usare try-with-resources qui, altrimenti lo scheduler si chiude subito!
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // L'istanziazione dei servizi va bene qui
        MonitorService monitorService = new MonitorService(new TerapiaDAO(), new AssunzioneFarmaciDAO(), new NotificheService(new NotificheDAO()), new PazientiDAO());

        System.out.println("Avvio dello scheduler dei task di monitoraggio...");

        // --- Task 1: Schedula checkFarmaciDaily() ogni ora ---
        Runnable checkAdherenceTask = () -> {
            try {
                System.out.println("Esecuzione task checkFarmaciDaily...");
                monitorService.checkFarmaciDaily();
            } catch (DataAccessException e) {
                System.err.println("Errore critico in checkFarmaciDaily: " + e.getMessage());
            }
        };

        scheduler.scheduleAtFixedRate(checkAdherenceTask, 0, 1, TimeUnit.HOURS);
        System.out.println("Task 'checkFarmaciDaily' schedulato ogni ora.");


        // --- Task 2: Schedula checkFarmaci3Daily() ogni giorno alle 18:00 ---
        Runnable check3DayAdherenceTask = () -> {
            try {
                System.out.println("Esecuzione task checkFarmaci3Daily...");
                monitorService.checkFarmaci3Daily();
            } catch (DataAccessException e) {
                System.err.println("Errore critico in checkFarmaci3Daily: " + e.getMessage());
            }
        };

        // Calcoliamo il ritardo fino alle prossime 18:00
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime nextRun = now.withHour(18).withMinute(0).withSecond(0);
        if (now.isAfter(nextRun)) {
            // Se sono già passate le 18, schedula per domani
            nextRun = nextRun.plusDays(1);
        }

        Duration duration = Duration.between(now, nextRun);
        long initialDelayInSeconds = duration.getSeconds();

        // Scheduliamo il task per partire dopo 'initialDelayInSeconds' e ripetersi ogni 24 ore
        scheduler.scheduleAtFixedRate(check3DayAdherenceTask, initialDelayInSeconds, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);

        System.out.println("Task 'checkFarmaci3Daily' schedulato per le 18:00. Prossima esecuzione tra " + initialDelayInSeconds + " secondi.");
        System.out.println("Lo scheduler è attivo e rimarrà in esecuzione.");
    }
}