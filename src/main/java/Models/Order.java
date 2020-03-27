package Models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class Order{

    private IntegerProperty id;
    private ObjectProperty<List<Dish>> dishes = new SimpleObjectProperty<>();
    private ObjectProperty<LocalDateTime> created = new SimpleObjectProperty<>();
    private ObjectProperty<LocalDateTime> updated = new SimpleObjectProperty<>();
    private int userId;

    private boolean ready = false;
    public Order() {
    }

    public Order(List<Dish> dishes) {
        this.dishes.setValue(dishes);
    }

    public IntegerProperty getId() {
        return id;
    }

    public void setId(int id) {
        this.id.setValue(id);
    }


    public ObjectProperty<List<Dish>> getDishes() {
        return dishes;
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes.setValue(dishes);
    }

    public ObjectProperty<LocalDateTime> getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created.setValue(created);
    }

    public ObjectProperty<LocalDateTime> getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated.setValue(updated);
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
