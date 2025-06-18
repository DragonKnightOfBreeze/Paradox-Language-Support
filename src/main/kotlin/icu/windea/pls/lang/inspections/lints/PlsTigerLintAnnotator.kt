package icu.windea.pls.lang.inspections.lints

import com.intellij.lang.annotation.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.integrations.lints.*
import icu.windea.pls.integrations.lints.tools.*

/**
 * @see PlsTigerLintManager
 * @see PlsTigerLintToolProvider
 * @see PlsTigerLintResult
 */
class PlsTigerLintAnnotator : ExternalAnnotator<PlsTigerLintAnnotator.Info, PlsTigerLintResult>() {
    data class Info(val file: PsiFile)

    override fun getPairedBatchInspectionShortName() = PlsTigerLintInspection.SHORT_NAME

    override fun collectInformation(file: PsiFile): Info? {
        return Info(file)
    }

    override fun doAnnotate(collectedInfo: Info?): PlsTigerLintResult? {
        val file = collectedInfo?.file ?: return null
        return PlsTigerLintManager.getTigerLintResultForFile(file.virtualFile, file.project)
    }

    override fun apply(file: PsiFile, annotationResult: PlsTigerLintResult?, holder: AnnotationHolder) {
        if (annotationResult == null) return
        val items = annotationResult.items
        if (items.isEmpty()) return

        for (item in items) {
            val severity = getHighlightSeverity(item)
            val message = getMessage(item)
            for (location in item.locations) {
                //TODO 2.0.0-dev 提供对conf文件的支持并在这里应用过滤

                // for whole file
                if (location.linenr == null || location.column == null || location.length == null) {
                    holder.newAnnotation(severity, message).fileLevel().create()
                    continue
                }

                val lineStartOffset = file.fileDocument.getLineStartOffset(location.linenr)
                val range = TextRange.from(lineStartOffset + location.column - 1, location.length)
                holder.newAnnotation(severity, message).range(range).create()
            }
        }
    }

    private fun getHighlightSeverity(item: PlsTigerLintResult.Item): HighlightSeverity {
        return when (item.severity) {
            PlsTigerLintResult.Severity.Tips -> HighlightSeverity.INFORMATION
            PlsTigerLintResult.Severity.Untidy -> HighlightSeverity.WEAK_WARNING
            PlsTigerLintResult.Severity.Warning -> HighlightSeverity.WARNING
            PlsTigerLintResult.Severity.Error -> HighlightSeverity.ERROR
            PlsTigerLintResult.Severity.Fatal -> HighlightSeverity.ERROR
        }
    }

    private fun getMessage(item: PlsTigerLintResult.Item): String {
        // output example:
        // error(missing-item): media alias asia_confucianism_shin not defined in gfx/media_aliases/

        return buildString {
            append("[").append(item.confidence).append("] ")
            append(item.severity).append("(").append(item.key).append("): ")
            append(item.message)
            if (item.info.isNotNullOrEmpty()) appendLine().append(item.info)
        }
    }
}
