package com.mizo0203.hoshiguma.repo;

import com.mizo0203.hoshiguma.repo.line.messaging.data.MessageObject;
import com.mizo0203.hoshiguma.repo.line.messaging.data.PushMessageData;
import com.mizo0203.hoshiguma.repo.line.messaging.data.ReplyMessageData;
import com.mizo0203.hoshiguma.repo.line.messaging.data.webHook.event.RequestBody;
import com.mizo0203.hoshiguma.util.HttpUtil;
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
  private static final String MESSAGING_API_PUSH_MESSAGE_URL_STR =
      "https://api.line.me/v2/bot/message/push";
  private static final String MESSAGING_API_IDS_MEMBERS_GROUP_URL_STR =
      "https://api.line.me/v2/bot/group/%s/members/ids";

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
      HttpUtil.post(url, reqProp, body, null);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * プッシュメッセージを送る
   *
   * <p>注：プッシュメッセージは一部のプランでのみご利用いただけます。詳しくは、LINE@サイトを参照してください。
   *
   * <p>ユーザー、グループ、またはトークルームに、任意のタイミングでプッシュメッセージを送信するAPIです。
   *
   * <p>https://developers.line.me/ja/docs/messaging-api/reference/#anchor-0c00cb0f42b970892f7c3382f92620dca5a110fc
   *
   * @param channelAccessToken channel access token
   * @param to 送信先のID。Webhookイベントオブジェクトで返される、userId、groupId、またはroomIdの値を使用します。LINEアプリに表示されるLINE
   *     IDは使用しないでください。
   * @param messages 送信するメッセージ 最大件数：5
   */
  public void pushMessage(String channelAccessToken, String to, MessageObject[] messages) {
    PushMessageData pushMessageData = new PushMessageData(to, messages);
    String body = PaserUtil.toJson(pushMessageData);
    try {
      URL url = new URL(MESSAGING_API_PUSH_MESSAGE_URL_STR);
      Map<String, String> reqProp = new HashMap<>();
      reqProp.put("Content-Type", "application/json");
      reqProp.put("Authorization", "Bearer " + channelAccessToken);
      HttpUtil.post(url, reqProp, body, null);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * グループメンバーのユーザーIDを取得する
   *
   * <p>注：この機能は認証済みLINE@アカウントまたは公式アカウントのみでご利用いただけます。詳しくは、LINE@サイトの「LINE@アカウントを作成しましょう」ページまたはLINE
   * Partnerサイトを参照してください。
   *
   * <p>ボットが参加しているグループのメンバーの、ユーザーIDを取得するAPIです。ボットを友だちとして追加していないユーザーや、ボットをブロックしているユーザーのユーザーIDも取得します。
   *
   * <p>https://developers.line.me/ja/docs/messaging-api/reference/#anchor-b3c29117f4c090d4c3aabc67516a0092e9e9a3b8
   *
   * @param channelAccessToken channel access token
   * @param groupId グループID。Webhookイベントオブジェクトのsourceオブジェクトで返されます。
   */
  public void idsMembersGroup(String channelAccessToken, String groupId) {
    try {
      URL url = new URL(String.format(MESSAGING_API_IDS_MEMBERS_GROUP_URL_STR, groupId));
      Map<String, String> reqProp = new HashMap<>();
      reqProp.put("Content-Type", "application/json");
      reqProp.put("Authorization", "Bearer " + channelAccessToken);
      HttpUtil.get(url, reqProp, null);
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
