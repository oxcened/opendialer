package dev.alenajam.opendialer.feature.contactsSearch

import android.content.Context
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import dev.alenajam.opendialer.core.aosp.SmartDialMatchPosition
import dev.alenajam.opendialer.core.aosp.SmartDialNameMatcher

internal fun highlightSearchMatch(
    context: Context,
    text: String,
    query: String,
    isDialpadSearch: Boolean,
    isPhoneNumber: Boolean,
): AnnotatedString {
    if (text.isBlank() || query.isBlank()) return AnnotatedString(text)

    val matchRanges = if (isDialpadSearch) {
        val matcher = SmartDialNameMatcher(query)
        if (isPhoneNumber) {
            listOfNotNull(matcher.matchesNumber(context, text, query))
        } else if (matcher.matches(context, text)) {
            matcher.matchPositions
        } else {
            emptyList()
        }
    } else if (isPhoneNumber) {
        textPhoneMatchPositions(text, query)
    } else {
        textPrefixMatchPositions(text, query)
    }

    return buildAnnotatedString {
        append(text)
        matchRanges.forEach { match ->
            val start = match.start.coerceIn(0, text.length)
            val end = match.end.coerceIn(start, text.length)
            if (start < end) {
                addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
            }
        }
    }
}

internal fun textPrefixMatchPositions(text: String, query: String): List<SmartDialMatchPosition> = buildList {
    var searchStart = 0
    while (searchStart < text.length) {
        val matchStart = text.indexOf(query, searchStart, ignoreCase = true)
        if (matchStart < 0) break
        if (matchStart == 0 || text[matchStart - 1].isWhitespace()) {
            add(SmartDialMatchPosition(matchStart, matchStart + query.length))
            break
        }
        searchStart = matchStart + query.length
    }
}

private fun textPhoneMatchPositions(text: String, query: String): List<SmartDialMatchPosition> {
    val queryDigits = query.filter(Char::isDigit)
    if (queryDigits.isEmpty()) return emptyList()

    val digitIndices = text.indices.filter { text[it].isDigit() }
    val digits = digitIndices.joinToString(separator = "") { text[it].toString() }
    val matchStart = digits.indexOf(queryDigits)
    if (matchStart < 0) return emptyList()

    val start = digitIndices[matchStart]
    val end = digitIndices[matchStart + queryDigits.lastIndex] + 1
    return listOf(SmartDialMatchPosition(start, end))
}
