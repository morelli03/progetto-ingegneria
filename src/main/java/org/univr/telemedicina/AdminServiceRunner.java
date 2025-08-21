package org.univr.telemedicina;

import org.univr.telemedicina.dao.PazientiDAO;
import org.univr.telemedicina.dao.UtenteDAO;
import org.univr.telemedicina.service.AdminService;

// classe provvisoria per creare un utente tramite terminale
public class AdminServiceRunner {

    public static void main(String[] args) {
        // 1 inizializza i dao necessari
        UtenteDAO utenteDAO = new UtenteDAO();
        PazientiDAO pazientiDAO = new PazientiDAO();

        // 2 crea un'istanza di adminservice
        AdminService adminService = new AdminService(utenteDAO, pazientiDAO);

        System.out.println("--- avvio procedura creazione utente ---");
        System.out.println("segui le istruzioni e inserisci i dati nel terminale qui sotto");

        // 3 esegui il metodo per creare un utente
        adminService.creaUtente();

        System.out.println("--- procedura terminata ---");
    }
}