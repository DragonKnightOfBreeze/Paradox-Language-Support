package icu.windea.pls.core.codeInsight

import com.intellij.lang.*
import com.intellij.openapi.util.text.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*

//com.intellij.codeInsight.hint.JavaTypeProvider

/**
 * 用于显示类型信息（`View > Type Info`）。
 */
class ParadoxTypeProvider : ExpressionTypeProvider<ParadoxTypedElement>() {
	val sectionColor = Gray.get(0x90)
	
	private val ParadoxTypedElement.definitionType: String?
		get() = this.castOrNull<ParadoxScriptProperty>()?.definitionInfo?.typesText
	
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
	 * 显示定义的类型，或者对应的CWT规则表达式，或者数据类型。
	 */
	override fun getInformationHint(element: ParadoxTypedElement): String {
		//优先显示最相关的类型
		element.definitionType?.let { return it.escapeXml() }
		element.configExpression?.let { return it.escapeXml() }
		element.type?.let { return it.text.escapeXml() }
		return ParadoxDataType.UnknownType.text
	}
	
	/**
	 * 显示定义的类型（或者定义属性的类型）、数据类型、脚本表达式、CWT规则表达式等（如果存在）。
	 * 如果对应的CWT规则匹配，也显示枚举名、值集名等。
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
			if(element is ParadoxScriptExpressionElement) {
				var addEnumName = false
				if(element is ParadoxScriptStringExpressionElement) {
					val complexEnumValueInfo = element.complexEnumValueInfo
					if(complexEnumValueInfo != null) {
						addEnumName = true
						add(makeHtmlRow(PlsDocBundle.message("title.complexEnumName"), complexEnumValueInfo.name))
					}
				}
				val configs = ParadoxCwtConfigHandler.resolveConfigs(element)
				val config = configs.firstOrNull()
				if(config != null) {
					val expression = config.expression
					when(expression.type) {
						CwtDataTypes.Enum -> {
							if(!addEnumName) {
								val configGroup = config.info.configGroup
								val enumName = expression.value.orEmpty()
								if(configGroup.complexEnums.keys.contains(enumName)) {
									add(makeHtmlRow(PlsDocBundle.message("title.complexEnumName"), enumName))
								} else {
									add(makeHtmlRow(PlsDocBundle.message("title.enumName"), enumName))
								}
							}
						}
						CwtDataTypes.Value, CwtDataTypes.ValueSet -> {
							val valueSetName = expression.value.orEmpty()
							add(makeHtmlRow(PlsDocBundle.message("title.valueSetName"), valueSetName))
						}
						else -> pass()
					}
				}
			}
		}
		return HtmlChunk.tag("table").children(children).toString()
	}
	
	private fun makeHtmlRow(titleText: String, contentText: String): HtmlChunk {
		val titleCell: HtmlChunk = HtmlChunk.tag("td")
			.attr("align", "left").attr("valign", "top")
			.style("color:" + ColorUtil.toHtmlColor(sectionColor))
			.addText("$titleText:")
		val contentCell: HtmlChunk = HtmlChunk.tag("td").addText(contentText)
		return HtmlChunk.tag("tr").children(titleCell, contentCell)
	}
}

