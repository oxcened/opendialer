package dev.alenajam.opendialer.data.calls

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.BlockedNumberContract

object BlockedNumbersData {
    fun insert(contentResolver: ContentResolver, number: String): Uri? {
        val values = ContentValues().apply {
            put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number)
        }
        return contentResolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI, values)
    }

    fun unblock(context: Context, number: String): Int {
        return BlockedNumberContract.unblock(context, number)
    }

    fun isBlocked(context: Context, number: String?): Boolean {
        return BlockedNumberContract.isBlocked(context, number)
    }
}
