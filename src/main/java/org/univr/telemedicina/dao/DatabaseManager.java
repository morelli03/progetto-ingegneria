package org.univr.telemedicina.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    // URL di connessione al database SQLite.
    // "main.sqlite" deve trovarsi nella cartella radice del progetto.
    private static String URL = "jdbc:sqlite:main.sqlite";

    /**
     * Restituisce una nuova connessione al database.
     * @return un oggetto Connection
     * @throws SQLException se la connessione fallisce
     */
    public static Connection getConnection() throws SQLException {
        // Non è più necessario Class.forName() con i driver JDBC moderni
        return DriverManager.getConnection(URL);
    }
}