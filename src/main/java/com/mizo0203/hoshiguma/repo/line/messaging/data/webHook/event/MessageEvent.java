package com.mizo0203.hoshiguma.repo.line.messaging.data.webHook.event;

import com.mizo0203.hoshiguma.repo.line.messaging.data.MessageData;

/**
 * メッセージイベント
 *
 * <p>送信されたメッセージを含む webhook イベントオブジェクトです。 メッセージのタイプに対応するメッセージオブジェクトが、message
 * プロパティに含まれます。メッセージイベントには応答できます。
 *
 * <p>https://developers.line.me/ja/docs/messaging-api/reference/#anchor-e2c59da01216760e81e8ca10c55b2e28b276c3e5
 */
public class MessageEvent extends WebHookEventObject {
  /* package */ MessageEvent() {
    // NOP
  }

  /** @return イベントへの応答に使用するトークン */
  public String getReplyToken() {
    return replyToken;
  }

  /** @return メッセージの内容を含むオブジェクト。メッセージには以下のタイプがあります。 */
  public MessageData getMessage() {
    return message;
  }
}
