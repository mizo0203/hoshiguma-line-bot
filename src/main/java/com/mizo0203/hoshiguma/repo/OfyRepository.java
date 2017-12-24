package com.mizo0203.hoshiguma.repo;

import com.googlecode.objectify.ObjectifyService;
import com.mizo0203.hoshiguma.repo.objectify.entity.KeyEntity;
import com.mizo0203.hoshiguma.repo.objectify.entity.LineTalkRoomConfig;

/* package */ class OfyRepository {

  @SuppressWarnings("EmptyMethod")
  public void destroy() {
    // NOP
  }

  public LineTalkRoomConfig loadLineTalkRoomConfig(String key) {
    return ObjectifyService.ofy().load().type(LineTalkRoomConfig.class).id(key).now();
  }

  public void saveLineTalkRoomConfig(LineTalkRoomConfig entity) {
    ObjectifyService.ofy().save().entity(entity).now();
  }

  public KeyEntity loadKeyEntity(String key) {
    return ObjectifyService.ofy().load().type(KeyEntity.class).id(key).now();
  }

  public void saveKeyEntity(KeyEntity entity) {
    ObjectifyService.ofy().save().entity(entity).now();
  }
}
