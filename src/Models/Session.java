package Models;

import Services.ProductsService;
import ViewsControllers.ProductsController;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

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
    private static ArrayList<Warehouse> allWarehouses;
    private static Map<String, Product> globalProductCatalog;
    private static Timestamp lastUpdate;


    public static Map<String, Product> getGlobalProductCatalog() {
        return globalProductCatalog;
    }

    public static void setGlobalProductCatalog(Map<String, Product> globalProductCatalog) {
        Session.globalProductCatalog = globalProductCatalog;
    }


    public static Warehouse getCurrentWarehouse() {
        return currentWarehouse;
    }

    public static void setCurrentWarehouse(Warehouse warehouse) {
        currentWarehouse = warehouse;
    }

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

    public static void logOut() {
        ProductsService.stopBackgroundSync();
        currentUser = null;
        warehouses = null;
        products = null;
        currentWarehouse = null;
        allWarehouses = null;
        lastUpdate = null;
        // Keep globalProductCatalog intact
    }

    public static void BackToDashboard() {
        currentWarehouse = null;
        products = null;
        lastUpdate = null;
    }

    public static ArrayList<Warehouse> getAllWarehouses() {
        return allWarehouses;
    }

    public static void setAllWarehouses(ArrayList<Warehouse> allWarehouses) {
        Session.allWarehouses = allWarehouses;
    }

    public static Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public static void setLastUpdate(Timestamp lastUpdate) {
        Session.lastUpdate = lastUpdate;
    }
}
