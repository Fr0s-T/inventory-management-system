package Classes;

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

}
//package Classes;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//
//public class DataBaseConnection {
//    private static final String SERVER = "inventorymanegmentsystem-srv.database.windows.net";
//    private static final String DATABASE = "IMS";
//    private static final String USERNAME = "sqladmin";
//    private static final String PASSWORD = "XcrJ8EB~u?J43Em";
//
//    private static final String CONNECTION_URL = "jdbc:sqlserver://" + SERVER + ":1433;"
//            + "database=" + DATABASE + ";"
//            + "user=" + USERNAME + ";"
//            + "password=" + PASSWORD + ";"
//            + "encrypt=true;"
//            + "trustServerCertificate=false;"
//            + "loginTimeout=30;";
//
//    // Static method to get a connection
//    public static Connection getConnection() throws SQLException {
//        return DriverManager.getConnection(CONNECTION_URL);
//    }
//}

