package com.mizo0203.hoshiguma.repo.line.messaging.data;

import java.util.Arrays;

/** リクエストボディ */
public class RequestBody {

  public WebHookEventObject[] events;

  @Override
  public String toString() {
    return "events: " + Arrays.toString(events);
  }
}
