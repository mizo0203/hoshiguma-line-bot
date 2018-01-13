package com.mizo0203.hoshiguma.repo.line.messaging.data;

@SuppressWarnings({"unused", "WeakerAccess"})
public class PushMessageData {

  private final String to;
  private final MessageObject[] messages;

  public PushMessageData(String to, MessageObject[] messages) {
    this.to = to;
    this.messages = messages;
  }
}
