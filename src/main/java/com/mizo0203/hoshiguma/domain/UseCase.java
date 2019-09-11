package com.mizo0203.hoshiguma.domain;

import com.mizo0203.hoshiguma.ContentServlet;
import com.mizo0203.hoshiguma.repo.Repository;
import com.mizo0203.hoshiguma.repo.liff.data.CandidateDates;
import com.mizo0203.hoshiguma.repo.liff.data.Member;
import com.mizo0203.hoshiguma.repo.line.messaging.data.MessageObject;
import com.mizo0203.hoshiguma.repo.line.messaging.data.TemplateMessageObject;
import com.mizo0203.hoshiguma.repo.line.messaging.data.TextMessageObject;
import com.mizo0203.hoshiguma.repo.line.messaging.data.action.Action;
import com.mizo0203.hoshiguma.repo.line.messaging.data.action.DateTimePickerAction;
import com.mizo0203.hoshiguma.repo.line.messaging.data.action.PostBackAction;
import com.mizo0203.hoshiguma.repo.line.messaging.data.action.UriAction;
import com.mizo0203.hoshiguma.repo.line.messaging.data.template.ButtonTemplate;
import com.mizo0203.hoshiguma.repo.line.messaging.data.template.Template;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class UseCase implements AutoCloseable {
  private static final Logger LOG = Logger.getLogger(ContentServlet.class.getName());
  private final Repository mRepository;
  private final Translator mTranslator;

  public UseCase() {
    mRepository = new Repository();
    mTranslator = new Translator();
  }

  @Deprecated
  public UseCase(Repository repository) {
    mRepository = repository;
    mTranslator = new Translator();
  }

  @Override
  public void close() {
    mRepository.destroy();
  }

  /** 日程調整を送信する */
  public void replyCandidateDates(String replyToken) {
    mRepository.replyMessage(
        replyToken,
        new MessageObject[] {
          createInputCompletedMessageData("了解だ！\n皆は出欠を入力してくれ！"),
        });
  }

  /** 日程調整のリマインダーを送信する */
  public void remindCandidateDates(String source_id) {
    mRepository.pushMessage(
        source_id,
        new MessageObject[] {
          createInputCompletedMessageData("リマインダーだ！\nもし、出欠入力がまだなら入力してくれ！"),
        });
  }

  /** 日程調整を締め切る */
  public void closeCandidateDates(String source_id) {
    MessageObject[] messages = new MessageObject[1];
    messages[0] = new TextMessageObject("締め切りだ！");
    mRepository.pushMessage(source_id, messages);
  }

  private MessageObject createInputCompletedMessageData(String text) {
    Template template =
        new ButtonTemplate(
            text,
            new Action[] {
              new UriAction("出欠入力フォームを起動", "line://app/1553006014-OJKlWjqN"),
            });
    return new TemplateMessageObject("新しいメッセージを確認してくれ！", template);
  }

  public void replyRequestAdditionCandidateDateMessage(String replyToken, String text) {
    mRepository.replyMessage(
        replyToken,
        new MessageObject[] {
          createRequestAdditionCandidateDateMessageObject(text),
        });
  }

  private MessageObject createRequestAdditionCandidateDateMessageObject(String text) {
    Action[] actions = new Action[3];
    actions[0] =
        new DateTimePickerAction("data1", DateTimePickerAction.Mode.DATE_TIME)
            .label("候補日時を追加(最大10)");
    actions[1] = new PostBackAction("data2").label("候補日時の編集を完了");
    actions[2] = new PostBackAction("data3").label("候補日時をクリア");
    Template template = new ButtonTemplate(text, actions);
    return new TemplateMessageObject("新しいメッセージを確認してくれ！", template);
  }

  public void clearCandidateDate(String sourceId) {
    mRepository.clearCandidateDate(sourceId);
  }

  public CandidateDates getCandidateDates(String source_id) {
    //        mRepository.addCandidateDate(source_id, new Date());
    return new CandidateDates(mRepository.getCandidateDates(source_id));
  }

  public void submitAnswer(
      String groupId, String userId, String displayName, Map<Long, Member.Answer> answer) {
    List<Date> candidateDatesList = new ArrayList<>();
    for (long dateNum : answer.keySet()) {
      if (answer.get(dateNum).equals(Member.Answer.attendance)
          || answer.get(dateNum).equals(Member.Answer.late)) {
        candidateDatesList.add(new Date(dateNum));
      }
    }
    mRepository.setMemberCandidateDate(groupId, userId, candidateDatesList.toArray(new Date[0]));
    StringBuilder text = new StringBuilder(displayName + " さんが入力しました！");
    for (String candidate_date : mRepository.getMemberCandidateDateStrings(groupId, userId)) {
      LOG.info("candidate_date: " + candidate_date);
      text.append("\n").append(candidate_date);
    }
    MessageObject[] messages =
        new MessageObject[] {
          new TextMessageObject(text.toString()),
        };
    mRepository.pushMessage(groupId, messages);
  }
}
