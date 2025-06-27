package org.univr.telemedicina.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.univr.telemedicina.dao.UtenteDAO;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.Utente;

import java.util.Optional;


public class AuthService {

    private final UtenteDAO utenteDao;
    private final PasswordEncoder passwordEncoder;

    public AuthService() {
        this.utenteDao = new UtenteDAO();
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Verifica se una password in chiaro corrisponde a un hash salvato.
     * @param emailUtente email che ha inserito
     * @param passwordInChiaro La password fornita dall'utente al momento del login.
     * @return Utente se le password corrispondono, altrimenti Optional.empty().
     */
    public Optional<Utente> verificaPassword(String emailUtente, String passwordInChiaro) {
        // cerca nel database per quell'email, restituisce empty se non esiste la mail
        Optional<Utente> utenteTrovato;
        try {
            utenteTrovato = utenteDao.findByEmail(emailUtente);
        } catch (DataAccessException e) {
            System.err.println("Errore durante la ricerca dell'utente per email: " + e.getMessage());
            // qui devo lanciare un'eccezione personalizzata
            return Optional.empty(); // ritorna empty se c'è un errore nel database
        }

        if (utenteTrovato.isPresent()) {
            Utente utente = utenteTrovato.get(); // prende l'utente dentro optional e lo fa diventare Utente

            // se la password è uguale alla password hashata ritorna utente
            if(passwordEncoder.matches(passwordInChiaro, utente.getHashedPassword())){
                return Optional.of(utente);
            }
        }

        //se l'email non esiste nel database o la password è sbagliata ritorna empty
        return Optional.empty();

    }

}
