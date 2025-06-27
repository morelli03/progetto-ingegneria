package org.univr.telemedicina.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.univr.telemedicina.dao.UtenteDAO;
import org.univr.telemedicina.model.Utente;

import java.util.Optional;


public class AuthService {


    // dependency injection
    private final UtenteDAO utenteDao;
    private final PasswordEncoder passwordEncoder;

    // Le dipendenze vengono passate (iniettate) tramite il costruttore
    public AuthService(UtenteDAO utenteDao, PasswordEncoder passwordEncoder) {
        this.utenteDao = utenteDao;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Verifica se una password in chiaro corrisponde a un hash salvato.
     * @param emailUtente email che ha inserito
     * @param passwordInChiaro La password fornita dall'utente al momento del login.
     * @return true se le password corrispondono, altrimenti false.
     */
    public Optional<Utente> verificaPassword(String emailUtente, String passwordInChiaro) {
        // cerca nel database per quell'email, restituisce empty se non eiste la mail
        Optional<Utente> utenteTrovato = utenteDao.findByEmail(emailUtente);

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
