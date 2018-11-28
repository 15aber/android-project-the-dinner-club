package dk.tennarasmussen.thedinnerclub.Model;

public class User {

    private String name;
    private String streetName;
    private String zipCode;
    private String city;
    private long phone;
    private String email;
    private String dinnerClub;
    private String clubInvitation;

    public User() {
    }

    public User(String name, String streetName, String zipCode, String city, long phone, String email) {
        this.name = name;
        this.streetName = streetName;
        this.zipCode = zipCode;
        this.city = city;
        this.phone = phone;
        this.email = email;
    }

    public User(String name, String streetName, String zipCode, String city, long phone, String email, String dinnerClub) {
        this.name = name;
        this.streetName = streetName;
        this.zipCode = zipCode;
        this.city = city;
        this.phone = phone;
        this.email = email;
        this.dinnerClub = dinnerClub;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public long getPhone() {
        return phone;
    }

    public void setPhone(int phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDinnerClub() {
        return dinnerClub;
    }

    public void setDinnerClub(String dinnerClub) {
        this.dinnerClub = dinnerClub;
    }

    public String getClubInvitation() {
        return clubInvitation;
    }

    public void setClubInvitation(String clubInvitation) {
        this.clubInvitation = clubInvitation;
    }
}
