package icu.windea.pls.lang.codeInsight.type

import com.intellij.lang.ExpressionTypeProvider
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.psi.PsiElement
import com.intellij.ui.ColorUtil.*
import com.intellij.ui.Gray
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.values.FallbackStrings

/**
 * 用于显示各种类型信息（`View > Type Info`）。
 *
 * - 类型 - 如果表示一个表达式则可用。
 * - 规则类型 - 如果表示一个规则则可用。
 */
class CwtTypeProvider : ExpressionTypeProvider<PsiElement>() {
    // com.intellij.codeInsight.hint.JavaTypeProvider

    override fun getExpressionsAt(elementAt: PsiElement): List<PsiElement> {
        return CwtTypeManager.findTypedElements(elementAt)
    }

    override fun getInformationHint(element: PsiElement): @NlsContexts.HintText String {
        CwtTypeManager.getType(element)?.let { return it.id }
        return FallbackStrings.unknown
    }

    override fun getErrorHint(): @NlsContexts.HintText String {
        return PlsBundle.message("no.expression.found")
    }

    override fun hasAdvancedInformation(): Boolean {
        return true
    }

    override fun getAdvancedInformationHint(element: PsiElement): @NlsContexts.HintText String {
        val map = buildMap {
            val type = CwtTypeManager.getType(element)
            type?.let { this[PlsBundle.message("title.type")] = it.id }

            val configType = CwtTypeManager.getConfigType(element)
            configType?.let { this[PlsBundle.message("title.configType")] = it.id }

            val expression = CwtTypeManager.getExpression(element)
            expression?.let { this[PlsBundle.message("title.expression")] = it }
        }
        return buildHtml(map)
    }

    private fun buildHtml(map: Map<String, String>): String {
        val sectionColor = Gray.get(0x90)
        val rows = map.map {
            val titleCell = HtmlChunk.tag("td")
                .attr("align", "left").attr("valign", "top")
                .style("color:" + toHtmlColor(sectionColor))
                .addText("${it.key}:")
            val contentCell = HtmlChunk.tag("td").addText(it.value)
            HtmlChunk.tag("tr").children(titleCell, contentCell)
        }
        return HtmlChunk.tag("table").children(rows).toString()
    }
}
