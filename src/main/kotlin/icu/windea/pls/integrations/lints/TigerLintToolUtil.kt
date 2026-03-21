package icu.windea.pls.integrations.lints

import icu.windea.pls.integrations.PlsIntegrationsBundle
import icu.windea.pls.integrations.lints.TigerLintResult.*
import icu.windea.pls.integrations.settings.PlsIntegrationsSettings
import icu.windea.pls.lang.inspections.lints.PlsTigerLintAnnotator
import icu.windea.pls.lang.inspections.lints.PlsTigerLintInspection
import kotlin.reflect.KMutableProperty0

object TigerLintToolUtil {
    /**
     * 按严重度（[severity]）和置信度（[confidence]）得到代码检查使用的高亮级别。
     *
     * 优先使用用户在设置中配置的 Tiger 高亮映射（Severity x Confidence），否则回退到默认映射。
     *
     * @see PlsIntegrationsSettings.TigerHighlightState
     * @see PlsTigerLintAnnotator
     * @see PlsTigerLintInspection
     */
    fun getHighlightSeverity(confidence: Confidence, severity: Severity): LintHighlightSeverity {
        return runCatching { getConfiguredHighlightSeverity(confidence, severity).get() }
            .getOrElse { getDefaultHighlightSeverity(confidence, severity) }
    }

    /**
     * 按严重度（[severity]）和置信度（[confidence]），得到代码检查使用的已配置的高亮级别对应的配置项的 Kotlin 属性。
     *
     * @see PlsIntegrationsSettings.TigerHighlightState
     * @see PlsTigerLintAnnotator
     * @see PlsTigerLintInspection
     */
    fun getConfiguredHighlightSeverity(confidence: Confidence, severity: Severity): KMutableProperty0<LintHighlightSeverity> {
        val mapping = PlsIntegrationsSettings.getInstance().state.lint.tigerHighlight
        return when (severity) {
            Severity.TIPS -> when (confidence) {
                Confidence.WEAK -> mapping::tipsWeak
                Confidence.REASONABLE -> mapping::tipsReasonable
                Confidence.STRONG -> mapping::tipsStrong
            }
            Severity.UNTIDY -> when (confidence) {
                Confidence.WEAK -> mapping::untidyWeak
                Confidence.REASONABLE -> mapping::untidyReasonable
                Confidence.STRONG -> mapping::untidyStrong
            }
            Severity.WARNING -> when (confidence) {
                Confidence.WEAK -> mapping::warningWeak
                Confidence.REASONABLE -> mapping::warningReasonable
                Confidence.STRONG -> mapping::warningStrong
            }
            Severity.ERROR -> when (confidence) {
                Confidence.WEAK -> mapping::errorWeak
                Confidence.REASONABLE -> mapping::errorReasonable
                Confidence.STRONG -> mapping::errorStrong
            }
            Severity.FATAL -> when (confidence) {
                Confidence.WEAK -> mapping::fatalWeak
                Confidence.REASONABLE -> mapping::fatalReasonable
                Confidence.STRONG -> mapping::fatalStrong
            }
        }
    }

    /**
     * 按严重度（[severity]）和置信度（[confidence]），得到代码检查使用的默认高亮级别。
     *
     * @see PlsTigerLintAnnotator
     * @see PlsTigerLintInspection
     */
    fun getDefaultHighlightSeverity(confidence: Confidence, severity: Severity): LintHighlightSeverity {
        return when (severity) {
            Severity.FATAL -> LintHighlightSeverity.ERROR
            Severity.ERROR -> LintHighlightSeverity.ERROR
            Severity.WARNING -> LintHighlightSeverity.WARNING
            Severity.UNTIDY -> when (confidence) {
                Confidence.STRONG -> LintHighlightSeverity.WARNING
                else -> LintHighlightSeverity.WEAK_WARNING
            }
            Severity.TIPS -> when (confidence) {
                Confidence.STRONG -> LintHighlightSeverity.WEAK_WARNING
                else -> LintHighlightSeverity.INFORMATION
            }
        }
    }

    fun getConfidenceDisplayName(confidence: Confidence): String {
        return when (confidence) {
            Confidence.WEAK -> PlsIntegrationsBundle.message("lint.tiger.confidence.weak")
            Confidence.REASONABLE -> PlsIntegrationsBundle.message("lint.tiger.confidence.reasonable")
            Confidence.STRONG -> PlsIntegrationsBundle.message("lint.tiger.confidence.strong")
        }
    }

    fun getSeverityDisplayName(severity: Severity): String {
        return when (severity) {
            Severity.TIPS -> PlsIntegrationsBundle.message("lint.tiger.severity.tips")
            Severity.UNTIDY -> PlsIntegrationsBundle.message("lint.tiger.severity.untidy")
            Severity.WARNING -> PlsIntegrationsBundle.message("lint.tiger.severity.warning")
            Severity.ERROR -> PlsIntegrationsBundle.message("lint.tiger.severity.error")
            Severity.FATAL -> PlsIntegrationsBundle.message("lint.tiger.severity.fatal")
        }
    }
}
