package icu.windea.pls.lang.codeInsight

import com.intellij.lang.*
import com.intellij.openapi.util.text.*
import com.intellij.psi.*
import com.intellij.ui.*
import com.intellij.ui.ColorUtil.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

//com.intellij.codeInsight.hint.JavaTypeProvider

/**
 * 用于显示各种类型信息（`View > Type Info`）。
 *
 * * 基本类型 - 基于PSI的类型
 * * 表达式 - 如果PSI表示一个表达式则可用
 * * 规则表达式 - 如果存在对应的CWT规则表达式则可用
 * * 定义类型 - 如果PSI是[ParadoxScriptPropertyKey]则可用
 * * 作用域上下文信息 - 如果存在则可用
 */
class ParadoxTypeProvider : ExpressionTypeProvider<PsiElement>() {
    val sectionColor = Gray.get(0x90)

    override fun getExpressionsAt(elementAt: PsiElement): List<PsiElement> {
        return ParadoxTypeManager.findTypedElements(elementAt)
    }

    /**
     * 优先显示最相关的类型信息（定义类型，规则表达式，或者基本类型）。
     * 显示定义的类型，或者对应的CWT规则表达式，或者基本类型。
     */
    override fun getInformationHint(element: PsiElement): String {
        ParadoxTypeManager.getDefinitionType(element)?.let { return it.escapeXml() }
        ParadoxTypeManager.getConfigExpression(element)?.let { return it.escapeXml() }
        ParadoxTypeManager.getType(element).let { return it.id }
    }

    override fun getErrorHint(): String {
        return PlsBundle.message("no.expression.found")
    }

    override fun hasAdvancedInformation(): Boolean {
        return true
    }

    override fun getAdvancedInformationHint(element: PsiElement): String {
        val map = buildMap {
            val definitionType = ParadoxTypeManager.getDefinitionType(element)
            definitionType?.let { this[PlsBundle.message("title.definitionType")] = it }

            val type = ParadoxTypeManager.getType(element)
            type.let { this[PlsBundle.message("title.type")] = it.id }

            val expression = ParadoxTypeManager.getExpression(element)
            expression?.let { this[PlsBundle.message("title.expression")] = it }

            val configExpression = ParadoxTypeManager.getConfigExpression(element)
            configExpression?.let { this[PlsBundle.message("title.configExpression")] = it }

            val scopeContext = ParadoxTypeManager.getScopeContext(element)
            val scopeContextString = scopeContext?.toScopeMap()?.entries?.joinToString("\n") { (key, value) -> "$key = $value" }
            scopeContextString?.let { this[PlsBundle.message("title.scopeContext")] = scopeContextString }
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

