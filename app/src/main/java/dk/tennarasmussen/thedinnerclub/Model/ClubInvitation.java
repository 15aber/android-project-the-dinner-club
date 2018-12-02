package dk.tennarasmussen.thedinnerclub.Model;

public class ClubInvitation {
  public String recipientId;
  public String senderId;
  public String dinnerClubId;
  public String senderName;
  public String dinnerClubName;
  public boolean accepted;

  public ClubInvitation() {
  }

  public ClubInvitation(String recipientId, String senderId, String dinnerClubId, String senderName, String dinnerClubName) {
    this.recipientId = recipientId;
    this.senderId = senderId;
    this.dinnerClubId = dinnerClubId;
    this.senderName = senderName;
    this.dinnerClubName = dinnerClubName;
    this.accepted = false;
  }
}