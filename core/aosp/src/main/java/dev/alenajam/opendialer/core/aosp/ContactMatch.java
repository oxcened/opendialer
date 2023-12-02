package dev.alenajam.opendialer.core.aosp;

import java.util.Objects;

/**
 * Data format for finding duplicated contacts.
 */
public class ContactMatch {

  private final String lookupKey;
  private final long id;

  public ContactMatch(String lookupKey, long id) {
    this.lookupKey = lookupKey;
    this.id = id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(lookupKey, id);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object instanceof ContactMatch) {
      final ContactMatch that = (ContactMatch) object;
      return Objects.equals(this.lookupKey, that.lookupKey) && Objects.equals(this.id, that.id);
    }
    return false;
  }
}