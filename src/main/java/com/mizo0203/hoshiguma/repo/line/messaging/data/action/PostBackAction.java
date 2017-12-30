package com.mizo0203.hoshiguma.repo.line.messaging.data.action;

public class PostBackAction extends Action {

  private final String data;
  private String text;
  private String label;

  public PostBackAction(String data) {
    super("postback");
    this.data = data;
  }

  public PostBackAction label(String label) {
    this.label = label;
    return this;
  }

  public PostBackAction text(String text) {
    this.text = text;
    return this;
  }
}
