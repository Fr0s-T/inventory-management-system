package ViewsControllers.ShipmentForm;

import Models.ShipmentQRPayload;

/**
 *
 *
 * Author: @Frost
 *
 */

public class QrShipmentMapper {

    // Sealed-ish partner outcome (no preview sealed keywords for wide compatibility)
    public interface Partner {}

    public record PartnerId(int id) implements Partner {
    }

    public record PartnerExternal(String name) implements Partner {
        public PartnerExternal(String name) {
            this.name = name == null ? "" : name;
        }
    }

    /**
     * Determines the shipment kind (Reception or Expedition) from the perspective of the current warehouse.
     */
    public ShipmentController.ShipmentKind decideLocalKind(ShipmentQRPayload payload, int currentWarehouseId) {
        Integer destinationWarehouseId = parsePositiveInt(payload.destination());
        if (destinationWarehouseId != null && destinationWarehouseId == currentWarehouseId) {
            return ShipmentController.ShipmentKind.RECEPTION;
        }

        Integer sourceWarehouseId = parsePositiveInt(payload.source());
        if (sourceWarehouseId != null && sourceWarehouseId == currentWarehouseId) {
            return ShipmentController.ShipmentKind.EXPEDITION;
        }

        // Fallback to label inversion if warehouse IDs don't match
        String typeFromPayload = payload.getType();
        if (typeFromPayload != null) {
            String normalizedType = typeFromPayload.trim().toLowerCase();
            if ("expedition".equals(normalizedType)) {
                return ShipmentController.ShipmentKind.RECEPTION;
            }
            if ("reception".equals(normalizedType)) {
                return ShipmentController.ShipmentKind.EXPEDITION;
            }
        }
        return ShipmentController.ShipmentKind.RECEPTION;
    }

    /**
     * Determines the partner when the shipment is being received.
     */
    public Partner resolvePartnerForReception(ShipmentQRPayload payload) {
        if (payload.isInNetwork()) {
            Integer sourceWarehouseId = parsePositiveInt(payload.source());
            if (sourceWarehouseId != null) {
                return new PartnerId(sourceWarehouseId);
            }
        }
        return new PartnerExternal(nz(payload.source()));
    }

    /**
     * Determines the partner when the shipment is being sent (expedition).
     */
    public Partner resolvePartnerForExpedition(ShipmentQRPayload payload) {
        if (payload.isInNetwork()) {
            Integer destinationWarehouseId = parsePositiveInt(payload.destination());
            if (destinationWarehouseId != null) {
                return new PartnerId(destinationWarehouseId);
            }
        }
        return new PartnerExternal(nz(payload.destination()));
    }

    /**
     * Safely parses a positive integer from a string.
     */
    public Integer parsePositiveInt(String s) {
        if (s == null) return null;
        try {
            int parsedValue = Integer.parseInt(s.trim());
            if (parsedValue > 0) return parsedValue;
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns an empty string if the provided string is null.
     */
    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
