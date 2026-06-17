package icu.windea.pls.integrations.lints

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.icons.AllIcons
import com.intellij.lang.annotation.HighlightSeverity
import icu.windea.pls.core.optimized
import icu.windea.pls.integrations.PlsIntegrationsBundle

/**
 * 高亮严重度级别的枚举。
 */
enum class LintHighlightSeverity(val value: HighlightSeverity?) {
    INFORMATION(HighlightSeverity.INFORMATION),
    WEAK_WARNING(HighlightSeverity.WEAK_WARNING),
    WARNING(HighlightSeverity.WARNING),
    ERROR(HighlightSeverity.ERROR),
    Merged(null),
    ;

    val level = value?.let { HighlightDisplayLevel.find(it) }
    val displayName = value?.displayName ?: PlsIntegrationsBundle.message("lint.highlightSeverity.mixed")
    val icon = level?.icon ?: AllIcons.General.InspectionsMixed

    companion object {
        private val values = entries.toList().optimized()
        private val valuesSpecific = entries.filter { it != Merged }.optimized()

        @JvmStatic
        fun getAll(): List<LintHighlightSeverity> = values

        @JvmStatic
        fun getAllSpecific(): List<LintHighlightSeverity> = valuesSpecific
    }
}
