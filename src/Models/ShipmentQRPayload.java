package Models;

import java.util.List;

public record ShipmentQRPayload(String shipmentType, String source, String destination, boolean isInNetwork,
                                List<ItemEntry> items) {

    // ✅ Add this method so controller can access shipmentType properly
    public String getType() {
        return shipmentType;
    }

    // ========== NEW METHODS ==========

    public boolean isOutsideNetwork() {
        return !isInNetwork;
    }

    public String getSourceName() {
        return isOutsideNetwork() && "Reception".equalsIgnoreCase(shipmentType) ? source : null;
    }

    public String getDestinationName() {
        return isOutsideNetwork() && "Expedition".equalsIgnoreCase(shipmentType) ? destination : null;
    }

    public int getSourceId() {
        if (!isOutsideNetwork() && "Reception".equalsIgnoreCase(shipmentType)) {
            try {
                return Integer.parseInt(source);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    public int getDestinationId() {
        if (!isOutsideNetwork() && "Expedition".equalsIgnoreCase(shipmentType)) {
            try {
                return Integer.parseInt(destination);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return "ShipmentQRPayload{" +
                "shipmentType='" + shipmentType + '\'' +
                ", source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", isInNetwork=" + isInNetwork +
                ", items=" + items +
                '}';
    }
}
