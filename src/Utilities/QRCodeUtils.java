package Utilities;

import Models.Product;
import Models.Warehouse;
import Models.ShipmentQRPayload;
import Models.ItemEntry;
import com.google.gson.Gson;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
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
                                              Warehouse destination,
                                              boolean isInNetwork,
                                              String outsideNetworkName,
                                              String filePath,
                                              String shipmentType) {
        try {
            // Ensure directory exists
            Path path = FileSystems.getDefault().getPath(filePath);
            Path dir = path.getParent();
            if (dir != null && !java.nio.file.Files.exists(dir)) {
                java.nio.file.Files.createDirectories(dir);
            }

            // Convert to ItemEntry list
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

            // Decide how to represent source and destination
            String sourceStr = source != null ? String.valueOf(source.getId()) : outsideNetworkName;
            String destinationStr = destination != null ? String.valueOf(destination.getId()) : outsideNetworkName;

            // Create payload
            ShipmentQRPayload payload = new ShipmentQRPayload(
                    shipmentType,
                    sourceStr,
                    destinationStr,
                    isInNetwork,
                    itemEntries
            );

            // Serialize and generate QR
            Gson gson = new Gson();
            String jsonPayload = gson.toJson(payload);

            generateQRCode(jsonPayload, filePath);

        } catch (Exception e) {
            System.err.println("❌ Failed to generate QR Code: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static ShipmentQRPayload decodeShipmentQRCode(String filePath) {
        try {
            BufferedImage image = ImageIO.read(new File(filePath));
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = new MultiFormatReader().decode(bitmap);
            String json = result.getText();

            Gson gson = new Gson();
            return gson.fromJson(json, ShipmentQRPayload.class);

        } catch (Exception e) {
            System.err.println("❌ Failed to decode QR Code: " + e.getMessage());
            return null;
        }
    }




}
