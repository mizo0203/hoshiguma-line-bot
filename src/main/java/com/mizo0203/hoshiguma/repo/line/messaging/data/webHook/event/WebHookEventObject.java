package com.mizo0203.hoshiguma.repo.line.messaging.data.webHook.event;

import com.mizo0203.hoshiguma.repo.line.messaging.data.MessageData;
import com.mizo0203.hoshiguma.repo.line.messaging.data.PostBack;
import com.mizo0203.hoshiguma.repo.line.messaging.data.SourceData;

@SuppressWarnings({"unused", "WeakerAccess", "SpellCheckingInspection"})
public class WebHookEventObject {

  public static final String TYPE_JOIN = "join";
  public static final String TYPE_MESSAGE = "message";
  public static final String TYPE_POST_BACK = "postback";
  /* package */ String replyToken;
  /* package */ String type;
  /* package */ long timestamp;
  /* package */ SourceData source;
  /* package */ MessageData message;
  /* package */ PostBack postback;

  private <T extends WebHookEventObject> T copy(T dest) {
    dest.replyToken = replyToken;
    dest.type = type;
    dest.timestamp = timestamp;
    dest.source = source;
    dest.message = message;
    dest.postback = postback;
    return dest;
  }

  public JoinEvent getJoinEventObject() {
    switch (type) {
      case TYPE_JOIN:
        return new JoinEvent();
      default:
        return null;
    }
  }

  /** @return イベントのタイプを表す識別子 */
  public String getType() {
    return type;
  }

  /** @return イベントの発生時刻（ミリ秒） */
  public long getTimestamp() {
    return timestamp;
  }

  /** @return イベントの送信元情報を含むユーザー、グループ、またはトークルームオブジェクト */
  public SourceData getSource() {
    return source;
  }

  public WebHookEventObject concrete() {
    switch (type) {
      case TYPE_JOIN:
        return copy(new JoinEvent());
      case TYPE_MESSAGE:
        return copy(new MessageEvent());
      case TYPE_POST_BACK:
        return copy(new PostBackEvent());
      default:
        return null;
    }
  }
}
