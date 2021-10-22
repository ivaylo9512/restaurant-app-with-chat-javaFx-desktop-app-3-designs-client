package models;

public class Menu {
    private long id;
    private String name;
    private long restaurantId;

    public Menu() {
    }

    public Menu(Menu menu){
        this.id = menu.getId();
        this.name = menu.getName();
    }

    public Menu(int id, String name) {
        this.id = id;
        this.name = name;
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

    public long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(long restaurantId) {
        this.restaurantId = restaurantId;
    }
}
