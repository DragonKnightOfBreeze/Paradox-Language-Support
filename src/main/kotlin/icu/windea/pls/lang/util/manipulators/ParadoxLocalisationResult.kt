package icu.windea.pls.lang.util.manipulators

import icu.windea.pls.core.*
import icu.windea.pls.lang.*

data class ParadoxLocalisationResult(
    val key: String,
    val text: String,
) {
    companion object {
        @JvmField
        val EMPTY = ParadoxLocalisationResult("", "")

        @JvmStatic
        fun fromLine(line: String): ParadoxLocalisationResult {
            val line0 = line.trim()
            val colonIndex = line0.indexOf(':')
            if (colonIndex == -1) return EMPTY
            val key = line0.substring(0, colonIndex)
            if (!key.isIdentifier('.')) return EMPTY
            val lqIndex = line0.indexOf('"', colonIndex + 1)
            val separator = line0.substring(colonIndex + 1, lqIndex).trimEnd()
            if (separator.isNotEmpty() && separator.any { !it.isExactDigit() }) return EMPTY
            val text = line0.substring(lqIndex + 1).removeSuffix("\"")
            return ParadoxLocalisationResult(key, text)
        }
    }
}
