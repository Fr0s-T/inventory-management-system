package Classes;

/*
 *
 * Author: @Frost
 *
 */

public class User {
    public int getLoginAttempts() {
        return loginAttempts;
    }

    public void setLoginAttempts(int loginAttempts) {
        this.loginAttempts = loginAttempts;
    }

    public enum Role {
        REGIONAL_MANAGER(1),//warehouse id = null maneges many warehouses
        WAREHOUSE_MANAGER(2),
        SHIFT_MANAGER(3),
        EMPLOYEE(4);

        private final int id;

        Role(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static Role fromId(int id) {
            for (Role role : Role.values()) {
                if (role.getId() == id) {
                    return role;
                }
            }
            throw new IllegalArgumentException("Invalid role ID: " + id);
        }
    }

    private final int id;
    private final String firstName;
    private final String middleName;
    private final String lastName;
    private final String username;
    private final Role role;
    private final int warehouseId;
    private final String picture;
    private int loginAttempts = 0;

    public User(int id, String firstName, String middleName, String lastName, String username, int roleId, int warehouseId, String picture) {
        this.id = id;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.username = username;
        this.role = Role.fromId(roleId);
        this.warehouseId = warehouseId;
        this.picture = picture;
    }
    public User(int id, String firstName, String middleName, String lastName, String username, int roleId, int warehouseId) {
        this(id, firstName, middleName, lastName, username, roleId, warehouseId, null);
    }
    // Getters for all fields

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public int getWarehouseId() {
        return warehouseId;
    }

    public String getPicture() {
        return picture;
    }
    @Override
    public String toString() {
        return "User ID: " + id + "\n"
                + "Name: " + firstName + " " + middleName + " " + lastName + "\n"
                + "Username: " + username + "\n"
                + "Role: " + role + "\n"
                + "Warehouse ID: " + warehouseId + "\n"
                + "Picture: " + (picture != null ? picture : "No picture") + "\n"
                + "---------------------------------";
    }

}
