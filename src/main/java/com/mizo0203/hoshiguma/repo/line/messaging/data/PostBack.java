package com.mizo0203.hoshiguma.repo.line.messaging.data;

public class PostBack {

  public String data;
  public Params params;

  public static class Params {

    public String date;
    public String time;
    public String datetime;
  }
}
