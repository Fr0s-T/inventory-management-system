package Models;

public record ItemEntry(String itemCode, String name, int quantity, float unitPrice) {

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
