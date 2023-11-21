package dev.alenajam.opendialer.model;

import java.io.Serializable;

public class Contact implements Serializable {
  private int id;
  private String name;
  private String number;
  private boolean starred;
  private String imageUri;
  private String lookupKey;

  public Contact(int id) {
    this.id = id;
  }

  public Contact(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public Contact(String name, String number) {
    this.name = name;
    this.number = number;
  }

  public Contact(int id, String name, boolean starred, String imageUri) {
    this.id = id;
    this.name = name;
    this.starred = starred;
    this.imageUri = imageUri;
  }

  public Contact(int id, String name, String number) {
    this.id = id;
    this.name = name;
    this.number = number;
  }

  public Contact(int id, String name, String number, String imageUri) {
    this.id = id;
    this.name = name;
    this.number = number;
    this.imageUri = imageUri;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNumber() {
    return number;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public boolean isStarred() {
    return starred;
  }

  public String getImageUri() {
    return imageUri;
  }

  public String getLookupKey() {
    return lookupKey;
  }

  public void setLookupKey(String lookupKey) {
    this.lookupKey = lookupKey;
  }
}
