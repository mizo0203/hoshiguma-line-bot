package com.mizo0203.hoshiguma.repo.line.messaging.data.action;

@SuppressWarnings({"unused", "WeakerAccess"})
public class PostBackAction extends Action {

  public String label;
  public String data;
  public String text;

  public PostBackAction(String data) {
    super("postback");
    this.data = data;
  }

  public PostBackAction label(String label) {
    this.label = label;
    return this;
  }

}
