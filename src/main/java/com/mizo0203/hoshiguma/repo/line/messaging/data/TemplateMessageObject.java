package com.mizo0203.hoshiguma.repo.line.messaging.data;

import com.mizo0203.hoshiguma.repo.line.messaging.data.template.Template;

@SuppressWarnings({"unused", "WeakerAccess"})
public class TemplateMessageObject extends MessageObject {

  public final String altText;
  public final Template template;

  public TemplateMessageObject(String altText, Template template) {
    super("template");
    this.altText = altText;
    this.template = template;
  }
}
