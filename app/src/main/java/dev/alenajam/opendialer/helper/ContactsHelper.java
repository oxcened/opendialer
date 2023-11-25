package dev.alenajam.opendialer.helper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import androidx.annotation.Nullable;

import dev.alenajam.opendialer.model.Contact;
import dev.alenajam.opendialer.util.PermissionUtils;

public abstract class ContactsHelper {
  private static String[] projectionPhoneLookup = new String[]{
      ContactsContract.PhoneLookup.CONTACT_ID,
      ContactsContract.PhoneLookup.DISPLAY_NAME,
      ContactsContract.PhoneLookup.TYPE,
      ContactsContract.PhoneLookup.LABEL,
      ContactsContract.PhoneLookup.NUMBER,
      ContactsContract.PhoneLookup.NORMALIZED_NUMBER,
      ContactsContract.PhoneLookup.PHOTO_ID,
      ContactsContract.PhoneLookup.LOOKUP_KEY,
      ContactsContract.PhoneLookup.PHOTO_URI,
      ContactsContract.PhoneLookup._ID
  };

  @Nullable
  public static Contact getContactByPhoneNumber(Context context, String phoneNumber) {
    if (!PermissionUtils.hasContactsPermission(context) || phoneNumber == null || phoneNumber.isEmpty())
      return null;

    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
    Cursor cursor = context.getContentResolver().query(
        uri, projectionPhoneLookup, null, null, null, null
    );

    if (cursor != null && cursor.moveToFirst()) {
      Contact contact = new Contact(
          cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID)),
          cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)),
          phoneNumber,
          cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_URI))
      );
      cursor.close();
      return contact;
    } else if (cursor != null) cursor.close();
    return null;
  }
}
