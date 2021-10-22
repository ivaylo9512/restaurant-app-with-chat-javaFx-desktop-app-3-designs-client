package models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Dish{
    private long id;
    private String name;
    private boolean ready;
    private long orderId;
    private boolean loading;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long updatedById;
    private boolean orderReady;

    public Dish() {
    }

    public Dish(String name) {
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

    public boolean isReady() {
        return ready;
    }

    public void setReady(Boolean ready) {
        this.ready = ready;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return id == dish.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public long getUpdatedById() {
        return updatedById;
    }

    public void setUpdatedById(long updatedById) {
        this.updatedById = updatedById;
    }

    public boolean isOrderReady() {
        return orderReady;
    }

    public void setOrderReady(boolean orderReady) {
        this.orderReady = orderReady;
    }
}
