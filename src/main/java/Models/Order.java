package Models;

import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.List;

public class Order{

    private IntegerProperty id = new SimpleIntegerProperty();
    private ObservableList<Dish> dishes = FXCollections.observableArrayList();
    private ObjectProperty<LocalDateTime> created = new SimpleObjectProperty<>();
    private ObjectProperty<LocalDateTime> updated = new SimpleObjectProperty<>();
    private int userId;
    private int restaurantId;

    private boolean ready;
    public Order() {
    }
    public Order(int id) {
        this.id.set(id);
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

    @JsonProperty("id")
    public int getIdValue(){
        return id.get();
    }

    @JsonProperty("created")
    public LocalDateTime getCreateValue(){
        return created.get();
    }

    @JsonProperty("updated")
    public LocalDateTime getUpdatedValue(){
        return updated.get();
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

    public int getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }
}
