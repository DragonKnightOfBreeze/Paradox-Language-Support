package icu.windea.pls.lang.codeInsight

import com.intellij.lang.ExpressionTypeProvider
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.psi.PsiElement
import com.intellij.ui.ColorUtil.toHtmlColor
import com.intellij.ui.Gray
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.escapeXml
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxType
import icu.windea.pls.model.scope.toScopeMap
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey

// com.intellij.codeInsight.hint.JavaTypeProvider

/**
 * 用于显示各种类型信息（`View > Type Info`）。
 *
 * - 名字 - 如果 PSI 表示一个封装变量、定义、本地化或参数则可用。
 * - 表达式 - 如果 PSI 表示一个表达式则可用。
 * - 基本类型 - 基于 PSI 的类型。
 * - 定义类型 - 如果 PSI 是 [ParadoxScriptPropertyKey] 则可用。
 * - 本地化类型 - 如果 PSI 是 [ParadoxLocalisationProperty] 或 [ParadoxLocalisationParameter] 则可用。
 * - 规则表达式 - 如果存在对应的规则表达式则可用。
 * - 作用域上下文信息 - 如果存在则可用。
 * 覆盖方式 - 仅限（全局）封装变量、（作为脚本属性的）定义、本地化。
 */
class ParadoxTypeProvider : ExpressionTypeProvider<PsiElement>() {
    override fun getExpressionsAt(elementAt: PsiElement): List<PsiElement> {
        return ParadoxTypeManager.findTypedElements(elementAt)
    }

    /**
     * 优先显示最相关的类型信息（定义类型，本地化类型、规则表达式，或者基本类型）。
     */
    override fun getInformationHint(element: PsiElement): String {
        ParadoxTypeManager.getDefinitionType(element)?.let { return it.escapeXml() }
        ParadoxTypeManager.getLocalisationType(element)?.let { return it.id }
        ParadoxTypeManager.getConfigExpression(element)?.let { return it.escapeXml() }
        ParadoxTypeManager.getType(element)?.let { return it.id }
        return ParadoxType.Unknown.id
    }

    override fun getErrorHint(): String {
        return PlsBundle.message("no.expression.found")
    }

    override fun hasAdvancedInformation(): Boolean {
        return true
    }

    override fun getAdvancedInformationHint(element: PsiElement): String {
        val map = buildMap {
            val name = ParadoxTypeManager.getName(element)
            name?.let { this[PlsBundle.message("title.name")] = it }

            val expression = ParadoxTypeManager.getExpression(element)
            expression?.let { this[PlsBundle.message("title.expression")] = it }

            val type = ParadoxTypeManager.getType(element)
            type?.let { this[PlsBundle.message("title.type")] = it.id }

            val definitionType = ParadoxTypeManager.getDefinitionType(element)
            definitionType?.let { this[PlsBundle.message("title.definitionType")] = it }

            val localisationType = ParadoxTypeManager.getLocalisationType(element)
            localisationType?.let { this[PlsBundle.message("title.localisationType")] = it.id }

            val configExpression = ParadoxTypeManager.getConfigExpression(element)
            configExpression?.let { this[PlsBundle.message("title.configExpression")] = it }

            val scopeContext = ParadoxTypeManager.getScopeContext(element)
            val scopeContextString = scopeContext?.toScopeMap()?.entries?.joinToString("\n") { (key, value) -> "$key = $value" }
            scopeContextString?.let { this[PlsBundle.message("title.scopeContext")] = it }

            val priority = ParadoxTypeManager.getPriority(element)
            priority?.let { this[PlsBundle.message("title.overrideStrategy")] = it.toString() }
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

