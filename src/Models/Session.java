package Models;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 *
 * Author: @Frost
 *
 */

public class Session {
    private static User currentUser;
    private static ArrayList<Warehouse> warehouses;
    private static ArrayList<Product> products;
    private static Warehouse currentWarehouse;

    public static Warehouse getCurrentWarehouse(){
        return currentWarehouse;
    }

    public static void setCurrentWarehouse(Warehouse warehouse){ currentWarehouse = warehouse; }

    public static ArrayList<Product> getProducts() {
        return products;
    }

    public static void setProducts(ArrayList<Product> products) {
        Session.products = products;
    }

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

    public static void logOut(){
        currentUser = null;
        warehouses = null;
        products = null;
    }
}
