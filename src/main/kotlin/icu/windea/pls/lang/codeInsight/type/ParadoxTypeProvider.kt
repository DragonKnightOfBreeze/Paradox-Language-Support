package icu.windea.pls.lang.codeInsight.type

import com.intellij.lang.ExpressionTypeProvider
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.psi.PsiElement
import com.intellij.ui.ColorUtil.*
import com.intellij.ui.Gray
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.util.values.FallbackStrings
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.lang.type.ParadoxTypeService

/**
 * 用于显示各种类型信息（`View > Type Info`）。
 *
 * - 类型 - 如果表示一个表达式、封装变量、封装变量引用、内联数学数字等则可用。
 * - 名字 - 如果表示一个封装变量、封装变量引用、定义、定值变量、本地化等则可用。
 * - 定义类型 - 如果表示一个定义候选（定义、定义注入、定义模板）则可用。
 * - 本地化类型 - 如果表示一个本地化属性或本地化参数则可用。
 * - 表达式 - 如果表示一个表达式则可用。
 * - 规则表达式 - 如果存在对应的规则表达式则可用。
 * - 覆盖策略 - 仅限（全局）封装变量、（作为脚本属性的）定义、本地化。
 * - 作用域上下文信息 - 如果存在则可用。
 */
class ParadoxTypeProvider : ExpressionTypeProvider<PsiElement>() {
    // com.intellij.codeInsight.hint.JavaTypeProvider

    override fun getExpressionsAt(elementAt: PsiElement): List<PsiElement> {
        return ParadoxTypeService.findTypedElements(elementAt)
    }

    /**
     * 优先显示最相关的类型信息（定义类型，本地化类型、规则表达式，或者基本类型）。
     */
    override fun getInformationHint(element: PsiElement): String {
        ParadoxTypeService.getDefinitionType(element)?.let { return it.escapeXml() }
        ParadoxTypeService.getLocalisationType(element)?.let { return it.id }
        ParadoxTypeService.getConfigExpression(element)?.let { return it.escapeXml() }
        ParadoxTypeService.getType(element)?.let { return it.text }
        return FallbackStrings.unknown
    }

    override fun getErrorHint(): String {
        return ChronicleBundle.message("no.expression.found")
    }

    override fun hasAdvancedInformation(): Boolean {
        return true
    }

    override fun getAdvancedInformationHint(element: PsiElement): String {
        val map = buildMap {
            val type = ParadoxTypeService.getType(element)
            type?.let { this[ChronicleBundle.message("title.type")] = it.text }

            val name = ParadoxTypeService.getName(element)
            name?.let { this[ChronicleBundle.message("title.name")] = it.or.anonymous() }

            val definitionType = ParadoxTypeService.getDefinitionType(element)
            definitionType?.let { this[ChronicleBundle.message("title.definitionType")] = it }

            val localisationType = ParadoxTypeService.getLocalisationType(element)
            localisationType?.let { this[ChronicleBundle.message("title.localisationType")] = it.id }

            val expression = ParadoxTypeService.getExpression(element)
            expression?.let { this[ChronicleBundle.message("title.expression")] = it }

            val configExpression = ParadoxTypeService.getConfigExpression(element)
            configExpression?.let { this[ChronicleBundle.message("title.configExpression")] = it }

            val priority = ParadoxTypeService.getOverrideStrategy(element)
            priority?.let { this[ChronicleBundle.message("title.overrideStrategy")] = it.toString() }

            val scopeContext = ParadoxTypeService.getScopeContext(element)
            val scopeContextString = scopeContext?.toScopeMap()?.entries?.joinToString("\n") { (key, value) -> "$key = $value" }
            scopeContextString?.let { this[ChronicleBundle.message("title.scopeContext")] = it }
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

