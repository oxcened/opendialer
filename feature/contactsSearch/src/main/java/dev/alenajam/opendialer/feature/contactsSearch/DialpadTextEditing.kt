package dev.alenajam.opendialer.feature.contactsSearch

import androidx.compose.ui.text.TextRange

internal data class DialpadTextEdit(
    val text: String,
    val selection: TextRange
)

internal fun replaceDialpadSelection(
    text: String,
    selection: TextRange,
    replacement: String
): DialpadTextEdit {
    val start = minOf(selection.start, selection.end).coerceIn(0, text.length)
    val end = maxOf(selection.start, selection.end).coerceIn(start, text.length)

    return DialpadTextEdit(
        text = text.replaceRange(start, end, replacement),
        selection = TextRange(start + replacement.length)
    )
}

internal fun deleteDialpadSelection(
    text: String,
    selection: TextRange
): DialpadTextEdit {
    val start = minOf(selection.start, selection.end).coerceIn(0, text.length)
    val end = maxOf(selection.start, selection.end).coerceIn(start, text.length)
    val deleteStart = if (start == end) (start - 1).coerceAtLeast(0) else start

    return DialpadTextEdit(
        text = text.replaceRange(deleteStart, end, ""),
        selection = TextRange(deleteStart)
    )
}
