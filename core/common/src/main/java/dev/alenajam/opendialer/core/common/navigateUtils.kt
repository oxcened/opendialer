package dev.alenajam.opendialer.core.common

import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest

const val CALL_DETAIL_PARAM_CALL_IDS =
  "dev.alenajam.opendialer.feature.callDetail.CallDetailFragment_param_call_ids"

fun NavController.navigateToCallDetail(callIds: List<Int>) {
  val ids = callIds.joinToString(",")
  SharedPreferenceHelper.getSharedPreferences(context).edit()
    .putString(CALL_DETAIL_PARAM_CALL_IDS, ids).apply()
  val request = NavDeepLinkRequest.Builder
    .fromUri("android-app://dev.alenajam.opendialer/feature/callDetail/callDetailFragment".toUri())
    .build()
  navigate(request)
}