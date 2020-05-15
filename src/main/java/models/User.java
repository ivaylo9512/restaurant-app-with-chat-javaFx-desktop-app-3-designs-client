package models;


import javafx.beans.property.*;
import javafx.scene.image.Image;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

public class User {
    private IntegerProperty id = new SimpleIntegerProperty();
    private StringProperty username = new SimpleStringProperty();
    private StringProperty firstName = new SimpleStringProperty();
    private StringProperty lastName = new SimpleStringProperty();
    private StringProperty age = new SimpleStringProperty();
    private StringProperty country = new SimpleStringProperty();
    private StringProperty role = new SimpleStringProperty();
    private StringProperty profilePicture = new SimpleStringProperty();
    private Restaurant restaurant;
    private List<Chat> chats = new ArrayList<>();
    private LocalDateTime lastCheck;

    private ObjectProperty<Image> image = new SimpleObjectProperty<>();

    private static Image defaultImage = new Image(User.class.getResourceAsStream("/images/default-picture.png"));


    public User(){
    }

    public User(int id, String username, String firstName, String lastName, int age, String country, String role, String profilePicture, Restaurant restaurant) {
        this.id.setValue(id);
        this.username.setValue(username);
        this.firstName.setValue(firstName);
        this.lastName.setValue(lastName);
        this.age.setValue(String.valueOf(age));
        this.country.setValue(country);
        this.role.setValue(role);
        this.profilePicture.setValue(profilePicture);
        this.restaurant = restaurant;
    }

    public User(String username, String firstName, String lastName, int age, String country) {
        this.username.setValue(username);
        this.firstName.setValue(firstName);
        this.lastName.setValue(lastName);
        this.age.setValue(String.valueOf(age));
        this.country.setValue(country);
    }
    public User(User user) {
        this.firstName.setValue(user.getFirstName().get());
        this.lastName.setValue(user.getLastName().get());
        this.age.setValue(user.getAge().get());
        this.country.setValue(user.getCountry().get());
    }

    public StringProperty getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username.setValue(username);
    }

    public IntegerProperty getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id.setValue(id);
    }

    public StringProperty getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role.setValue(role);
    }

    public StringProperty getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName.setValue(firstName);
    }

    public StringProperty getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName.setValue(lastName);
    }

    public StringProperty getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age.setValue(String.valueOf(age));
    }

    public StringProperty getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country.setValue(country);
    }

    public StringProperty getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        try(InputStream in = new BufferedInputStream(new URL(profilePicture).openStream())){
            image.set(new Image(in));
        }catch(Exception e){
            image.set(defaultImage);
        }
        this.profilePicture.setValue(profilePicture);
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public ObjectProperty<Image> getImage() {
        return image;
    }

    public List<Chat> getChats() {
        return chats;
    }

    public void setChats(List<Chat> chats) {
        this.chats = chats;
    }

    public void setImage(Image image) {
        this.image.setValue(image);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return firstName.get().equals(user.firstName.get()) &&
                lastName.get().equals(user.lastName.get()) &&
                age.get().equals(user.age.get()) &&
                country.get().equals(user.country.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(username.get(), firstName.get(), lastName.get(), age.get(), country.get());
    }

    public LocalDateTime getLastCheck() {
        return lastCheck;
    }

    public void setLastCheck(LocalDateTime lastCheck) {
        this.lastCheck = lastCheck;
    }
}