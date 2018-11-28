package dk.tennarasmussen.thedinnerclub.Model;

public class ClubInvitation {
    public String recipientId;
    public String senderId;
    public String senderName;
    public String dinnerClubName;
    public boolean accepted;

    public ClubInvitation() {
    }

    public ClubInvitation(String recipientId, String senderId, String senderName, String dinnerClubName) {
        this.recipientId = recipientId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.dinnerClubName = dinnerClubName;
        this.accepted = false;
    }
}
