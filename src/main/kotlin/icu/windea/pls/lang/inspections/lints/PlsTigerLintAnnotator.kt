package icu.windea.pls.lang.inspections.lints

import com.intellij.lang.annotation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.integrations.lints.*
import icu.windea.pls.integrations.lints.tools.*

//com.intellij.codeInspection.javaDoc.JavadocHtmlLintAnnotator

/**
 * @see PlsTigerLintManager
 * @see PlsTigerLintToolProvider
 * @see PlsTigerLintResult
 */
class PlsTigerLintAnnotator : ExternalAnnotator<PlsTigerLintAnnotator.Info, PlsTigerLintResult>() {
    data class Info(val file: PsiFile)

    override fun getPairedBatchInspectionShortName() = PlsTigerLintInspection.SHORT_NAME

    override fun collectInformation(file: PsiFile): Info? {
        if (!PlsTigerLintManager.checkAvailableFor(file)) return null
        return Info(file)
    }

    override fun collectInformation(file: PsiFile, editor: Editor, hasErrors: Boolean): Info? {
        return collectInformation(file) //兼容先前已经检测到错误的情况
    }

    override fun doAnnotate(collectedInfo: Info?): PlsTigerLintResult? {
        val file = collectedInfo?.file ?: return null
        return PlsTigerLintManager.getTigerLintResultForFile(file)
    }

    override fun apply(file: PsiFile, annotationResult: PlsTigerLintResult?, holder: AnnotationHolder) {
        if (annotationResult == null) return
        val items = annotationResult.items
        if (items.isEmpty()) return

        for (item in items) {
            val severity = getHighlightSeverity(item)
            val message = getMessage(annotationResult, item)
            for (location in item.locations) {
                val extraMessage = getExtraMessage(annotationResult, item, location)
                val fullMessage = message + extraMessage

                // for whole file
                if (location.lineNumber == null || location.column == null) {
                    holder.newAnnotation(severity, fullMessage)
                        .fileLevel()
                        .problemGroup { getProblemGroup(annotationResult, item) }
                        .create()
                    continue
                }

                val lineStartOffset = file.fileDocument.getLineStartOffset(location.lineNumber - 1)
                val range = TextRange.from(lineStartOffset + location.column - 1, (location.length ?: 0).coerceAtLeast(0))
                holder.newAnnotation(severity, fullMessage)
                    .range(range)
                    .letIf(location.length == null) { it.afterEndOfLine() }
                    .problemGroup { getProblemGroup(annotationResult, item) }
                    .create()
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

    private fun getMessage(result: PlsTigerLintResult, item: PlsTigerLintResult.Item): String {
        // output example:
        // error(missing-item): media alias asia_confucianism_shin not defined in gfx/media_aliases/

        return buildString {
            append("[").append(result.name).append("] ") //tool name
            append(item.severity).append("(").append(item.key).append("): ") //prefix (severity+key)
            append(item.message) //message
        }
    }

    private fun getExtraMessage(result: PlsTigerLintResult, item: PlsTigerLintResult.Item, location: PlsTigerLintResult.Location): String {
        val list = buildList {
            item.confidence.name.lowercase().let { add("Confidence: $it") }
            location.tag?.orNull()?.let { add("Tag: $it") }
            item.info?.orNull()?.let { add("Info: $it") }
        }
        return list.joinToString(", ", " (", ")")
    }

    private fun getProblemGroup(result: PlsTigerLintResult, item: PlsTigerLintResult.Item): String {
        return "PLS_TIGER_LINT.${result.name}"
    }
}
