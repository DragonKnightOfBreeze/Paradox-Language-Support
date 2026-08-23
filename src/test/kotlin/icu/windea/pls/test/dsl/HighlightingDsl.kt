@file:Suppress("unused")

package icu.windea.pls.test.dsl

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture

// see: https://plugins.jetbrains.com/docs/intellij/testing-highlighting.html

/**
 * 这个 DSL 提供了一组作用域方法，从而支持以字符串插值的格式构建用于检查代码高亮结果的测试数据文本。
 *
 * 这种方式相比直接使用原始文本或原始文件，更加可读和可维护。
 *
 * 示例：
 * - `myFixture.configureByText("test.txt") { "${error(message)}key${errorEnd} = value" }`
 */
@DslMarker
annotation class HighlightingDsl

/**
 * @see HighlightingDsl
 */
inline fun <R> highlightingScope(block: HighlightingScope.() -> R): R = HighlightingScope.block()

/**
 * @see HighlightingDsl
 */
inline fun CodeInsightTestFixture.configureByText(fileName: String, block: HighlightingScope.() -> String): PsiFile = configureByText(fileName, HighlightingScope.block())

/**
 * @see HighlightingDsl
 */
inline fun CodeInsightTestFixture.createFile(fileName: String, block: HighlightingScope.() -> String): VirtualFile = createFile(fileName, HighlightingScope.block())

/**
 * @see HighlightingDsl
 */
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

    fun warningEnd() = "</$warningMarker>"

    fun weakWarningEnd() = "</$weakWarningMarker>"

    fun infoEnd() = "</$infoMarker>"
}
