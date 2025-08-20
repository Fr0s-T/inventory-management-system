package Utilities;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 *
 * Author: @Frost
 *
 */

public class DataBaseConnection {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String server = dotenv.get("DB_SERVER");
    private static final String database = dotenv.get("DB_NAME");
    private static final String username = dotenv.get("DB_USER"); // Now used again
    private static final String password = dotenv.get("DB_PASSWORD"); // Now used again

    public static String getConnectionUrl() {
        return "jdbc:sqlserver://" + server + ":1433;"
                + "databaseName=" + database + ";"
                + "encrypt=true;"
                + "trustServerCertificate=true;" // Changed to true like the demo
                + "loginTimeout=30;";
        // integratedSecurity is now removed
    }

    public static Connection getConnection() throws Exception {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        // Now using username and password like the demo
        return DriverManager.getConnection(getConnectionUrl(), username, password);
    }
}
