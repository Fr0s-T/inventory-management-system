package Classes;

public class DataBaseConnection {
    private final String server = "inventorymanegmentsystem-srv.database.windows.net";
    private final String database = "inventory_manegment_system_db";
    private final String username = "sqladmin";
    private final String password = "XcrJ8EB~u?J43Em";


    public String getConnectionUrl(int i) {
        if (i != 300){
            throw new IllegalArgumentException("i doesn't have the correct password");
        }
        return "jdbc:sqlserver://" + server + ":1433;"
                + "database=" + database + ";" + "user=" + username + ";" + "password=" + password + ";"
                + "encrypt=true;" + "trustServerCertificate=false;" + "loginTimeout=30;";
    }

}
