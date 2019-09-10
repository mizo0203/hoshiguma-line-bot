package com.mizo0203.hoshiguma.domain;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Translator {

  private final DateFormat mDateFormat;

  public Translator() {
    mDateFormat = new SimpleDateFormat(Define.DATE_FORMAT_PATTERN);
    mDateFormat.setTimeZone(Define.LINE_TIME_ZONE);
  }

  public String formatDate(Date date) {
    return mDateFormat
        .format(date)
        .replace("Sun", "日")
        .replace("Mon", "月")
        .replace("Tue", "火")
        .replace("Wed", "水")
        .replace("Thu", "木")
        .replace("Fri", "金")
        .replace("Sat", "土");
  }
}
