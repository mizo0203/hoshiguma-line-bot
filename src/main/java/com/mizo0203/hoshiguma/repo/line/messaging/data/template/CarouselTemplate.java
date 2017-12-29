package com.mizo0203.hoshiguma.repo.line.messaging.data.template;

import com.mizo0203.hoshiguma.repo.line.messaging.data.action.Action;

public class CarouselTemplate extends Template {

  private final ColumnObject[] columns;
  private String imageAspectRatio;
  private String imageSize;

  /** @param columns カラムの配列(最大カラム数：10) */
  public CarouselTemplate(ColumnObject[] columns) {
    super("carousel");
    this.columns = columns;
  }

  /** カルーセルのカラムオブジェクト */
  public static class ColumnObject {
    private final String text;
    private final Action[] actions;
    private String thumbnailImageUrl;
    private String imageBackgroundColor;
    private String title;

    /**
     * @param text メッセージテキスト 画像もタイトルも指定しない場合の最大文字数：120 画像またはタイトルを指定する場合の最大文字数：60
     * @param actions タップされたときのアクション 最大件数：3
     */
    public ColumnObject(String text, Action[] actions) {
      this.text = text;
      this.actions = actions;
    }
  }
}
