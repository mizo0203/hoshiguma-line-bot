package com.mizo0203.hoshiguma.repo;

import com.mizo0203.hoshiguma.repo.line.messaging.data.MessageObject;
import com.mizo0203.hoshiguma.repo.line.messaging.data.RequestBody;
import com.mizo0203.hoshiguma.repo.line.messaging.data.SourceData;
import com.mizo0203.hoshiguma.repo.objectify.entity.KeyEntity;
import com.mizo0203.hoshiguma.repo.objectify.entity.LineTalkRoomConfig;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class Repository {

  private static final Logger LOG = Logger.getLogger(Repository.class.getName());

  private final OfyRepository mOfyRepository;

  private final LineRepository mLineRepository;

  public Repository() {
    mOfyRepository = new OfyRepository();
    mLineRepository = new LineRepository();
  }

  public void destroy() {
    mOfyRepository.destroy();
    mLineRepository.destroy();
  }

  public State getState(SourceData source) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(source);
    if (config == null) {
      return null;
    }
    return config.event_name != null ? State.HAS_EVENT_NAME : State.NO_EVENT_NAME;
  }

  public void setEventName(SourceData source, String event_name) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(source);
    if (config == null) {
      return;
    }
    config.event_name = event_name;
    mOfyRepository.saveLineTalkRoomConfig(config);
  }

  @SuppressWarnings("unused")
  public String getEventName(SourceData source) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(source);
    if (config == null) {
      return null;
    }
    return config.event_name;
  }

  public void addCandidateDate(SourceData source, Date candidateDate) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(source);
    if (config == null) {
      return;
    }
    if (config.candidate_dates == null) {
      config.candidate_dates = new Date[0];
    }
    List<Date> candidateDateList = new ArrayList<>(Arrays.asList(config.candidate_dates));
    candidateDateList.add(candidateDate);
    config.candidate_dates = candidateDateList.toArray(new Date[candidateDateList.size()]);
    mOfyRepository.saveLineTalkRoomConfig(config);
  }

  public Date[] getCandidateDates(SourceData source) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(source);
    if (config == null) {
      return null;
    }
    return config.candidate_dates;
  }

  public void clearCandidateDate(SourceData source) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(source);
    if (config == null) {
      return;
    }
    config.candidate_dates = null;
    mOfyRepository.saveLineTalkRoomConfig(config);
  }

  public void clearEvent(SourceData source) {
    LineTalkRoomConfig config = getOrCreateLineTalkRoomConfig(source);
    if (config == null) {
      return;
    }
    config.event_name = null;
    config.candidate_dates = null;
    mOfyRepository.saveLineTalkRoomConfig(config);
  }

  private LineTalkRoomConfig getOrCreateLineTalkRoomConfig(SourceData source) {
    String key = createLineTalkRoomConfigKey(source);
    if (key == null) {
      return null;
    }
    LineTalkRoomConfig config = mOfyRepository.loadLineTalkRoomConfig(key);
    if (config == null) {
      config = new LineTalkRoomConfig(key);
      mOfyRepository.saveLineTalkRoomConfig(config);
    }
    return config;
  }

  private String createLineTalkRoomConfigKey(SourceData source) {
    if (source.groupId != null) {
      return "groupId_" + source.groupId;
    } else if (source.userId != null) {
      return "userId_" + source.userId;
    } else {
      return null;
    }
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
   * リクエストボディを取得する
   *
   * @param req an {@link HttpServletRequest} object that contains the request the client has made
   *     of the servlet
   * @return リクエストボディは、webhookイベントオブジェクトの配列を含むJSONオブジェクトです。
   */
  public RequestBody getRequestBody(HttpServletRequest req) {
    return mLineRepository.getRequestBody(getChannelSecret(), req);
  }
}
