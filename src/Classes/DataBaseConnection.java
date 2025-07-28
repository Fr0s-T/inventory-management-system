package Classes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseConnection {
    private static final String server = "inventorymanegmentsystem-srv.database.windows.net";
    private static final String database = "IMS";
    private static final String username = "sqladmin";
    private static final String password = "XcrJ8EB~u?J43Em";


    public static String getConnectionUrl(int i) {
        if (i != 300){
            throw new IllegalArgumentException("i doesn't have the correct password");
        }
        return "jdbc:sqlserver://" + server + ":1433;"
                + "database=" + database + ";" + "user=" + username + ";" + "password=" + password + ";"
                + "encrypt=true;" + "trustServerCertificate=false;" + "loginTimeout=30;";
    }
    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        return DriverManager.getConnection(getConnectionUrl(300));
    }

}
