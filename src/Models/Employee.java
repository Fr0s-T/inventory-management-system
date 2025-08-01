package Models;

public class Employee {

    private int id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String username;
    private String password;
    private boolean onDuty;
    private int roleID;
    private int warehouseID;


    public Employee(int id, String fristName, String middleName, String lastName, String username, String password, boolean onDuty, int roleID, int warehouseID) {

        this.id = id;
        this.firstName=fristName;
        this.middleName=middleName;
        this.lastName=lastName;
        this.username=username;
        this.password=password;
        this.onDuty=onDuty;
        this.roleID=roleID;
        this.warehouseID=warehouseID;

    }

    public Employee(String fristName, String middleName, String lastName, String username, String password, boolean onDuty, int roleID, int warehouseID) {

        this.firstName=fristName;
        this.middleName=middleName;
        this.lastName=lastName;
        this.username=username;
        this.password=password;
        this.onDuty=onDuty;
        this.roleID=roleID;
        this.warehouseID=warehouseID;

    }

    public Employee(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isOnDuty() {
        return onDuty;
    }

    public void setOnDuty(boolean onDuty) {
        this.onDuty = onDuty;
    }

    public int getRoleID() {
        return roleID;
    }

    public void setRoleID(int roleID) {
        this.roleID = roleID;
    }

    public int getWarehouseID() {
        return warehouseID;
    }

    public void setWarehouseID(int warehouseID) {
        this.warehouseID = warehouseID;
    }
}

