package model;

import java.util.Map;

import db.DataBase;

public class User {
    private String userId;
    private String password;
    private String name;
    private String email;

    public User(String userId, String password, String name, String email) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "User [userId=" + userId + ", password=" + password + ", name=" + name + ", email=" + email + "]";
    }

    public static void create(Map<String, String> user) {
        String userId = user.get("userId");
        String password = user.get("password");
        String name = user.get("name");
        String email = user.get("email");
        DataBase.addUser(new User(userId, password, name, email));
    }
}
