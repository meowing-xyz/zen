package xyz.meowing.zen.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object StringUtils {
    private val emoteRegex = "[^\\u0000-\\u007F]".toRegex()
    private val formatRegex = "[§&][0-9a-fk-or]".toRegex()

    fun String.removeFormatting(): String {
        return this.replace(formatRegex, "")
    }

    fun String.removeEmotes() = replace(emoteRegex, "")

    fun String.getRegexGroups(regex: Regex): MatchGroupCollection? {
        val regexMatchResult = regex.find(this) ?: return null
        return regexMatchResult.groups
    }

    fun String.decodeRoman(): Int {
        val values = mapOf('I' to 1, 'V' to 5, 'X' to 10, 'L' to 50, 'C' to 100, 'D' to 500, 'M' to 1000)
        var result = 0
        var prev = 0

        for (char in this.reversed()) {
            val current = values[char] ?: 0
            if (current < prev) result -= current
            else result += current
            prev = current
        }
        return result
    }

    fun getFormattedDate(): String {
        val today = LocalDate.now()
        val day = today.dayOfMonth
        val suffix = getDaySuffix(day)
        val formatter = DateTimeFormatter.ofPattern("MMMM d'$suffix', yyyy", Locale.ENGLISH)
        return today.format(formatter)
    }

    private fun getDaySuffix(day: Int): String {
        return when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }
    }
}