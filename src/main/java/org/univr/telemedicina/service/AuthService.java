package org.univr.telemedicina.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.univr.telemedicina.dao.UtenteDAO;
import org.univr.telemedicina.exception.AuthServiceException;
import org.univr.telemedicina.exception.DataAccessException;
import org.univr.telemedicina.model.Utente;

import java.util.Optional;


public class AuthService {

    private final UtenteDAO utenteDao;
    private final PasswordEncoder passwordEncoder;


    public AuthService(UtenteDAO utenteDao, PasswordEncoder passwordEncoder) {
        this.utenteDao = utenteDao;
        this.passwordEncoder = passwordEncoder;
    }

    // verifica se una password in chiaro corrisponde a un hash salvato
    // @param emailutente email che ha inserito
    // @param passwordinchiaro la password fornita dall'utente al momento del login
    // @return utente se le password corrispondono altrimenti optional.empty()
    public Optional<Utente> verificaPassword(String emailUtente, String passwordInChiaro) throws AuthServiceException {
        // cerca nel database per quell'email restituisce empty se non esiste la mail
        Optional<Utente> utenteTrovato;
        try {
            utenteTrovato = utenteDao.findByEmail(emailUtente);
        } catch (DataAccessException e) {
            System.err.println("errore durante la ricerca dell'utente per email " + e.getMessage());
            // qui devo lanciare un'eccezione personalizzata
            throw new AuthServiceException("impossibile completare l'autenticazione a causa di un errore del server", e);
        }

        if (utenteTrovato.isPresent()) {
            Utente utente = utenteTrovato.get(); // prende l'utente dentro optional e lo fa diventare utente

            // se la password è uguale alla password hashata ritorna utente
            if(passwordEncoder.matches(passwordInChiaro, utente.getHashedPassword())){
                return Optional.of(utente);
            }
        }

        //se l'email non esiste nel database o la password è sbagliata ritorna empty
        return Optional.empty();

    }

}
