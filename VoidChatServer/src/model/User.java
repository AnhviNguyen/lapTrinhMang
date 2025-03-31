package model;

import java.io.Serializable;

/**
 * ClassName : User.java
 * Description : class to represent user in chat
 *
 * @author MotYim
 * @since 11-02-2017
 */
public class User implements Serializable {

    protected String username;
    protected String email;
    protected String fname;
    protected String lname;
    protected String password;
    protected String gender;
    protected String country;
    protected String status;
    protected String image;

    public User() {
    }

    public User(String username, String email, String fname, String lname, String password, String gender,
                String country) {
        this.username = username;
        this.email = email;
        this.fname = fname;
        this.lname = lname;
        this.password = password;
        this.gender = gender;
        this.country = country;
        // Set default image based on gender
        this.image = gender.equals("male") ? "/resouces/male.png" : "/resouces/female.png";
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(String username, String fname, String lname, String gender, String country) {
        this.username = username;
        this.fname = fname;
        this.lname = lname;
        this.gender = gender;
        this.country = country;
        // Set default image based on gender
        this.image = gender.equals("male") ? "/resouces/male.png" : "/resouces/female.png";
    }

    public User(String username, String email, String fname, String lname, String password, String gender,
                String country, String status) {
        this.username = username;
        this.email = email;
        this.fname = fname;
        this.lname = lname;
        this.password = password;
        this.gender = gender;
        this.country = country;
        this.status = status;
        // Set default image based on gender
        this.image = gender.equals("male") ? "/resouces/male.png" : "/resouces/female.png";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}