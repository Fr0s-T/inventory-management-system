package Models;

import java.util.ArrayList;

/**
 *
 * Author: @Frost
 *
 */

public class Session {
    private static User currentUser;
    private static ArrayList<Warehouse> warehouses;

    public static ArrayList<Warehouse> getWarehouses() {
        return warehouses;
    }

    public static void setWarehouses(ArrayList<Warehouse> warehouses) {
        Session.warehouses = warehouses;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }
}
