package com.mizo0203.hoshiguma.repo.line.messaging.data;

@SuppressWarnings({"unused", "WeakerAccess", "SpellCheckingInspection"})
public class WebHookEventObject {

  public static final String TYPE_JOIN = "join";
  public static final String TYPE_MESSAGE = "message";
  public static final String TYPE_POST_BACK = "postback";
  public String replyToken;
  public String type;
  public long timestamp;
  public SourceData source;
  public MessageData message;
  public PostBack postback;
}
