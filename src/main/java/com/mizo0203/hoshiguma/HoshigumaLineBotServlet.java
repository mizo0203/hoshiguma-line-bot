package com.mizo0203.hoshiguma;

import com.google.appengine.repackaged.com.google.api.client.util.Base64;
import com.mizo0203.hoshiguma.repo.Repository;
import com.mizo0203.hoshiguma.repo.State;
import com.mizo0203.hoshiguma.repo.line.messaging.data.MessageObject;
import com.mizo0203.hoshiguma.repo.line.messaging.data.ReplyMessageData;
import com.mizo0203.hoshiguma.repo.line.messaging.data.RequestBody;
import com.mizo0203.hoshiguma.repo.line.messaging.data.TemplateMessageObject;
import com.mizo0203.hoshiguma.repo.line.messaging.data.TextMessageObject;
import com.mizo0203.hoshiguma.repo.line.messaging.data.WebHookEventObject;
import com.mizo0203.hoshiguma.repo.line.messaging.data.action.Action;
import com.mizo0203.hoshiguma.repo.line.messaging.data.action.DateTimePickerAction;
import com.mizo0203.hoshiguma.repo.line.messaging.data.action.DateTimePickerAction.Mode;
import com.mizo0203.hoshiguma.repo.line.messaging.data.action.PostBackAction;
import com.mizo0203.hoshiguma.repo.line.messaging.data.template.ButtonTemplate;
import com.mizo0203.hoshiguma.repo.line.messaging.data.template.Template;
import com.mizo0203.hoshiguma.util.HttpPostUtil;
import com.mizo0203.hoshiguma.util.PaserUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
      RequestBody webhooks = PaserUtil.parseWebhooksData(line);
      for (WebHookEventObject event : webhooks.events) {
        LOG.info("replyToken: " + event.replyToken);
        switch (event.type) {
          case WebHookEventObject.TYPE_JOIN:
            onLineJoin(event);
            break;
          case WebHookEventObject.TYPE_MESSAGE:
            onLineMessage(event);
            break;
          case WebHookEventObject.TYPE_POST_BACK:
            onLinePostBack(event);
            break;
          default:
            break;
        }
      }
    } finally {
      mRepository.destroy();
    }
  }

  private void onLineJoin(WebHookEventObject event) {
    mRepository.clearEvent(event.source);
    ReplyMessageData replyMessageData = new ReplyMessageData();
    replyMessageData.replyToken = event.replyToken;
    replyMessageData.messages = new MessageObject[1];
    replyMessageData.messages[0] = new TextMessageObject("幹事は任せろ！\nところで何の飲み会だっけ？w");
    postLine(PaserUtil.toJson(replyMessageData));
  }

  private void onLineMessage(final WebHookEventObject event) {
    LOG.info("text: " + event.message.text);
    if (event.message.text == null) {
      return;
    }
    ReplyMessageData replyMessageData = new ReplyMessageData();
    replyMessageData.replyToken = event.replyToken;
    State state = mRepository.getState(event.source);
    switch (state) {
      case NO_EVENT_NAME: {
        String event_name = event.message.text.split("\n")[0];
        mRepository.setEventName(event.source, event_name);
        replyMessageData.messages = new MessageObject[1];
        replyMessageData.messages[0] = createMessageData(
            "ああ、" + event_name + "だったな！\n早速、日程調整するぞ！！\n候補を教えてくれ！");
        // イベント名の修正機能
        // 日程調整機能の ON/OFF 切り替え
        postLine(PaserUtil.toJson(replyMessageData));
        break;
      }
      case HAS_EVENT_NAME: {
        // NOP
        break;
      }
      default:
        break;
    }
  }

  private void onLinePostBack(WebHookEventObject event) {
    ReplyMessageData replyMessageData = new ReplyMessageData();
    replyMessageData.replyToken = event.replyToken;
    switch (event.postback.data) {
      case "data1": {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        String strDate = event.postback.params.datetime;
        try {
          Date date = fmt.parse(strDate);
          System.out.println(strDate + "をDateオブジェクトへ変換　→　" + date);//[2]
          mRepository.addCandidateDate(event.source, date);
          Date[] candidateDates = mRepository.getCandidateDates(event.source);
          SimpleDateFormat format2 = new SimpleDateFormat("MM/dd(E) HH:mm -");
          StringBuilder text = new StringBuilder("↓候補日時一覧だ！↓");
          for (Date candidateDate : candidateDates) {
            text.append("\n").append(format2.format(candidateDate));
          }
          replyMessageData.messages = new MessageObject[1];
          replyMessageData.messages[0] = new TextMessageObject(text.toString());
          postLine(PaserUtil.toJson(replyMessageData));
        } catch (ParseException e) {
          e.printStackTrace();
        }
        break;
      }
      case "data2": {
        replyMessageData.messages = new MessageObject[1];
        replyMessageData.messages[0] = new TextMessageObject("了解だ！");
        postLine(PaserUtil.toJson(replyMessageData));
        break;
      }
      case "data3": {
        mRepository.clearCandidateDate(event.source);
        replyMessageData.messages = new MessageObject[1];
        replyMessageData.messages[0] = createMessageData("候補をクリアしたぞ！\n改めて候補を教えてくれ！");
        postLine(PaserUtil.toJson(replyMessageData));
        break;
      }
    }
  }

  private MessageObject createMessageData(String text) {
    Action[] actions = new Action[3];
    actions[0] = new DateTimePickerAction("data1", Mode.DATE_TIME).label("候補日時を追加(最大10)");
    actions[1] = new PostBackAction("data2").label("候補日時の追加を完了");
    actions[2] = new PostBackAction("data3").label("候補日時の追加をクリア");
    Template template = new ButtonTemplate(text,
        actions);
    return new TemplateMessageObject(
        "テンプレートメッセージはiOS版およびAndroid版のLINE 6.7.0以降で対応しています。", template);
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
