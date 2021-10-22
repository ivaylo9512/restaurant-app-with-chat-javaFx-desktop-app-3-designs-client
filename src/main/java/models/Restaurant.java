package models;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Restaurant {
    private long id;
    private String name;
    private String address;
    private String type;
    private List<Menu> menu;
    private List<Order> orders;

    public Restaurant() {
    }

    public Restaurant(long id, String name, String address, String type, List<Menu> menu) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.type = type;
        this.menu = menu;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Menu> getMenu() {
        return menu;
    }

    public void setMenu(List<Menu> menu) {
        this.menu = menu;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(Map<Integer, Order> orders) {
        this.orders = new ArrayList<>(orders.values());
    }
}
