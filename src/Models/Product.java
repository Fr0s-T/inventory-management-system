package Models;

public class Product {
    private String itemCode;
    private String color;
    private int quantity;
    private String size;
    private String section;
    private String picture;
    private int shipmentDetailsID;


    public Product() {
    }

    public Product(String itemCode, String color, int quantity, String size, String section, String picture, int shipmentDetailsID) {
        this.itemCode = itemCode;
        this.color = color;
        this.quantity = quantity;
        this.size = size;
        this.section = section;
        this.picture = picture;
        this.shipmentDetailsID = shipmentDetailsID;
    }

    // ✅ Getters and Setters
    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public int getShipmentDetailsID() {
        return shipmentDetailsID;
    }

    public void setShipmentDetailsID(int shipmentDetailsID) {
        this.shipmentDetailsID = shipmentDetailsID;
    }
    @Override
    public String toString() {
        return "Product {" +
                "ItemCode='" + itemCode + '\'' +
                ", Color='" + color + '\'' +
                ", Quantity=" + quantity +
                ", Size='" + size + '\'' +
                ", Section='" + section + '\'' +
                ", Picture='" + (picture != null ? picture : "No image") + '\'' +
                ", ShipmentDetailsID=" + shipmentDetailsID +
                '}';
    }
}
