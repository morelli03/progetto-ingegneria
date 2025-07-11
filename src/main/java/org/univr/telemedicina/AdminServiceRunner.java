package org.univr.telemedicina;

import org.univr.telemedicina.dao.PazientiDAO;
import org.univr.telemedicina.dao.UtenteDAO;
import org.univr.telemedicina.service.AdminService;

/**
 * Classe provvisoria per creare un utente tramite terminale.
 */
public class AdminServiceRunner {

    public static void main(String[] args) {
        // 1. Inizializza i DAO necessari
        UtenteDAO utenteDAO = new UtenteDAO();
        PazientiDAO pazientiDAO = new PazientiDAO();

        // 2. Crea un'istanza di AdminService
        AdminService adminService = new AdminService(utenteDAO, pazientiDAO);

        System.out.println("--- Avvio Procedura Creazione Utente ---");
        System.out.println("Segui le istruzioni e inserisci i dati nel terminale qui sotto.");

        // 3. Esegui il metodo per creare un utente
        adminService.creaUtente();

        System.out.println("--- Procedura Terminata ---");
    }
}