package models;

import javafx.beans.property.*;
import javafx.scene.image.Image;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

import static application.ServerRequests.base;

public class User {
    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty firstName = new SimpleStringProperty();
    private final StringProperty lastName = new SimpleStringProperty();
    private final StringProperty age = new SimpleStringProperty();
    private final StringProperty country = new SimpleStringProperty();
    private final StringProperty role = new SimpleStringProperty();
    private final StringProperty profileImage = new SimpleStringProperty();
    private Restaurant restaurant;
    private Map<Long, Chat> chats = new HashMap<>();
    private LocalDateTime lastCheck;

    private final ObjectProperty<Image> image = new SimpleObjectProperty<>();

    private static final Image defaultImage = new Image(User.class.getResourceAsStream("/images/default-picture.png"));


    public User(){
    }

    public User(long id, String username, String firstName, String lastName, int age, String country, String role, String profileImage, Restaurant restaurant) {
        this.id.setValue(id);
        this.username.setValue(username);
        this.firstName.setValue(firstName);
        this.lastName.setValue(lastName);
        this.age.setValue(String.valueOf(age));
        this.country.setValue(country);
        this.role.setValue(role);
        this.profileImage.setValue(profileImage);
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

    public StringProperty getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email.setValue(email);
    }

    public LongProperty getId() {
        return id;
    }

    public void setId(Long id) {
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

    public StringProperty getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        try(InputStream in = new BufferedInputStream(new URL(base + "/api/files/download/" + profileImage).openStream())){
            image.set(new Image(in));
        }catch(Exception e){
            image.set(defaultImage);
        }
        this.profileImage.setValue(profileImage);
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

    public Map<Long, Chat> getChats() {
        return chats;
    }

    public void setChats(Map<Long, Chat> chats) {
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