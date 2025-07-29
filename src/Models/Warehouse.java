package Models;

public class Warehouse {

    private int id;
    private int manegeId;
    private int capacity;
    private String location;

    public Warehouse(int id, int manegeId, int capacity, String location) {
        this.id = id;
        this.manegeId = manegeId;
        this.capacity = capacity;
        this.location = location;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getManegeId() {
        return manegeId;
    }

    public void setManegeId(int manegeId) {
        this.manegeId = manegeId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
