package Models;

public class ShipmentDetails {

    private String itemCode;
    private int quantity;
    private float totalPrice;
    private int shipmentId;
    private String shipmentType;
    private int sourceId;
    private int destinationId;

    public ShipmentDetails(){}

    public ShipmentDetails(String itemCode,int quantity,float totalPrice,int shipmentId,
                                    String shipmentType,int sourceId,int destinationId){

        this.itemCode=itemCode;
        this.quantity=quantity;
        this.totalPrice=totalPrice;
        this.shipmentId=shipmentId;
        this.shipmentType=shipmentType;
        this.sourceId=sourceId;
        this.destinationId=destinationId;

    }

    public String getItemCode() {return itemCode;}

    public void setItemCode(String itemCode) {this.itemCode = itemCode;}

    public int getQuantity() {return quantity;}

    public void setQuantity(int quantity) {this.quantity = quantity;}

    public float getTotalPrice() {return totalPrice;}

    public void setTotalPrice(float totalPrice) {this.totalPrice = totalPrice;}

    public int getShipmentId() {return shipmentId;}

    public void setShipmentId(int shipmentId) {this.shipmentId = shipmentId;}

    public String getShipmentType() {return shipmentType;}

    public void setShipmentType(String shipmentType) {this.shipmentType = shipmentType;}

    public int getSourceId() {return sourceId;}

    public void setSourceId(int sourceId) {this.sourceId = sourceId;}

    public int getDestinationId() {return destinationId;}

    public void setDestinationId(int destinationId) {this.destinationId = destinationId;}

    @Override
    public String toString() {
        return "ShipmentDetails{" +
                "itemCode='" + itemCode + '\'' +
                ", quantity=" + quantity +
                ", totalPrice=" + totalPrice +
                ", shipmentId=" + shipmentId +
                ", shipmentType='" + shipmentType + '\'' +
                ", sourceId=" + sourceId +
                ", destinationId=" + destinationId +
                '}';
    }
}


