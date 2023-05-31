package icu.windea.pls.core.codeInsight

import com.intellij.lang.*
import com.intellij.openapi.util.text.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

//com.intellij.codeInsight.hint.JavaTypeProvider

/**
 * 用于显示类型信息（`View > Type Info`）。
 */
class ParadoxTypeProvider : ExpressionTypeProvider<ParadoxTypedElement>() {
    val sectionColor = Gray.get(0x90)
    
    override fun getExpressionsAt(elementAt: PsiElement): List<ParadoxTypedElement> {
        val expressionElement = elementAt.parentOfType<ParadoxTypedElement>() ?: return emptyList()
        if(expressionElement is ParadoxScriptPropertyKey) {
            val property = expressionElement.parent.castOrNull<ParadoxScriptProperty>()
            if(property != null) return listOf(expressionElement, property)
        }
        return listOf(expressionElement)
    }
    
    override fun getErrorHint(): String {
        return PlsBundle.message("no.expression.found")
    }
    
    override fun hasAdvancedInformation(): Boolean {
        return true
    }
    
    /**
     * 显示定义的类型，或者对应的CWT规则表达式（如果存在），或者数据类型。
     */
    override fun getInformationHint(element: ParadoxTypedElement): String {
        //优先显示最相关的类型
        element.definitionType?.let { return it.escapeXml() }
        element.configExpression?.let { return it.escapeXml() }
        element.type?.let { return it.text.escapeXml() }
        return ParadoxType.Unknown.text
    }
    
    /**
     * 显示定义的类型、数据类型、脚本表达式、对应的CWT规则表达式（如果存在）、作用域上下文信息（如果支持）。
     */
    override fun getAdvancedInformationHint(element: ParadoxTypedElement): String {
        val children = buildList {
            element.definitionType?.let { type ->
                add(makeHtmlRow(PlsBundle.message("title.definitionType"), type))
            }
            element.type?.let { expressionType ->
                add(makeHtmlRow(PlsBundle.message("title.type"), expressionType.text))
            }
            element.expression?.let { expression ->
                add(makeHtmlRow(PlsBundle.message("title.expression"), expression))
            }
            element.configExpression?.let { configExpression ->
                add(makeHtmlRow(PlsBundle.message("title.configExpression"), configExpression))
            }
            //inferred config expression
            run {
                val parameterElement = when {
                    element is ParadoxScriptValue && element.isPropertyValue() -> {
                        val propertyKey = element.propertyKey ?: return@run
                        val propertyConfig = ParadoxConfigResolver.getConfigs(propertyKey, true, true, ParadoxConfigMatcher.Options.Default).firstOrNull() ?: return@run
                        ParadoxParameterSupport.resolveArgument(propertyKey, null, propertyConfig) ?: return@run
                    }
                    element is ParadoxParameter -> {
                        ParadoxParameterSupport.resolveParameter(element) ?: return@run
                    }
                    element is ParadoxConditionParameter -> {
                        ParadoxParameterSupport.resolveConditionParameter(element) ?: return@run
                    }
                    else -> return@run
                }
                val inferredConfig = ParadoxParameterHandler.inferConfig(parameterElement) ?: return@run
                add(makeHtmlRow(PlsBundle.message("title.inferredConfigExpression"), inferredConfig.expression.expressionString))
            }
            //scope context
            run {
                val memberElement = getMemberElement(element)
                if(memberElement != null && ParadoxScopeHandler.isScopeContextSupported(memberElement, indirect = true)) {
                    val scopeContext = ParadoxScopeHandler.getScopeContext(memberElement) ?: return@run
                    val text = scopeContext.detailMap.entries.joinToString("\n") { (key, value) -> "$key = $value" }
                    add(makeHtmlRow(PlsBundle.message("title.scopeContext"), text))
                }
            }
            run {
                if(element is ParadoxLocalisationCommandIdentifier) {
                    val scopeContext = ParadoxScopeHandler.getScopeContext(element)
                    if(scopeContext == null) return@run
                    val text = scopeContext.detailMap.entries.joinToString("\n") { (key, value) -> "$key = $value" }
                    add(makeHtmlRow(PlsBundle.message("title.scopeContext"), text))
                }
            }
        }
        return HtmlChunk.tag("table").children(children).toString()
    }
    
    private val ParadoxTypedElement.definitionType: String?
        get() {
            val definition = when {
                this is ParadoxScriptProperty -> this
                this is ParadoxScriptPropertyKey -> this.parent.castOrNull<ParadoxScriptProperty>()
                else -> null
            } ?: return null
            val definitionInfo = definition.definitionInfo ?: return null
            return definitionInfo.typesText
        }
    
    private fun getMemberElement(element: ParadoxTypedElement): ParadoxScriptMemberElement? {
        return when {
            element is ParadoxScriptProperty -> element
            element is ParadoxScriptPropertyKey -> element.parent as? ParadoxScriptProperty
            element is ParadoxScriptValue -> element
            else -> null
        }
    }
    
    private fun makeHtmlRow(titleText: String, contentText: String): HtmlChunk {
        val titleCell = HtmlChunk.tag("td")
            .attr("align", "left").attr("valign", "top")
            .style("color:" + ColorUtil.toHtmlColor(sectionColor))
            .addText("$titleText:")
        val contentCell = HtmlChunk.tag("td").addText(contentText)
        return HtmlChunk.tag("tr").children(titleCell, contentCell)
    }
}

