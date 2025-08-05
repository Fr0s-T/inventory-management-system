package Models;

/**
 *
 * Author: @Ilia
 *
 */


public class Shipment {
    private final int id;
    private final String date;
    private final String source;
    private final String destination;
    private final int quantity;
    private final String handledBy;

    public Shipment(int id, String date, String source, String destination, int quantity, String handledBy) {
        this.id = id;
        this.date = date;
        this.source = source;
        this.destination = destination;
        this.quantity = quantity;
        this.handledBy = handledBy;
    }


    public int getId() { return id; }
    public String getDate() { return date; }
    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public int getQuantity() { return quantity; }
    public String getHandledBy() { return handledBy; }
}

