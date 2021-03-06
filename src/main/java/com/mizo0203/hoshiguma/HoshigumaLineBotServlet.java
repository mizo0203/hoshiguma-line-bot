package com.mizo0203.hoshiguma;

import com.mizo0203.hoshiguma.domain.UseCase;
import com.mizo0203.hoshiguma.repo.Repository;
import com.mizo0203.hoshiguma.repo.State;
import com.mizo0203.hoshiguma.repo.line.messaging.data.MessageObject;
import com.mizo0203.hoshiguma.repo.line.messaging.data.TemplateMessageObject;
import com.mizo0203.hoshiguma.repo.line.messaging.data.TextMessageObject;
import com.mizo0203.hoshiguma.repo.line.messaging.data.action.Action;
import com.mizo0203.hoshiguma.repo.line.messaging.data.action.DateTimePickerAction;
import com.mizo0203.hoshiguma.repo.line.messaging.data.action.DateTimePickerAction.Mode;
import com.mizo0203.hoshiguma.repo.line.messaging.data.template.ButtonTemplate;
import com.mizo0203.hoshiguma.repo.line.messaging.data.template.Template;
import com.mizo0203.hoshiguma.repo.line.messaging.data.webHook.event.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

public class HoshigumaLineBotServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(HoshigumaLineBotServlet.class.getName());

  private UseCase mUseCase;
  @Deprecated private Repository mRepository;

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
    mUseCase = new UseCase(mRepository);
    try {
      RequestBody requestBody = mRepository.getRequestBody(req);
      if (requestBody == null) {
        return;
      }
      for (WebHookEventObject event : requestBody.concreteWebHookEventObject()) {
        if (event instanceof JoinEvent) {
          onLineJoin((JoinEvent) event);
        } else if (event instanceof MessageEvent) {
          onLineMessage((MessageEvent) event);
        } else if (event instanceof PostBackEvent) {
          onLinePostBack((PostBackEvent) event);
        }
      }
    } finally {
      // ボットアプリのサーバーに webhook から送信される HTTP POST リクエストには、ステータスコード 200 を返す必要があります。
      // https://developers.line.me/ja/docs/messaging-api/reference/#anchor-99cdae5b4b38ad4b86a137b508fd7b1b861e2366
      resp.setStatus(HttpServletResponse.SC_OK);
      mUseCase.close();
    }
  }

  private void onLineJoin(JoinEvent event) {
    LOG.info("replyToken: " + event.getReplyToken());
    mRepository.clearLineTalkRoomConfig(event.getSource().getSourceId());
    MessageObject[] messages = new MessageObject[1];
    messages[0] = new TextMessageObject("幹事は任せろ！\nところで何の飲み会だっけ？w");
    mRepository.replyMessage(event.getReplyToken(), messages);
  }

  private void onLineMessage(MessageEvent event) {
    LOG.info("text: " + event.getMessage().text);
    if (event.getMessage().text == null) {
      return;
    }
    State state = mRepository.getState(event.getSource());
    switch (state) {
      case NO_EVENT_NAME:
        {
          String event_name = event.getMessage().text.split("\n")[0];
          mRepository.setEventName(event.getSource(), event_name);
          // イベント名の修正機能
          // 日程調整機能の ON/OFF 切り替え
          mUseCase.replyRequestAdditionCandidateDateMessage(
              event.getReplyToken(), "ああ、" + event_name + "だったな！\n早速、日程調整するぞ！！\n候補を教えてくれ！");
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

  private void onLinePostBack(PostBackEvent event) {
    String[] postBackData = event.getPostBackData().split("\n");
    switch (postBackData[0]) {
      case "data1":
        {
          Date date = event.getPostBackParams().parseDatetime();
          mRepository.addCandidateDate(event.getSource(), date);
          String[] candidateDateStrings = mRepository.getCandidateDateStrings(event.getSource());
          StringBuilder text = new StringBuilder("↓候補日時一覧だ！↓");
          for (String candidateDateString : candidateDateStrings) {
            text.append("\n").append(candidateDateString);
          }
          MessageObject[] messages = new MessageObject[1];
          messages[0] = new TextMessageObject(text.toString());
          mRepository.replyMessage(event.getReplyToken(), messages);
          break;
        }
      case "data2":
        {
          MessageObject[] messages = new MessageObject[1];
          messages[0] = createCloseMessageData();
          mRepository.replyMessage(event.getReplyToken(), messages);
          break;
        }
      case "data22":
        {
          Date date = event.getPostBackParams().parseDatetime();
          mRepository.enqueueReminderTask(
              event.getSource(), date.getTime() - 10000); // TODO: 10 sec
          mRepository.enqueueCloseTask(event.getSource(), date.getTime());
          mUseCase.replyCandidateDates(event.getReplyToken());
          break;
        }
      case "data3":
        {
          mUseCase.clearCandidateDate(event.getSource().getSourceId());
          mUseCase.replyRequestAdditionCandidateDateMessage(
              event.getReplyToken(), "候補をクリアしたぞ！\n改めて候補を教えてくれ！");
          break;
        }
      case "data4":
        { // TODO
          MessageObject[] messages = new MessageObject[1];
          StringBuilder text = new StringBuilder("了解だ！");
          for (String candidate_date :
              mRepository.getMemberCandidateDateStrings(event.getSource())) {
            text.append("\n").append(candidate_date);
          }
          messages[0] = new TextMessageObject(text.toString());
          mRepository.replyMessage(event.getReplyToken(), messages);
          break;
        }
      case "data5":
        { // TODO
          mRepository.addMemberCandidateDate(
              event.getSource(), new Date(Long.parseLong(postBackData[1])));
          break;
        }
      case "data6":
        { // TODO
          mRepository.removeMemberCandidateDate(
              event.getSource(), new Date(Long.parseLong(postBackData[1])));
          break;
        }
      default:
        break;
    }
  }

  private MessageObject createCloseMessageData() {
    Action[] actions = new Action[1];
    actions[0] = new DateTimePickerAction("data22", Mode.DATE_TIME).label("締め切り時刻をセット");
    Template template = new ButtonTemplate("締め切り時刻をセットしてくれ！", actions);
    return new TemplateMessageObject("テンプレートメッセージはiOS版およびAndroid版のLINE 6.7.0以降で対応しています。", template);
  }
}
