package com.mizo0203.hoshiguma.repo;

import com.mizo0203.hoshiguma.repo.line.messaging.data.MessageObject;
import com.mizo0203.hoshiguma.repo.line.messaging.data.ReplyMessageData;
import com.mizo0203.hoshiguma.repo.line.messaging.data.RequestBody;
import com.mizo0203.hoshiguma.util.HttpPostUtil;
import com.mizo0203.hoshiguma.util.PaserUtil;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/* package */ class LineRepository {

  private static final Logger LOG = Logger.getLogger(LineRepository.class.getName());

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

  /**
   * リクエストボディを取得する
   *
   * @param req an {@link HttpServletRequest} object that contains the request the client has made
   *     of the servlet
   * @return リクエストボディは、webhookイベントオブジェクトの配列を含むJSONオブジェクトです。
   */
  public RequestBody getRequestBody(String channelSecret, HttpServletRequest req) {
    try {
      LOG.info("req: " + req.toString());
      LOG.info("getHeaderNames: " + req.getHeaderNames());
      LOG.info("getParameterMap: " + req.getParameterMap());
      String httpRequestBody = req.getReader().readLine();
      LOG.info("httpRequestBody: " + httpRequestBody);
      String expectSignature = req.getHeader("X-Line-Signature");
      if (verifySignature(channelSecret, httpRequestBody, expectSignature)) {
        return PaserUtil.parseWebhooksData(httpRequestBody);
      }
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "", e);
    }
    return null;
  }

  /**
   * 署名を検証する
   *
   * @param channelSecret Channel secret string
   * @param httpRequestBody Request body string
   * @param expectSignature X-Line-Signature request header string
   */
  private boolean verifySignature(
      String channelSecret, String httpRequestBody, String expectSignature) {
    try {
      SecretKeySpec key = new SecretKeySpec(channelSecret.getBytes(), "HmacSHA256");
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(key);
      byte[] source = httpRequestBody.getBytes("UTF-8");
      String actualSignature = Base64.encodeBase64String(mac.doFinal(source));
      // Compare X-Line-Signature request header string and the signature
      if (actualSignature.equals(expectSignature)) {
        return true;
      }
    } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
      LOG.log(Level.SEVERE, "", e);
    }
    return false;
  }
}
