package Utilities;

import Models.Product;
import Models.Warehouse;
import Models.ShipmentQRPayload;
import Models.ItemEntry;
import com.google.gson.Gson;
import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QRCodeUtils {

    public static void generateQRCode(String content, String filePath) {
        try {
            int width = 500;
            int height = 500;

            // Setup QR parameters with higher error correction
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // High = 30% recovery

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            Path path = FileSystems.getDefault().getPath(filePath);
            MatrixToImageWriter.writeToPath(matrix, "PNG", path);

            System.out.println("✅ QR Code saved at: " + filePath);
        } catch (WriterException | IOException e) {
            System.err.println("❌ Failed to generate QR Code: " + e.getMessage());
        }
    }

    public static void generateShipmentQRCode(ArrayList<Product> items,
                                              ArrayList<Integer> quantities,
                                              Warehouse source,
                                              boolean isInNetwork,
                                              String outsideNetworkName,
                                              String filePath) {
        try {
            // Step 1: Convert items + quantities → ItemEntry list
            List<ItemEntry> itemEntries = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                Product product = items.get(i);
                int quantity = quantities.get(i);
                itemEntries.add(new ItemEntry(
                        product.getItemCode(),
                        product.getName(),
                        quantity,
                        product.getUnitPrice()
                ));
            }

            // Step 2: Build Payload
            ShipmentQRPayload payload = new ShipmentQRPayload(
                    "Expedition",
                    isInNetwork ? String.valueOf(source.getId()) : outsideNetworkName,
                    isInNetwork,
                    itemEntries
            );

            // Step 3: Serialize and Generate QR Code
            Gson gson = new Gson();
            String jsonPayload = gson.toJson(payload);

            generateQRCode(jsonPayload, filePath);

        } catch (Exception e) {
            System.err.println("❌ Failed to generate QR Code: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
