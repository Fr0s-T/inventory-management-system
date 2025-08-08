package Models;

import java.util.List;

public class ShipmentQRPayload {

    private final String shipmentType;
    private final String source;
    private final boolean isInNetwork;
    private final List<ItemEntry> items;

    public ShipmentQRPayload(String shipmentType, String source, boolean isInNetwork, List<ItemEntry> items) {
        this.shipmentType = shipmentType;
        this.source = source;
        this.isInNetwork = isInNetwork;
        this.items = items;
    }

    public String getShipmentType() {
        return shipmentType;
    }

    public String getSource() {
        return source;
    }

    public boolean isInNetwork() {
        return isInNetwork;
    }

    public List<ItemEntry> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return "ShipmentQRPayload{" +
                "shipmentType='" + shipmentType + '\'' +
                ", source='" + source + '\'' +
                ", isInNetwork=" + isInNetwork +
                ", items=" + items +
                '}';
    }
}
