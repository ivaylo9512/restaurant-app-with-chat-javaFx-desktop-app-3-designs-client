package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Order{
    private final LongProperty id = new SimpleLongProperty();
    private final ObservableList<Dish> dishes = FXCollections.observableArrayList();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();
    private long userId;
    private long restaurantId;
    private int index;
    private boolean ready;

    public Order() {
    }
    public Order(long id) {
        this.id.set(id);
    }
    public Order(List<Dish> dishes) {
        this.dishes.setAll(dishes);
    }

    public LongProperty getId() {
        return id;
    }

    public void setId(long id) {
        this.id.setValue(id);
    }


    public ObservableList<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes.setAll(dishes);
    }

    public ObjectProperty<LocalDateTime> getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime created) {
        this.createdAt.setValue(created);
    }

    public ObjectProperty<LocalDateTime> getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updated) {
        this.updatedAt.setValue(updated);
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @JsonProperty("id")
    public long getIdValue(){
        return id.get();
    }

    @JsonProperty("createdAt")
    public LocalDateTime getCreateAtValue(){
        return createdAt.get();
    }

    @JsonProperty("updatedAt")
    public LocalDateTime getUpdatedAtValue(){
        return updatedAt.get();
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
        return Objects.hash(id.get());
    }

    public long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
