package com.mizo0203.hoshiguma;

import com.mizo0203.hoshiguma.repo.Define;
import com.mizo0203.hoshiguma.repo.Repository;
import com.mizo0203.hoshiguma.repo.line.messaging.data.MessageObject;
import com.mizo0203.hoshiguma.repo.line.messaging.data.TemplateMessageObject;
import com.mizo0203.hoshiguma.repo.line.messaging.data.TextMessageObject;
import com.mizo0203.hoshiguma.repo.line.messaging.data.action.Action;
import com.mizo0203.hoshiguma.repo.line.messaging.data.action.PostBackAction;
import com.mizo0203.hoshiguma.repo.line.messaging.data.template.ButtonTemplate;
import com.mizo0203.hoshiguma.repo.line.messaging.data.template.CarouselTemplate;
import com.mizo0203.hoshiguma.repo.line.messaging.data.template.Template;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UseCase {
  private final DateFormat mDateFormat;
  private final Repository mRepository;

  public UseCase() {
    mRepository = new Repository();
    mDateFormat = new SimpleDateFormat(Define.DATE_FORMAT_PATTERN);
    mDateFormat.setTimeZone(Define.LINE_TIME_ZONE);
  }

  @Deprecated
  UseCase(Repository repository) {
    mRepository = repository;
    mDateFormat = new SimpleDateFormat(Define.DATE_FORMAT_PATTERN);
    mDateFormat.setTimeZone(Define.LINE_TIME_ZONE);
  }

  public void destroy() {
    mRepository.destroy();
  }

  /** 日程調整を送信する */
  public void replyCandidateDates(String source_id, String replyToken) {
    MessageObject[] messages = new MessageObject[3];
    messages[0] = new TextMessageObject("了解だ！\n皆は出欠を入力してくれ！");
    messages[1] = createCarouselTemplate(source_id);
    messages[2] = createInputCompletedMessageData();
    mRepository.replyMessage(replyToken, messages);
  }

  /** 日程調整のリマインダーを送信する */
  public void remindCandidateDates(String source_id) {
    MessageObject[] messages = new MessageObject[3];
    messages[0] = new TextMessageObject("リマインダーだ！\nもし、出欠入力がまだなら入力してくれ！");
    messages[1] = createCarouselTemplate(source_id);
    messages[2] = createInputCompletedMessageData();
    mRepository.pushMessage(source_id, messages);
  }

  /** 日程調整を締め切る */
  public void closeCandidateDates(String source_id) {
    MessageObject[] messages = new MessageObject[1];
    messages[0] = new TextMessageObject("締め切りだ！");
    mRepository.pushMessage(source_id, messages);
  }

  private MessageObject createCarouselTemplate(String source_id) {
    Date[] candidateDates = mRepository.getCandidateDates(source_id);
    CarouselTemplate.ColumnObject[] columns =
        new CarouselTemplate.ColumnObject[candidateDates.length];
    for (int i = 0; i < columns.length; i++) {
      Action[] actions = new Action[2];
      actions[0] = new PostBackAction("data5\n" + candidateDates[i].getTime()).label("出席");
      actions[1] = new PostBackAction("data6\n" + candidateDates[i].getTime()).label("欠席");
      columns[i] = new CarouselTemplate.ColumnObject(formatDate(candidateDates[i]), actions);
    }
    Template template = new CarouselTemplate(columns);
    return new TemplateMessageObject("テンプレートメッセージはiOS版およびAndroid版のLINE 6.7.0以降で対応しています。", template);
  }

  private MessageObject createInputCompletedMessageData() {
    Action[] actions = new Action[1];
    actions[0] = new PostBackAction("data4").label("入力完了");
    Template template = new ButtonTemplate("最後に「入力完了」を押してくれ", actions);
    return new TemplateMessageObject("テンプレートメッセージはiOS版およびAndroid版のLINE 6.7.0以降で対応しています。", template);
  }

  private String formatDate(Date date) {
    return mDateFormat.format(date);
  }
}
