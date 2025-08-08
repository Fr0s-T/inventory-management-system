package Models;

public class ItemEntry {

    private final String itemCode;
    private final String name;
    private final int quantity;
    private final float unitPrice;

    public ItemEntry(String itemCode, String name, int quantity, float unitPrice) {
        this.itemCode = itemCode;
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getItemCode() {
        return itemCode;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public float getUnitPrice() {
        return unitPrice;
    }

    @Override
    public String toString() {
        return "ItemEntry{" +
                "itemCode='" + itemCode + '\'' +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                '}';
    }
}
