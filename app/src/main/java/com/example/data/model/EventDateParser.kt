package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Locale

object EventDateParser {
  private val PATTERNS = listOf(
    "yyyy-MM-dd",
    "d MMMM yyyy",
    "MMMM d, yyyy",
    "MMMM d yyyy",
    "d MMM yyyy",
    "MMM d, yyyy",
    "MMM d yyyy",
    "yyyy/MM/dd",
    "dd/MM/yyyy",
    "MM/dd/yyyy",
    "dd-MM-yyyy",
    "MM-dd-yyyy"
  )

  /**
   * Derives the full weekday name (e.g., "Saturday") from a given date string.
   * Returns empty string if the date is null, blank, or cannot be parsed.
   */
  fun deriveDayOfWeek(dateStr: String?, locale: Locale = Locale.getDefault()): String {
    if (dateStr.isNullOrBlank()) return ""
    val cleaned = dateStr.trim()

    for (pattern in PATTERNS) {
      try {
        val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
        sdf.isLenient = false
        val parsed = sdf.parse(cleaned)
        if (parsed != null) {
          val outFormat = SimpleDateFormat("EEEE", locale)
          return outFormat.format(parsed)
        }
      } catch (_: Exception) {
      }
    }

    if (locale != Locale.ENGLISH) {
      for (pattern in PATTERNS) {
        try {
          val sdf = SimpleDateFormat(pattern, locale)
          sdf.isLenient = false
          val parsed = sdf.parse(cleaned)
          if (parsed != null) {
            val outFormat = SimpleDateFormat("EEEE", locale)
            return outFormat.format(parsed)
          }
        } catch (_: Exception) {
        }
      }
    }

    return ""
  }

  /**
   * Formats wedding couple names with "with" (e.g. "Ali with Ayesha").
   * Handles empty names gracefully:
   * - Groom & Bride: "Ali with Ayesha"
   * - Groom only: "Ali"
   * - Bride only: "Ayesha"
   * - Neither: ""
   */
  fun formatCoupleNames(groomName: String?, brideName: String?): String {
    val groom = groomName?.trim().orEmpty()
    val bride = brideName?.trim().orEmpty()
    return when {
      groom.isNotEmpty() && bride.isNotEmpty() -> "$groom with $bride"
      groom.isNotEmpty() -> groom
      bride.isNotEmpty() -> bride
      else -> ""
    }
  }
}
