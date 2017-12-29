package com.mizo0203.hoshiguma;

import com.mizo0203.hoshiguma.repo.Repository;
import com.mizo0203.hoshiguma.repo.State;
import com.mizo0203.hoshiguma.repo.line.messaging.data.*;
import com.mizo0203.hoshiguma.repo.line.messaging.data.action.Action;
import com.mizo0203.hoshiguma.repo.line.messaging.data.action.DateTimePickerAction;
import com.mizo0203.hoshiguma.repo.line.messaging.data.action.DateTimePickerAction.Mode;
import com.mizo0203.hoshiguma.repo.line.messaging.data.action.PostBackAction;
import com.mizo0203.hoshiguma.repo.line.messaging.data.template.ButtonTemplate;
import com.mizo0203.hoshiguma.repo.line.messaging.data.template.Template;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class HoshigumaLineBotServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(HoshigumaLineBotServlet.class.getName());

  private Repository mRepository;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/plain");
    resp.getWriter().println("Hello, world");
    LOG.info(req.toString());
    LOG.info("getParameterMap" + req.getParameterMap());
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) {
    onLineWebhook(req, resp);
  }

  /**
   * LINE Platform からのリクエストを受信
   *
   * <p>友だち追加やメッセージの送信のようなイベントがトリガーされると、webhook URL に HTTPS POST リクエストが送信されます。Webhook URL
   * はチャネルに対してコンソールで設定します。
   *
   * <p>リクエストはボットアプリのサーバーで受信および処理されます。
   *
   * @param req an {@link HttpServletRequest} object that contains the request the client has made
   *     of the servlet
   * @param resp an {@link HttpServletResponse} object that contains the response the servlet sends
   *     to the client
   */
  private void onLineWebhook(HttpServletRequest req, HttpServletResponse resp) {
    mRepository = new Repository();
    try {
      RequestBody requestBody = mRepository.getRequestBody(req);
      if (requestBody == null) {
        return;
      }
      for (WebHookEventObject event : requestBody.events) {
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
      // ボットアプリのサーバーに webhook から送信される HTTP POST リクエストには、ステータスコード 200 を返す必要があります。
      // https://developers.line.me/ja/docs/messaging-api/reference/#anchor-99cdae5b4b38ad4b86a137b508fd7b1b861e2366
      resp.setStatus(HttpServletResponse.SC_OK);
      mRepository.destroy();
    }
  }

  private void onLineJoin(WebHookEventObject event) {
    mRepository.clearEvent(event.source);
    MessageObject[] messages = new MessageObject[1];
    messages[0] = new TextMessageObject("幹事は任せろ！\nところで何の飲み会だっけ？w");
    mRepository.replyMessage(event.replyToken, messages);
  }

  private void onLineMessage(WebHookEventObject event) {
    LOG.info("text: " + event.message.text);
    if (event.message.text == null) {
      return;
    }
    State state = mRepository.getState(event.source);
    switch (state) {
      case NO_EVENT_NAME:
        {
          String event_name = event.message.text.split("\n")[0];
          mRepository.setEventName(event.source, event_name);
          MessageObject[] messages = new MessageObject[1];
          messages[0] = createMessageData("ああ、" + event_name + "だったな！\n早速、日程調整するぞ！！\n候補を教えてくれ！");
          // イベント名の修正機能
          // 日程調整機能の ON/OFF 切り替え
          mRepository.replyMessage(event.replyToken, messages);
          break;
        }
      case HAS_EVENT_NAME:
        {
          // NOP
          break;
        }
      default:
        break;
    }
  }

  private void onLinePostBack(WebHookEventObject event) {
    switch (event.postback.data) {
      case "data1":
        {
          SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
          String strDate = event.postback.params.datetime;
          try {
            Date date = fmt.parse(strDate);
            System.out.println(strDate + "をDateオブジェクトへ変換　→　" + date); // [2]
            mRepository.addCandidateDate(event.source, date);
            Date[] candidateDates = mRepository.getCandidateDates(event.source);
            SimpleDateFormat format2 = new SimpleDateFormat("MM/dd(E) HH:mm -");
            StringBuilder text = new StringBuilder("↓候補日時一覧だ！↓");
            for (Date candidateDate : candidateDates) {
              text.append("\n").append(format2.format(candidateDate));
            }
            MessageObject[] messages = new MessageObject[1];
            messages[0] = new TextMessageObject(text.toString());
            mRepository.replyMessage(event.replyToken, messages);
          } catch (ParseException e) {
            e.printStackTrace();
          }
          break;
        }
      case "data2":
        {
          MessageObject[] messages = new MessageObject[1];
          messages[0] = new TextMessageObject("了解だ！");
          mRepository.replyMessage(event.replyToken, messages);
          break;
        }
      case "data3":
        {
          mRepository.clearCandidateDate(event.source);
          MessageObject[] messages = new MessageObject[1];
          messages[0] = createMessageData("候補をクリアしたぞ！\n改めて候補を教えてくれ！");
          mRepository.replyMessage(event.replyToken, messages);
          break;
        }
    }
  }

  private MessageObject createMessageData(String text) {
    Action[] actions = new Action[3];
    actions[0] = new DateTimePickerAction("data1", Mode.DATE_TIME).label("候補日時を追加(最大10)");
    actions[1] = new PostBackAction("data2").label("候補日時の編集を完了");
    actions[2] = new PostBackAction("data3").label("候補日時をクリア");
    Template template = new ButtonTemplate(text, actions);
    return new TemplateMessageObject("テンプレートメッセージはiOS版およびAndroid版のLINE 6.7.0以降で対応しています。", template);
  }
}
