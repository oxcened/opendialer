package dev.alenajam.opendialer.model;

import androidx.annotation.DrawableRes;

import java.io.Serializable;

public class CallOption implements Serializable {
  public static final int ID_COPY_NUMBER = 0;
  public static final int ID_EDIT_BEFORE_CALL = 1;
  public static final int ID_DELETE = 2;
  public static final int ID_SEND_MESSAGE = 3;
  public static final int ID_ADD_EXISTING = 4;
  public static final int ID_CREATE_CONTACT = 5;
  public static final int ID_CALL_DETAILS = 6;
  public static final int ID_BLOCK_CALLER = 7;
  public static final int ID_UNBLOCK_CALLER = 8;

  @DrawableRes
  private int id;
  private int icon;
  private String text;

  public CallOption(int id, int icon, String text) {
    this.id = id;
    this.icon = icon;
    this.text = text;
  }

  public CallOption(int id, int icon) {
    this.id = id;
    this.icon = icon;
  }

  public int getIcon() {
    return icon;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public int getId() {
    return id;
  }
}
