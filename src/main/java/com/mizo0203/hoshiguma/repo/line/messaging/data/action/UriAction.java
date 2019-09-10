package com.mizo0203.hoshiguma.repo.line.messaging.data.action;

public class UriAction extends Action {

  private final String label;
  private final String uri;

  public UriAction(String label, String uri) {
    super("uri");
    this.label = label;
    this.uri = uri;
  }
}
