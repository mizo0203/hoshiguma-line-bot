package com.mizo0203.hoshiguma.repo;

import com.mizo0203.hoshiguma.domain.Translator;
import com.mizo0203.hoshiguma.repo.line.messaging.data.MessageObject;
import com.mizo0203.hoshiguma.repo.line.messaging.data.SourceData;
import com.mizo0203.hoshiguma.repo.line.messaging.data.webHook.event.RequestBody;
import com.mizo0203.hoshiguma.repo.objectify.entity.KeyEntity;
import com.mizo0203.hoshiguma.repo.objectify.entity.LineTalkRoomConfig;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

public class Repository {

  private static final Logger LOG = Logger.getLogger(Repository.class.getName());
  private final OfyRepository mOfyRepository;
  private final LineRepository mLineRepository;
  private final PushQueueRepository mPushQueueRepository;
  private final Translator mTranslator;

  public Repository() {
    mOfyRepository = new OfyRepository();
    mLineRepository = new LineRepository();
    mPushQueueRepository = new PushQueueRepository();
    mTranslator = new Translator();
  }

  public void destroy() {
    mOfyRepository.destroy();
    mLineRepository.destroy();
    mPushQueueRepository.destroy();
  }

  public State getState(SourceData source) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(source.getSourceId());
    return config.event_name != null ? State.HAS_EVENT_NAME : State.NO_EVENT_NAME;
  }

  public void setEventName(SourceData source, String event_name) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(source.getSourceId());
    config.event_name = event_name;
    mOfyRepository.saveLineTalkRoomConfig(config);
  }

  @SuppressWarnings("unused")
  public String getEventName(SourceData source) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(source.getSourceId());
    return config.event_name;
  }

  public void addCandidateDate(SourceData source, Date candidateDate) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(source.getSourceId());
    config.candidate_dates.add(candidateDate);
    mOfyRepository.saveLineTalkRoomConfig(config);
  }

  public void addCandidateDate(String source_id, Date candidateDate) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(source_id);
    config.candidate_dates.add(candidateDate);
    mOfyRepository.saveLineTalkRoomConfig(config);
  }

  public Date[] getCandidateDates(String source_id) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(source_id);
    return config.candidate_dates.toArray(new Date[0]);
  }

  public String[] getCandidateDateStrings(SourceData source) {
    Date[] candidateDates = getCandidateDates(source.getSourceId());
    if (candidateDates == null) {
      return null;
    }
    String[] ret = new String[candidateDates.length];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = mTranslator.formatDate(candidateDates[i]);
    }
    return ret;
  }

  public void clearCandidateDate(String sourceId) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(sourceId);
    config.candidate_dates = null;
    mOfyRepository.saveLineTalkRoomConfig(config);
  }

  public void clearLineTalkRoomConfig(String sourceId) {
    mOfyRepository.deleteLineTalkRoomConfig(sourceId);
  }

  private LineTalkRoomConfig getOrCreateLineTalkRoomConfig(String source_id) {
    LineTalkRoomConfig config = mOfyRepository.loadLineTalkRoomConfig(source_id);
    if (config == null) {
      config = new LineTalkRoomConfig(source_id);
    }
    return config;
  }

  private String getChannelAccessToken() {
    KeyEntity keyEntity = mOfyRepository.loadKeyEntity("ChannelAccessToken");

    if (keyEntity == null) {
      keyEntity = new KeyEntity();
      keyEntity.key = "ChannelAccessToken";
      keyEntity.value = "";
      mOfyRepository.saveKeyEntity(keyEntity);
    }

    if (keyEntity.value.isEmpty()) {
      LOG.severe("ChannelAccessToken isEmpty");
    }

    return keyEntity.value;
  }

  private String getChannelSecret() {
    KeyEntity keyEntity = mOfyRepository.loadKeyEntity("ChannelSecret");

    if (keyEntity == null) {
      keyEntity = new KeyEntity();
      keyEntity.key = "ChannelSecret";
      keyEntity.value = "";
      mOfyRepository.saveKeyEntity(keyEntity);
    }

    if (keyEntity.value.isEmpty()) {
      LOG.severe("ChannelSecret isEmpty");
    }

    return keyEntity.value;
  }

  /**
   * 応答メッセージを送る
   *
   * @param replyToken Webhook で受信する応答トークン
   * @param messages 送信するメッセージ (最大件数：5)
   */
  public void replyMessage(String replyToken, MessageObject[] messages) {
    String channelAccessToken = getChannelAccessToken();
    mLineRepository.replyMessage(channelAccessToken, replyToken, messages);
  }

  /**
   * プッシュメッセージを送る
   *
   * @param to 送信先のID。Webhookイベントオブジェクトで返される、userId、groupId、またはroomIdの値を使用します。LINEアプリに表示されるLINE
   *     IDは使用しないでください。
   * @param messages 送信するメッセージ 最大件数：5
   */
  public void pushMessage(String to, MessageObject[] messages) {
    String channelAccessToken = getChannelAccessToken();
    mLineRepository.pushMessage(channelAccessToken, to, messages);
  }

  /**
   * グループメンバーのユーザーIDを取得する
   *
   * @param groupId グループID。Webhookイベントオブジェクトのsourceオブジェクトで返されます。
   */
  @SuppressWarnings("unused")
  public void idsMembersGroup(String groupId) {
    String channelAccessToken = getChannelAccessToken();
    mLineRepository.idsMembersGroup(channelAccessToken, groupId);
  }

  /**
   * リクエストボディを取得する
   *
   * @param req an {@link HttpServletRequest} object that contains the request the client has made
   *     of the servlet
   * @return リクエストボディは、webhookイベントオブジェクトの配列を含むJSONオブジェクトです。
   */
  public RequestBody getRequestBody(HttpServletRequest req) {
    return mLineRepository.getRequestBody(getChannelSecret(), req);
  }

  public void addMemberCandidateDate(SourceData source, Date candidateDate) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(source.getSourceId());
    SortedSet<Date> member_candidate_dates =
        config.member_candidate_dates.computeIfAbsent(source.getUserId(), k -> new TreeSet<>());
    member_candidate_dates.add(candidateDate);
    mOfyRepository.saveLineTalkRoomConfig(config);
  }

  public void removeMemberCandidateDate(SourceData source, Date candidateDate) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(source.getSourceId());
    SortedSet<Date> member_candidate_dates =
        config.member_candidate_dates.computeIfAbsent(source.getUserId(), k -> new TreeSet<>());
    member_candidate_dates.remove(candidateDate);
    mOfyRepository.saveLineTalkRoomConfig(config);
  }

  public void setMemberCandidateDate(String source_id, String userId, Date[] candidateDates) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(source_id);
    SortedSet<Date> member_candidate_dates =
        config.member_candidate_dates.computeIfAbsent(userId, k -> new TreeSet<>());
    member_candidate_dates.clear();
    member_candidate_dates.addAll(Arrays.asList(candidateDates));
    mOfyRepository.saveLineTalkRoomConfig(config);
  }

  public String[] getMemberCandidateDateStrings(SourceData source) {
    return getMemberCandidateDateStrings(source.getSourceId(), source.getUserId());
  }

  public String[] getMemberCandidateDateStrings(String source_id, String userId) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(source_id);
    SortedSet<Date> member_candidate_dates =
        config.member_candidate_dates.computeIfAbsent(userId, k -> new TreeSet<>());
    Date[] candidateDates = member_candidate_dates.toArray(new Date[0]);
    String[] ret = new String[candidateDates.length];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = mTranslator.formatDate(candidateDates[i]);
    }
    return ret;
  }

  public void enqueueReminderTask(SourceData source, long etaMillis) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(source.getSourceId());
    mPushQueueRepository.enqueueReminderTask(config.getSourceId(), etaMillis);
  }

  public void enqueueCloseTask(SourceData source, long etaMillis) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(source.getSourceId());
    mPushQueueRepository.enqueueCloseTask(config.getSourceId(), etaMillis);
  }
}
