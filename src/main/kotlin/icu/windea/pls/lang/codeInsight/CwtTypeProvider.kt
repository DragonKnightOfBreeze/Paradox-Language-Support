package icu.windea.pls.lang.codeInsight

import com.intellij.lang.*
import com.intellij.openapi.util.*
import com.intellij.openapi.util.text.*
import com.intellij.psi.*
import com.intellij.ui.*
import com.intellij.ui.ColorUtil.*
import icu.windea.pls.*
import icu.windea.pls.config.util.*

/**
 * 用于显示各种类型信息（`View > Type Info`）。
 *
 * * 基本类型 - 基于PSI的类型
 * * 规则类型 - 当PSI表示一个规则时可用，基于规则的位置
 */
class CwtTypeProvider : ExpressionTypeProvider<PsiElement>() {
    override fun getExpressionsAt(elementAt: PsiElement): List<PsiElement> {
        return CwtTypeManager.findTypedElements(elementAt)
    }

    override fun getInformationHint(element: PsiElement): @NlsContexts.HintText String {
        return CwtTypeManager.getType(element).id
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
            type.let { this[PlsBundle.message("title.cwtType")] = it.id }

            val configType = CwtConfigManager.getConfigType(element)
            configType?.let { this[PlsBundle.message("title.cwtConfigType")] = it.id }
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
