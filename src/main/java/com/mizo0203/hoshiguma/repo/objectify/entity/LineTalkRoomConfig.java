package com.mizo0203.hoshiguma.repo.objectify.entity;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.mizo0203.hoshiguma.repo.objectify.OfyHelper;
import java.util.Date;

/**
 * The @Entity tells Objectify about our entity. We also register it in {@link OfyHelper} Our
 * primary key @Id is set automatically by the Google Datastore for us. <p> We add a @Parent to tell
 * the object about its ancestor. We are doing this to support many guestbooks. Objectify, unlike
 * the AppEngine library requires that you specify the fields you want to index using @Index. Only
 * indexing the fields you need can lead to substantial gains in performance -- though if not
 * indexing your data from the start will require indexing it later. <p> NOTE - all the properties
 * are PUBLIC so that can keep the code simple.
 **/
@SuppressWarnings({"unused", "WeakerAccess"})
@Entity
public class LineTalkRoomConfig {

  @Id
  public String key;

  public String event_name;
  public Date[] candidate_dates;

  public LineTalkRoomConfig() {
    // Panelist must have a no-arg constructor
  }

  /**
   * A convenience constructor
   **/
  public LineTalkRoomConfig(String key) {
    this();
    this.key = key;
  }

}
