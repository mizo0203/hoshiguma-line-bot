package com.mizo0203.hoshiguma.repo.line.messaging.data;

@SuppressWarnings({"unused", "WeakerAccess"})
public class SourceData {

  private String type;
  private String userId;
  private String groupId;
  private String roomId;

  public String getSourceId() {
    switch (type) {
      case "user":
        return userId;
      case "group":
        return groupId;
      case "room":
        return roomId;
      default:
        return null;
    }
  }

  public String getUserId() {
    return userId;
  }
}
