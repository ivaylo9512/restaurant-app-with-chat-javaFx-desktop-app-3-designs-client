package Models;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class Order{

    private int id;
    private List<Dish> dishes;
    private LocalDateTime created;
    private LocalDateTime updated;

    private boolean ready = false;
    public Order() {
    }

    public Order(List<Dish> dishes) {
        this.id = id;
        this.dishes = dishes;
        this.created = created;
        this.updated = updated;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public List<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }
}
