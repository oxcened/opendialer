/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package dev.alenajam.opendialer.data.callsCache;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteFullException;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import dev.alenajam.opendialer.core.common.PermissionUtils;
import dev.alenajam.opendialer.core.aosp.PhoneNumberHelper;
import dev.alenajam.opendialer.core.aosp.UriUtils;

/**
 * Utility class to look up the contact information for a given number.
 */
public class ContactInfoHelper {

  private final Context context;

  public ContactInfoHelper(Context context) {
    this.context = context;
  }

  /**
   * Creates a JSON-encoded lookup uri for a unknown number without an associated contact
   *
   * @param number - Unknown phone number
   * @return JSON-encoded URI that can be used to perform a lookup when clicking on the quick
   * contact card.
   */
  public static Uri createTemporaryContactUri(String number) {
    try {
      final JSONObject contactRows =
          new JSONObject()
              .put(
                  ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                  new JSONObject()
                      .put(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                      .put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM));

      final String jsonString =
          new JSONObject()
              .put(ContactsContract.Contacts.DISPLAY_NAME, number)
              .put(ContactsContract.Contacts.DISPLAY_NAME_SOURCE, ContactsContract.DisplayNameSources.PHONE)
              .put(ContactsContract.Contacts.CONTENT_ITEM_TYPE, contactRows)
              .toString();

      return ContactsContract.Contacts.CONTENT_LOOKUP_URI
          .buildUpon()
          .appendPath("encoded")
          .appendQueryParameter(
              ContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(Long.MAX_VALUE))
          .encodedFragment(jsonString)
          .build();
    } catch (JSONException e) {
      return null;
    }
  }

  /**
   * Stores differences between the updated contact info and the current call log contact info.
   *
   * @param number      The number of the contact.
   * @param countryIso  The country associated with this number.
   * @param updatedInfo The updated contact info.
   * @param callLogInfo The call log entry's current contact info.
   */
  public void updateCallLogContactInfo(
      String number, String countryIso, ContactInfo updatedInfo, ContactInfo callLogInfo) {
    if (!PermissionUtils.hasRecentsPermission(context)) {
      return;
    }

    final ContentValues values = new ContentValues();
    boolean needsUpdate = false;

    if (callLogInfo != null) {
      if (!TextUtils.equals(updatedInfo.getName(), callLogInfo.getName())) {
        values.put(Calls.CACHED_NAME, updatedInfo.getName());
        needsUpdate = true;
      }

      if (!Objects.equals(updatedInfo.getType(), callLogInfo.getType())) {
        values.put(Calls.CACHED_NUMBER_TYPE, updatedInfo.getType());
        needsUpdate = true;
      }

      if (!TextUtils.equals(updatedInfo.getLabel(), callLogInfo.getLabel())) {
        values.put(Calls.CACHED_NUMBER_LABEL, updatedInfo.getLabel());
        needsUpdate = true;
      }

      // Only replace the normalized number if the new updated normalized number isn't empty.
      if (!TextUtils.isEmpty(updatedInfo.getNormalizedNumber())
          && !TextUtils.equals(updatedInfo.getNormalizedNumber(), callLogInfo.getNormalizedNumber())) {
        values.put(Calls.CACHED_NORMALIZED_NUMBER, updatedInfo.getNormalizedNumber());
        needsUpdate = true;
      }

      if (!TextUtils.equals(updatedInfo.getNumber(), callLogInfo.getNumber())) {
        values.put(Calls.CACHED_MATCHED_NUMBER, updatedInfo.getNumber());
        needsUpdate = true;
      }

      if (!Objects.equals(updatedInfo.getPhotoId(), callLogInfo.getPhotoId())) {
        values.put(Calls.CACHED_PHOTO_ID, updatedInfo.getPhotoId());
        needsUpdate = true;
      }

      final Uri updatedPhotoUriContactsOnly = UriUtils.nullForNonContactsUri(UriUtils.parseUriOrNull(updatedInfo.getPhotoUri()));
      if (!UriUtils.areEqual(updatedPhotoUriContactsOnly, UriUtils.parseUriOrNull(callLogInfo.getPhotoUri()))) {
        values.put(Calls.CACHED_PHOTO_URI, UriUtils.uriToString(updatedPhotoUriContactsOnly));
        needsUpdate = true;
      }

      if (!TextUtils.equals(updatedInfo.getGeoDescription(), callLogInfo.getGeoDescription())) {
        values.put(Calls.GEOCODED_LOCATION, updatedInfo.getGeoDescription());
        needsUpdate = true;
      }
    } else {
      // No previous values, store all of them.
      values.put(Calls.CACHED_NAME, updatedInfo.getName());
      values.put(Calls.CACHED_NUMBER_TYPE, updatedInfo.getType());
      values.put(Calls.CACHED_NUMBER_LABEL, updatedInfo.getLabel());
      values.put(Calls.CACHED_LOOKUP_URI, updatedInfo.getLookupUri());
      values.put(Calls.CACHED_MATCHED_NUMBER, updatedInfo.getNumber());
      values.put(Calls.CACHED_NORMALIZED_NUMBER, updatedInfo.getNormalizedNumber());
      values.put(Calls.CACHED_PHOTO_ID, updatedInfo.getPhotoId());
      values.put(Calls.CACHED_PHOTO_URI, updatedInfo.getPhotoUri());
      values.put(Calls.CACHED_FORMATTED_NUMBER, updatedInfo.getFormattedNumber());
      values.put(Calls.GEOCODED_LOCATION, updatedInfo.getGeoDescription());
      needsUpdate = true;
    }

    if (!needsUpdate) {
      return;
    }

    try {
      if (countryIso == null) {
        context
            .getContentResolver()
            .update(
                Calls.CONTENT_URI,
                values,
                Calls.NUMBER + " = ? AND " + Calls.COUNTRY_ISO + " IS NULL",
                new String[]{number});
      } else {
        int updated = context
            .getContentResolver()
            .update(
                Calls.CONTENT_URI,
                values,
                Calls.NUMBER + " = ? AND " + Calls.COUNTRY_ISO + " = ?",
                new String[]{number, countryIso});
      }
    } catch (SQLiteFullException e) {
      Log.e(ContactInfoHelper.class.getSimpleName(), "Unable to update contact info in call log db", e);
    }
  }

  public ContactInfo createEmptyContactInfoForNumber(String number, String countryIso) {
    String formattedNumber = formatPhoneNumber(number, null, countryIso);
    String normalizedNumber = PhoneNumberUtils.formatNumberToE164(number, countryIso);
    return new ContactInfo(
        null,
        number,
        null,
        null,
        null,
        UriUtils.uriToString(createTemporaryContactUri(formattedNumber)),
        normalizedNumber,
        formattedNumber,
        null,
        null
    );
  }

  /**
   * Format the given phone number
   *
   * @param number           the number to be formatted.
   * @param normalizedNumber the normalized number of the given number.
   * @param countryIso       the ISO 3166-1 two letters country code, the country's convention will be
   *                         used to format the number if the normalized phone is null.
   * @return the formatted number, or the given number if it was formatted.
   */
  public String formatPhoneNumber(String number, String normalizedNumber, String countryIso) {
    if (TextUtils.isEmpty(number)) {
      return "";
    }
    // If "number" is really a SIP address, don't try to do any formatting at all.
    if (PhoneNumberHelper.isUriNumber(number)) {
      return number;
    }
    if (TextUtils.isEmpty(countryIso)) {
      return number;
    }
    return PhoneNumberHelper.formatNumber(context, number, normalizedNumber, countryIso);
  }
}
