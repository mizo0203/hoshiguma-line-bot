package com.mizo0203.hoshiguma.repo.line.messaging.data;

import com.mizo0203.hoshiguma.repo.Define;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostBack {

  public String data;
  public Params params;

  public static class Params {
    public String date;
    public String time;
    public String datetime;

    public Date parseDatetime() {
      try {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        fmt.setTimeZone(Define.LINE_TIME_ZONE);
        return fmt.parse(datetime);
      } catch (ParseException e) {
        e.printStackTrace();
        return null;
      }
    }
  }
}
