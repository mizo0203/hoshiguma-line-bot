package com.mizo0203.hoshiguma.repo.line.messaging.data.webHook.event;

import com.mizo0203.hoshiguma.repo.line.messaging.data.PostBack;

/**
 * 参加イベント
 *
 * <p>アカウントがグループまたはトークルームに参加したことを示すイベントです。参加イベントには応答できます。
 *
 * <p>https://developers.line.me/ja/docs/messaging-api/reference/#anchor-511183a26c172a77583ef45edd4a5a0f596c5623
 */
public class PostBackEvent extends WebHookEventObject {

  /* package */ PostBackEvent() {
    // NOP
  }

  /** @return このイベントへの応答に使用するトークン */
  public String getReplyToken() {
    return replyToken;
  }

  /** @return ポストバックデータ */
  public String getPostBackData() {
    return postback.data;
  }

  /** @return 日時選択アクションを介してユーザーが選択した日時を含むJSONオブジェクト。 日時選択アクションによるポストバックアクションの場合にのみ返されます。 */
  public PostBack.Params getPostBackParams() {
    return postback.params;
  }
}
