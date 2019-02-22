package Models;

import java.io.Serializable;

public class Dish{
    private int id;
    private String name;
    private Boolean ready = false;

    public Dish() {
    }

    public Dish(int id, String name, Boolean ready) {
        this.id = id;
        this.name = name;
        this.ready = ready;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getReady() {
        return ready;
    }

    public void setReady(Boolean ready) {
        this.ready = ready;
    }
}
