package org.univr.telemedicina.scheduler;

import org.univr.telemedicina.dao.AssunzioneFarmaciDAO;
import org.univr.telemedicina.dao.NotificheDAO;
import org.univr.telemedicina.dao.PazientiDAO;
import org.univr.telemedicina.dao.TerapiaDAO;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.service.MonitorService;
import org.univr.telemedicina.service.NotificheService;

// Classe di utilità per eseguire i metodi di monitoraggio durante l'esame
public class DummyRunner {
    public static void main(String[] args) {
        MonitorService monitorService = new MonitorService(new TerapiaDAO(), new AssunzioneFarmaciDAO(), new NotificheService(new NotificheDAO()), new PazientiDAO());

        // Esegue entrambi i controlli una volta
        try {
            System.out.println("Esecuzione task checkFarmaciDaily...");
            monitorService.checkFarmaciDaily();
        } catch (DataAccessException e) {
            System.err.println("Errore critico in checkFarmaciDaily: " + e.getMessage());
        }
        try {
            System.out.println("Esecuzione task checkFarmaci3Daily...");
            monitorService.checkFarmaci3Daily();
        } catch (DataAccessException e) {
            System.err.println("Errore critico in checkFarmaci3Daily: " + e.getMessage());
        }
    }
}
