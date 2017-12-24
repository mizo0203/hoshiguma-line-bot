package com.mizo0203.hoshiguma;

import com.google.appengine.repackaged.com.google.api.client.util.Base64;
import com.mizo0203.hoshiguma.repo.Repository;
import com.mizo0203.hoshiguma.repo.State;
import com.mizo0203.hoshiguma.repo.line.messaging.data.EventData;
import com.mizo0203.hoshiguma.repo.line.messaging.data.MessageData;
import com.mizo0203.hoshiguma.repo.line.messaging.data.ReplyMessageData;
import com.mizo0203.hoshiguma.repo.line.messaging.data.WebhooksData;
import com.mizo0203.hoshiguma.util.HttpPostUtil;
import com.mizo0203.hoshiguma.util.PaserUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class HoshigumaLineBotServlet extends HttpServlet {

  private final static Logger LOG = Logger.getLogger(HoshigumaLineBotServlet.class.getName());

  private Repository mRepository;

  private void postLine(String body) {
    try {
      URL url = new URL("https://api.line.me/v2/bot/message/reply");
      String channelAccessToken = mRepository.getChannelAccessToken();
      Map<String, String> reqProp = new HashMap<>();
      reqProp.put("Content-Type", "application/json");
      reqProp.put("Authorization", "Bearer " + channelAccessToken);
      HttpPostUtil.post(url, reqProp, body, null);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/plain");
    resp.getWriter().println("Hello, world");
    LOG.info(req.toString());
    LOG.info("getParameterMap" + req.getParameterMap());
  }

  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    mRepository = new Repository();
    try {
      LOG.info("req: " + req.toString());
      LOG.info("getHeaderNames: " + req.getHeaderNames());
      LOG.info("getParameterMap: " + req.getParameterMap());
      String line = req.getReader().readLine();
      LOG.info("getReader: " + line);
      try {
        String expectSignature = req.getHeader("X-Line-Signature");
        verifySignature(mRepository.getChannelSecret(), line, expectSignature);
      } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
        LOG.log(Level.SEVERE, "", e);
        return;
      }
      WebhooksData webhooks = PaserUtil.parseWebhooksData(line);
      for (EventData event : webhooks.events) {
        LOG.info("replyToken: " + event.replyToken);
        switch (event.type) {
          case EventData.TYPE_JOIN:
            onLineJoin(event);
            break;
          case EventData.TYPE_MESSAGE:
            onLineMessage(event);
            break;
          default:
            break;
        }
      }
    } finally {
      mRepository.destroy();
    }
  }

  private void onLineJoin(EventData event) {
    ReplyMessageData replyMessageData = new ReplyMessageData();
    replyMessageData.replyToken = event.replyToken;
    replyMessageData.messages = new MessageData[1];
    replyMessageData.messages[0] = new MessageData();
    replyMessageData.messages[0].type = "text";
    mRepository.setEventName(event.source, null);
    replyMessageData.messages[0].text = "幹事は任せろ！\nイベント名は？";
    postLine(PaserUtil.toJson(replyMessageData));
  }

  private void onLineMessage(final EventData event) {
    LOG.info("text: " + event.message.text);
    if (event.message.text == null) {
      return;
    }
    ReplyMessageData replyMessageData = new ReplyMessageData();
    replyMessageData.replyToken = event.replyToken;
    replyMessageData.messages = new MessageData[1];
    replyMessageData.messages[0] = new MessageData();
    replyMessageData.messages[0].type = "text";
    State state = mRepository.getState(event.source);
    switch (state) {
      case NO_EVENT_NAME: {
        String event_name = event.message.text.split("\n")[0];
        mRepository.setEventName(event.source, event_name);
        replyMessageData.messages[0].text = event_name + "だな！\n了解！！\n日程はもう決まったのか？"; // TODO:
        // イベント名の修正機能
        // 日程調整機能の ON/OFF 切り替え
        postLine(PaserUtil.toJson(replyMessageData));
        break;
      }
      case HAS_EVENT_NAME: {
        //noinspection unused
        String event_name = mRepository.getEventName(event.source);
        replyMessageData.messages[0].text = event_name + "の件だな！\n早速、日程調整するぞ！！\n候補を教えてくれ！";
        postLine(PaserUtil.toJson(replyMessageData));
        break;
      }
      default:
        break;
    }
  }

  private void verifySignature(String channelSecret, String httpRequestBody, String expectSignature)
      throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, SignatureException {
    SecretKeySpec key = new SecretKeySpec(channelSecret.getBytes(), "HmacSHA256");
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(key);
    byte[] source = httpRequestBody.getBytes("UTF-8");
    String actualSignature = Base64.encodeBase64String(mac.doFinal(source));
    // Compare X-Line-Signature request header string and the signature
    if (actualSignature.equals(expectSignature)) {
      return;
    }
    throw new SignatureException();
  }

}
