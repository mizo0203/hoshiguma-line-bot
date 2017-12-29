package com.mizo0203.hoshiguma.repo.line.messaging.data.webHook.event;

import java.util.Arrays;

/** リクエストボディ */
public class RequestBody {
  private WebHookEventObject[] events;

  public WebHookEventObject[] concreteWebHookEventObject() {
    WebHookEventObject[] ret = new WebHookEventObject[events.length];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = events[i].concrete();
    }
    return ret;
  }

  @Override
  public String toString() {
    return "events: " + Arrays.toString(events);
  }
}
