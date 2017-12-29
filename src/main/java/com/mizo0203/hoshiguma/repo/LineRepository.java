package com.mizo0203.hoshiguma.repo;

import com.mizo0203.hoshiguma.repo.line.messaging.data.MessageObject;
import com.mizo0203.hoshiguma.repo.line.messaging.data.ReplyMessageData;
import com.mizo0203.hoshiguma.util.HttpPostUtil;
import com.mizo0203.hoshiguma.util.PaserUtil;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/* package */ class LineRepository {

  private static final String MESSAGING_API_REPLY_MESSAGE_URL_STR =
      "https://api.line.me/v2/bot/message/reply";

  @SuppressWarnings("EmptyMethod")
  public void destroy() {
    // NOP
  }

  /**
   * 応答メッセージを送る
   *
   * <p>ユーザー、グループ、またはトークルームからのイベントに対して応答メッセージを送信するAPIです。
   *
   * <p>イベントが発生するとwebhookを使って通知されます。応答できるイベントには応答トークンが発行されます。
   *
   * <p>応答トークンは一定の期間が経過すると無効になるため、メッセージを受信したらすぐに応答を返す必要があります。応答トークンは1回のみ使用できます。
   *
   * <p>https://developers.line.me/ja/docs/messaging-api/reference/#anchor-36ddabf319927434df30f0a74e21ad2cc69f0013
   *
   * @param channelAccessToken channel access token
   * @param replyToken Webhook で受信する応答トークン
   * @param messages 送信するメッセージ (最大件数：5)
   */
  public void replyMessage(String channelAccessToken, String replyToken, MessageObject[] messages) {
    ReplyMessageData replyMessageData = new ReplyMessageData();
    replyMessageData.replyToken = replyToken;
    replyMessageData.messages = messages;
    String body = PaserUtil.toJson(replyMessageData);
    try {
      URL url = new URL(MESSAGING_API_REPLY_MESSAGE_URL_STR);
      Map<String, String> reqProp = new HashMap<>();
      reqProp.put("Content-Type", "application/json");
      reqProp.put("Authorization", "Bearer " + channelAccessToken);
      HttpPostUtil.post(url, reqProp, body, null);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
