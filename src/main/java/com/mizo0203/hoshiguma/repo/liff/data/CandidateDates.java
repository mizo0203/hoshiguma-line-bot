package com.mizo0203.hoshiguma.repo.liff.data;

import com.mizo0203.hoshiguma.domain.Translator;

import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class CandidateDates {
  private final CandidateDate[] candidateDates;

  public CandidateDates(final Date[] candidateDates) {
    this.candidateDates =
        new ArrayList<CandidateDate>() {
          {
            for (Date date : candidateDates) {
              add(new CandidateDate(new Translator().formatDate(date), date.getTime()));
            }
          }
        }.toArray(new CandidateDate[0]);
  }

  private static class CandidateDate {
    private final String dateStr;
    private final long dateNum;

    private CandidateDate(String dateStr, long dateNum) {
      this.dateStr = dateStr;
      this.dateNum = dateNum;
    }
  }
}
