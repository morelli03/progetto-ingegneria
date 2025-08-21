package org.univr.telemedicina.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    // url di connessione al database sqlite
    // "main.sqlite" deve trovarsi nella cartella radice del progetto jdbc:sqlite:main.sqlite_journal_mode=wal
    private static String URL = "jdbc:sqlite:main.sqlite";

    // restituisce una nuova connessione al database
    // @return un oggetto connection
    // @throws sqlexception se la connessione fallisce
    public static Connection getConnection() throws SQLException {
        // non è più necessario class.forname() con i driver jdbc moderni
        return DriverManager.getConnection(URL);
    }
}