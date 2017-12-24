package com.mizo0203.hoshiguma.repo.line.messaging.data;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class MessageObject {

  public final String type;

  public MessageObject(String type) {
    this.type = type;
  }
}
