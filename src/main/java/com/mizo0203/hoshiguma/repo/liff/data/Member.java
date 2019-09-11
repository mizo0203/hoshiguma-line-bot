package com.mizo0203.hoshiguma.repo.liff.data;

import com.google.gson.annotations.SerializedName;

public class Member {
  public final String groupId;
  public final String displayName;
  public final Answer c0;
  public final Answer c1;
  public final Answer c2;
  public final Answer c3;
  public final Answer c4;
  public final Answer c5;
  public final Answer c6;
  public final Answer c7;
  public final Answer c8;
  public final Answer c9;
  public final Answer c10;
  public final long d0;
  public final long d1;
  public final long d2;
  public final long d3;
  public final long d4;
  public final long d5;
  public final long d6;
  public final long d7;
  public final long d8;
  public final long d9;
  public final long d10;

  public Member(
      String groupId,
      String displayName,
      Answer c0,
      Answer c1,
      Answer c2,
      Answer c3,
      Answer c4,
      Answer c5,
      Answer c6,
      Answer c7,
      Answer c8,
      Answer c9,
      Answer c10,
      long d0,
      long d1,
      long d2,
      long d3,
      long d4,
      long d5,
      long d6,
      long d7,
      long d8,
      long d9,
      long d10) {
    this.groupId = groupId;
    this.displayName = displayName;
    this.c0 = c0;
    this.c1 = c1;
    this.c2 = c2;
    this.c3 = c3;
    this.c4 = c4;
    this.c5 = c5;
    this.c6 = c6;
    this.c7 = c7;
    this.c8 = c8;
    this.c9 = c9;
    this.c10 = c10;
    this.d0 = d0;
    this.d1 = d1;
    this.d2 = d2;
    this.d3 = d3;
    this.d4 = d4;
    this.d5 = d5;
    this.d6 = d6;
    this.d7 = d7;
    this.d8 = d8;
    this.d9 = d9;
    this.d10 = d10;
  }

  public enum Answer {
    @SerializedName("attendance")
    attendance,
    @SerializedName("late")
    late,
    @SerializedName("absent")
    absent,
    @SerializedName("checking")
    checking,
    ;

    public static Answer fromString(String name) {
      if (name != null) {
        switch (name) {
          case "attendance":
            return Answer.attendance;
          case "late":
            return Answer.late;
          case "absent":
            return Answer.absent;
          case "checking":
            return Answer.checking;
        }
      }
      return null;
    }
  }
}
