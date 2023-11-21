/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package dev.alenajam.opendialer.util;

import android.content.Context;
import android.provider.CallLog;
import android.telephony.PhoneNumberUtils;
import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PhoneNumberHelper {

  private static final Set<String> LEGACY_UNKNOWN_NUMBERS =
      new HashSet<>(Arrays.asList("-1", "-2", "-3"));

  /**
   * Returns true if it is possible to place a call to the given number.
   */
  public static boolean canPlaceCallsTo(CharSequence number, int presentation) {
    return presentation == CallLog.Calls.PRESENTATION_ALLOWED
        && !TextUtils.isEmpty(number)
        && !isLegacyUnknownNumbers(number);
  }

  /**
   * Returns true if the input phone number contains special characters.
   */
  public static boolean numberHasSpecialChars(String number) {
    return !TextUtils.isEmpty(number) && number.contains("#");
  }

  /**
   * Returns true if the raw numbers of the two input phone numbers are the same.
   */
  public static boolean sameRawNumbers(String number1, String number2) {
    String rawNumber1 =
        PhoneNumberUtils.stripSeparators(PhoneNumberUtils.convertKeypadLettersToDigits(number1));
    String rawNumber2 =
        PhoneNumberUtils.stripSeparators(PhoneNumberUtils.convertKeypadLettersToDigits(number2));

    return rawNumber1.equals(rawNumber2);
  }

  /**
   * Returns true if the given number is a SIP address. To be able to mock-out this, it is not a
   * static method.
   */
  public static boolean isSipNumber(CharSequence number) {
    return number != null && isUriNumber(number.toString());
  }

  public static boolean isLegacyUnknownNumbers(CharSequence number) {
    return number != null && LEGACY_UNKNOWN_NUMBERS.contains(number.toString());
  }

  /**
   * An enhanced version of {@link PhoneNumberUtils#formatNumber(String, String, String)}.
   *
   * <p>The {@link Context} parameter allows us to tweak formatting according to device properties.
   *
   * <p>Returns the formatted phone number (e.g, 1-123-456-7890) or the original number if
   * formatting fails or is intentionally ignored.
   */
  public static String formatNumber(
      Context context, @Nullable String number, @Nullable String numberE164, String countryIso) {
    // The number can be null e.g. schema is voicemail and uri content is empty.
    if (number == null) {
      return null;
    }

   /* if (MotorolaUtils.shouldDisablePhoneNumberFormatting(context)) {
      return number;
    }
*/
    String formattedNumber = PhoneNumberUtils.formatNumber(number, numberE164, countryIso);
    return formattedNumber != null ? formattedNumber : number;
  }

  /**
   * @see #formatNumber(Context, String, String, String).
   */
  public static String formatNumber(Context context, @Nullable String number, String countryIso) {
    return formatNumber(context, number, /* numberE164 = */ null, countryIso);
  }

  @Nullable
  public static CharSequence formatNumberForDisplay(
      Context context, @Nullable String number, @NonNull String countryIso) {
    if (number == null) {
      return null;
    }

    return PhoneNumberUtils.createTtsSpannable(
        BidiFormatter.getInstance()
            .unicodeWrap(formatNumber(context, number, countryIso), TextDirectionHeuristics.LTR));
  }

  /**
   * Determines if the specified number is actually a URI (i.e. a SIP address) rather than a regular
   * PSTN phone number, based on whether or not the number contains an "@" character.
   *
   * @param number Phone number
   * @return true if number contains @
   * <p>TODO: Remove if PhoneNumberUtils.isUriNumber(String number) is made public.
   */
  public static boolean isUriNumber(String number) {
    // Note we allow either "@" or "%40" to indicate a URI, in case
    // the passed-in string is URI-escaped.  (Neither "@" nor "%40"
    // will ever be found in a legal PSTN number.)
    return number != null && (number.contains("@") || number.contains("%40"));
  }
}
