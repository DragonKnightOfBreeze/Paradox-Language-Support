package icu.windea.pls.ai.model.results

import icu.windea.pls.core.isExactDigit

data class LocalisationAiResult(
    val key: String,
    val text: String,
) : AiResult {
    companion object {
        @JvmField
        val EMPTY = LocalisationAiResult("", "")

        @JvmStatic
        fun fromLine(line: String): LocalisationAiResult {
            val line0 = line.trim()
            val colonIndex = line0.indexOf(':')
            if (colonIndex == -1) return EMPTY
            val key = line0.substring(0, colonIndex)
            val lqIndex = line0.indexOf('"', colonIndex + 1)
            val separator = line0.substring(colonIndex + 1, lqIndex).trimEnd()
            if (separator.isNotEmpty() && separator.any { !it.isExactDigit() }) return EMPTY
            val text = line0.substring(lqIndex + 1).removeSuffix("\"")
            return LocalisationAiResult(key, text)
        }
    }
}
