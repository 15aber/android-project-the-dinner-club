package dk.tennarasmussen.thedinnerclub.Model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//Inspired by https://firebase.google.com/docs/database/android/read-and-write
//@IgnoreExtraProperties
public class DinnerClub {
    public String clubId;
    public String clubName;
    public Map<String, Boolean> members = new HashMap<>();

    public DinnerClub() {
    }

    public DinnerClub(String clubId, String clubName) {
        this.clubId = clubId;
        this.clubName = clubName;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("clubId", clubId);
        result.put("clubName", clubName);
        result.put("members", members);

        return result;
    }
}
