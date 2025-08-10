package icu.windea.pls.ai.util.manipulators

import icu.windea.pls.core.isExactDigit

data class ParadoxLocalisationAiResult(
    val key: String,
    val text: String,
) {
    companion object {
        @JvmField
        val EMPTY = ParadoxLocalisationAiResult("", "")

        @JvmStatic
        fun fromLine(line: String): ParadoxLocalisationAiResult {
            val line0 = line.trim()
            val colonIndex = line0.indexOf(':')
            if (colonIndex == -1) return EMPTY
            val key = line0.substring(0, colonIndex)
            val lqIndex = line0.indexOf('"', colonIndex + 1)
            val separator = line0.substring(colonIndex + 1, lqIndex).trimEnd()
            if (separator.isNotEmpty() && separator.any { !it.isExactDigit() }) return EMPTY
            val text = line0.substring(lqIndex + 1).removeSuffix("\"")
            return ParadoxLocalisationAiResult(key, text)
        }
    }
}
