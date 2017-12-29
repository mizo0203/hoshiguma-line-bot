package com.mizo0203.hoshiguma.repo.line.messaging.data;

/**
 * メッセージオブジェクト
 *
 * <p>送信するメッセージの内容を表すJSONオブジェクトです。
 *
 * <p>テキスト 画像 動画 音声 位置情報 スタンプ イメージマップ テンプレート
 *
 * <p>https://developers.line.me/ja/docs/messaging-api/reference/#message-objects
 */
public abstract class MessageObject {

  @SuppressWarnings({"FieldCanBeLocal", "unused"})
  private final String type;

  /* package */ MessageObject(String type) {
    this.type = type;
  }
}
