package icu.windea.pls.integrations.lints

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.icons.AllIcons
import com.intellij.lang.annotation.HighlightSeverity
import icu.windea.pls.PlsBundle

/**
 * 高亮严重度级别的枚举。
 */
enum class PlsLintHighlightSeverity(val value: HighlightSeverity?) {
    INFORMATION(HighlightSeverity.INFORMATION),
    WEAK_WARNING(HighlightSeverity.WEAK_WARNING),
    WARNING(HighlightSeverity.WARNING),
    ERROR(HighlightSeverity.ERROR),
    Merged(null),
    ;

    val level = value?.let { HighlightDisplayLevel.find(it) }
    val displayName = value?.displayName ?: PlsBundle.message("lint.highlightSeverity.mixed")
    val icon = level?.icon ?: AllIcons.General.InspectionsMixed

    companion object {
        private val values = entries.toList()
        private val valuesNoMerged = values - Merged

        fun getAll(withMerged: Boolean = false): List<PlsLintHighlightSeverity> {
            return if (withMerged) values else valuesNoMerged
        }
    }
}
