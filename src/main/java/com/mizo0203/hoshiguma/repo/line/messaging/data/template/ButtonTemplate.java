package com.mizo0203.hoshiguma.repo.line.messaging.data.template;

import com.mizo0203.hoshiguma.repo.line.messaging.data.action.Action;

public class ButtonTemplate extends Template {

  public final String text;
  public final Action[] actions;
  public String thumbnailImageUrl;
  public String imageAspectRatio;
  public String imageSize;
  public String imageBackgroundColor;
  public String title;

  public ButtonTemplate(String text, Action[] actions) {
    super("buttons");
    this.text = text;
    this.actions = actions;
  }
}
