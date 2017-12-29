package com.mizo0203.hoshiguma.repo.line.messaging.data.webHook.event;

/**
 * 参加イベント
 *
 * <p>アカウントがグループまたはトークルームに参加したことを示すイベントです。参加イベントには応答できます。
 *
 * <p>https://developers.line.me/ja/docs/messaging-api/reference/#anchor-511183a26c172a77583ef45edd4a5a0f596c5623
 */
public class JoinEvent extends WebHookEventObject {

  /* package */ JoinEvent() {
    // NOP
  }

  /** @return このイベントへの応答に使用するトークン */
  public String getReplyToken() {
    return replyToken;
  }
}
