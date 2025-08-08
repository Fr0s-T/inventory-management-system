package Tests;

import Utilities.QRCodeUtils;

public class TestQR {
    public static void main(String[] args) {
        String sampleJson = """
        {
          "type": "OUT",
          "src": "Beirut Central",
          "items": [
            {"code": "X123", "name": "Cotton Shirt", "qty": 10, "price": 14.5}
          ]
        }
        """;

        QRCodeUtils.generateQRCode(sampleJson, "C:/Users/fouad/Desktop/shipment_qr.png");
    }
}
