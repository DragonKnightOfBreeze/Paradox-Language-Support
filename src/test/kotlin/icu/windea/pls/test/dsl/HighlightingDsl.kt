@file:Suppress("unused")

package icu.windea.pls.test.dsl

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.testFramework.fixtures.CodeInsightTestFixture

// see: https://plugins.jetbrains.com/docs/intellij/testing-highlighting.html

@DslMarker
annotation class HighlightingDsl

inline fun <R> highlightingScope(block: HighlightingScope.() -> R): R = HighlightingScope.block()

@HighlightingDsl
object HighlightingScope {
    const val errorMarker = CodeInsightTestFixture.ERROR_MARKER
    const val warningMarker = CodeInsightTestFixture.WARNING_MARKER
    const val weakWarningMarker = CodeInsightTestFixture.WEAK_WARNING_MARKER
    const val infoMarker = CodeInsightTestFixture.INFO_MARKER

    fun error(descr: String) = """<$errorMarker descr="${descr.escapeDescr()}">"""

    fun warning(descr: String) = """<$warningMarker descr="${descr.escapeDescr()}">"""

    fun weakWarning(descr: String) = """<$weakWarningMarker descr="${descr.escapeDescr()}">"""

    fun info(descr: String) = """<$infoMarker descr="${descr.escapeDescr()}">"""

    private fun String.escapeDescr(): String = replace("\"", "\\\\\"")

    fun info(textAttributesKey: TextAttributesKey) = """<$infoMarker descr="null" textAttributesKey="${textAttributesKey.externalName}">"""

    fun errorEnd() = "</$errorMarker>"

    fun warnEnd() = "</$warningMarker>"

    fun weakWarningEnd() = "</$weakWarningMarker>"

    fun infoEnd() = "</$infoMarker>"
}
