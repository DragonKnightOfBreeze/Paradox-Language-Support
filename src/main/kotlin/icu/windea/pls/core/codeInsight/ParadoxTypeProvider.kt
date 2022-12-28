package icu.windea.pls.core.codeInsight

import com.intellij.lang.*
import com.intellij.openapi.util.text.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.config.script.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.psi.*
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
		return ParadoxDataType.UnknownType.text
	}
	
	/**
	 * 显示定义的类型、数据类型、脚本表达式、对应的CWT规则表达式（如果存在）、作用域上下文信息（如果支持）。
	 */
	override fun getAdvancedInformationHint(element: ParadoxTypedElement): String {
		val children = buildList {
			element.definitionType?.let { type ->
				add(makeHtmlRow(PlsDocBundle.message("title.definitionType"), type))
			}
			element.type?.let { expressionType ->
				add(makeHtmlRow(PlsDocBundle.message("title.type"), expressionType.text))
			}
			element.expression?.let { expression ->
				add(makeHtmlRow(PlsDocBundle.message("title.expression"), expression))
			}
			element.configExpression?.let { configExpression ->
				add(makeHtmlRow(PlsDocBundle.message("title.configExpression"), configExpression))
			}
			val memberElement = getMemberElement(element)
			if(memberElement != null && ScopeConfigHandler.isScopeContextSupported(memberElement)) {
				val scopeContext = ScopeConfigHandler.getScopeContext(memberElement)
				if(scopeContext != null) {
					val text = scopeContext.map.entries.joinToString("\n") { (key, value) -> "$key = $value" }
					add(makeHtmlRow(PlsDocBundle.message("title.scopeContext"), text))
				}
			}
			if(element is ParadoxLocalisationCommandIdentifier) {
				val scopeContext = ScopeConfigHandler.getScopeContext(element)
				if(scopeContext != null) {
					val text = scopeContext.map.entries.joinToString("\n") { (key, value) -> "$key = $value" }
					add(makeHtmlRow(PlsDocBundle.message("title.scopeContext"), text))
				}
			}
		}
		return HtmlChunk.tag("table").children(children).toString()
	}
	
	private val ParadoxTypedElement.definitionType: String?
		get() = when {
			this is ParadoxScriptProperty -> this.definitionInfo?.typesText
			this is ParadoxScriptPropertyKey -> this.parent.castOrNull<ParadoxScriptProperty>()?.definitionInfo?.typesText
			else -> null
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

