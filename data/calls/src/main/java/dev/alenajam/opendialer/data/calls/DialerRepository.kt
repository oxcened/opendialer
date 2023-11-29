package dev.alenajam.opendialer.data.calls

import android.content.ContentResolver
import kotlinx.coroutines.flow.Flow

interface DialerRepository {
  fun getCalls(contentResolver: ContentResolver): Flow<List<DialerCallEntity>>
}