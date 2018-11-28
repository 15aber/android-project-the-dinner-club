package dk.tennarasmussen.thedinnerclub;

//From https://stackoverflow.com/a/43863803
public class EmailEncoder {
    static String encodeUserEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }

    static String decodeUserEmail(String userEmail) {
        return userEmail.replace(",", ".");
    }

}
