package Tests;

import Models.Product;
import Services.ProductsService;

import java.util.ArrayList;

public class testProductService {
    public static void main(String[] args){
        ArrayList<Product> products = ProductsService.getProductsByWarehouseId(50);
        for (Product product:products){
            System.out.println(product.toString()+"\n");
        }
    }
}
