package com.mizo0203.hoshiguma.repo.line.messaging.data.action;

public class DateTimePickerAction extends Action {

  public String label;
  public String data;
  public String mode;
  public String initial;
  public String max;
  public String min;

  public DateTimePickerAction(String data, Mode mode) {
    super("datetimepicker");
    this.data = data;
    this.mode = mode.toString();
  }

  public DateTimePickerAction label(String label) {
    this.label = label;
    return this;
  }

  public enum Mode {
    DATE,
    TIME,
    DATE_TIME,
    ;

    @Override
    public String toString() {
      switch (this) {
        case DATE:
          return "date";
        case TIME:
          return "time";
        case DATE_TIME:
          return "datetime";
        default:
          throw new IllegalStateException(super.toString());
      }
    }
  }
}
