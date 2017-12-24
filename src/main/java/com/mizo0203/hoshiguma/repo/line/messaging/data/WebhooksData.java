package com.mizo0203.hoshiguma.repo.line.messaging.data;

import java.util.Arrays;

@SuppressWarnings({"unused", "WeakerAccess"})
public class WebhooksData {

  public EventData[] events;

  @Override
  public String toString() {
    return "events: " + Arrays.toString(events);
  }
}
