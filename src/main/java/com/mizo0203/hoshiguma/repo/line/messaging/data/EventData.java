package com.mizo0203.hoshiguma.repo.line.messaging.data;

@SuppressWarnings({"unused", "WeakerAccess"})
public class EventData {

  public static final String TYPE_JOIN = "join";
  public static final String TYPE_MESSAGE = "message";
  public String replyToken;
  public String type;
  public long timestamp;
  public SourceData source;
  public MessageData message;
}
