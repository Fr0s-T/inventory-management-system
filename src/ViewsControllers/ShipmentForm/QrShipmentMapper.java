package ViewsControllers.ShipmentForm;

import Models.ShipmentQRPayload;

public class QrShipmentMapper {

    // Sealed-ish partner outcome (no preview sealed keywords for wide compatibility)
    public interface Partner {}
    public static final class PartnerId implements Partner {
        private final int id;
        public PartnerId(int id) { this.id = id; }
        public int id() { return id; }
    }
    public static final class PartnerExternal implements Partner {
        private final String name;
        public PartnerExternal(String name) { this.name = name == null ? "" : name; }
        public String name() { return name; }
    }

    public ShipmentController.ShipmentKind decideLocalKind(ShipmentQRPayload payload, int currentWarehouseId) {
        Integer destId = parsePositiveInt(payload.destination());
        if (destId != null && destId == currentWarehouseId) {
            return ShipmentController.ShipmentKind.RECEPTION;
        }

        Integer srcId = parsePositiveInt(payload.source());
        if (srcId != null && srcId == currentWarehouseId) {
            return ShipmentController.ShipmentKind.EXPEDITION;
        }

        // Fallback to label inversion
        String t = payload.getType();
        if (t != null) {
            String tt = t.trim().toLowerCase();
            if ("expedition".equals(tt)) {
                return ShipmentController.ShipmentKind.RECEPTION;
            }
            if ("reception".equals(tt)) {
                return ShipmentController.ShipmentKind.EXPEDITION;
            }
        }
        return ShipmentController.ShipmentKind.RECEPTION;
    }

    public Partner resolvePartnerForReception(ShipmentQRPayload payload) {
        if (payload.isInNetwork()) {
            Integer id = parsePositiveInt(payload.source());
            if (id != null) {
                return new PartnerId(id);
            }
        }
        return new PartnerExternal(nz(payload.source()));
    }

    public Partner resolvePartnerForExpedition(ShipmentQRPayload payload) {
        if (payload.isInNetwork()) {
            Integer id = parsePositiveInt(payload.destination());
            if (id != null) {
                return new PartnerId(id);
            }
        }
        return new PartnerExternal(nz(payload.destination()));
    }

    public Integer parsePositiveInt(String s) {
        if (s == null) return null;
        try {
            int v = Integer.parseInt(s.trim());
            if (v > 0) return v;
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
