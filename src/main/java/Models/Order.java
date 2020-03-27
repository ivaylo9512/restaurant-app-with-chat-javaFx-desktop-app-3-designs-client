package Models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Order{

    private IntegerProperty id;
    private ObservableList<Dish> dishes = FXCollections.observableArrayList();
    private ObjectProperty<LocalDateTime> created = new SimpleObjectProperty<>();
    private ObjectProperty<LocalDateTime> updated = new SimpleObjectProperty<>();
    private int userId;

    private boolean ready = false;
    public Order() {
    }

    public Order(List<Dish> dishes) {
        this.dishes.setAll(dishes);
    }

    public IntegerProperty getId() {
        return id;
    }

    public void setId(int id) {
        this.id.setValue(id);
    }


    public ObservableList<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes.setAll(dishes);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id.get() == order.id.get();
    }

    @Override
    public int hashCode() {
        return id.get();
    }
}
