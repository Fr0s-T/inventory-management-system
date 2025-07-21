public class Main {

   private final String server = "inventorymanegmentsystem-srv.database.windows.net";
   private final String database = "inventory_manegment_system_db";
   private final String username = "sqladmin";
   private final String password = "XcrJ8EB~u?J43Em";

    private final String connectionUrl = "jdbc:sqlserver://" + server + ":1433;"
            + "database=" + database + ";"
            + "user=" + username + ";"
            + "password=" + password + ";"
            + "encrypt=true;"
            + "trustServerCertificate=false;"
            + "loginTimeout=30;";

    public String getConnectionUrl(int i) {
        if (i != 300){
            throw new IllegalArgumentException("i doesn't have the correct password");
        }
        return connectionUrl;
    }
    //Ready innit
    //Update main logic

    // login mf
    //main mf display products
    //product mf
    //settings limited use
    //Analytics su only

}

