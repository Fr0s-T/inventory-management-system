package Utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 *
 * Author: @Frost
 *
 *
 */



public class DataBaseConnection {
    private static final String server = System.getenv("DB_SERVER");
    private static final String database = System.getenv("DB_NAME");
    private static final String username = System.getenv("DB_USER");
    private static final String password = System.getenv("DB_PASSWORD");

    public static String getConnectionUrl() {
        return "jdbc:sqlserver://" + server + ":1433;"
                + "database=" + database + ";"
                + "user=" + username + ";"
                + "password=" + password + ";"
                + "encrypt=true;"
                + "trustServerCertificate=false;"
                + "loginTimeout=30;";
    }

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        return DriverManager.getConnection(getConnectionUrl());
    }
}
