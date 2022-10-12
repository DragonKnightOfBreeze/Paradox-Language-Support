package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.documentation.*
import com.intellij.lang.*
import com.intellij.openapi.util.text.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

//com.intellij.codeInsight.hint.JavaTypeProvider

/**
 * 脚本文件的类型提供器。用于显示类型信息（`View > Type Info`）。
 *
 * 支持的PSI元素：
 * * 变量（variable） - 显示变量的值类型。
 * * 属性（property） - 显示定义的类型（如果是定义），或者定义元素的规则表达式（如果是定义元素），或者属性的值类型。
 * * 键（key） - 显示定义元素的规则表达式（如果是定义元素），或者值类型。
 * * 值（value） - 显示定义元素的规则表达式（如果是定义元素），或者值类型。
 * * 内联数学表达式的操作数（inline_math_factor） - 显示数字、变量引用或参数的值类型。
 */
class ParadoxScriptTypeProvider : ExpressionTypeProvider<ParadoxScriptTypedElement>() {
	override fun getExpressionsAt(elementAt: PsiElement): List<ParadoxScriptTypedElement> {
		//如果最接近的expressionElement是propertyKey，需要判断作为父节点的property是否是定义
		//如果是，直接返回property，否则返回propertyKey+property
		val expressionElement = elementAt.parentOfType<ParadoxScriptTypedElement>() ?: return emptyList()
		if(expressionElement is ParadoxScriptPropertyKey) {
			val property = expressionElement.parent.castOrNull<ParadoxScriptProperty>()
			if(property != null) {
				if(property.definitionInfo != null) return listOf(property)
				return listOf(expressionElement, property)
			}
		}
		return listOf(expressionElement)
	}
	
	override fun getErrorHint(): String {
		return PlsBundle.message("no.expression.found")
	}
	
	/**
	 * 显示定义的类型，或者定义属性的类型，或者值的类型。
	 */
	override fun getInformationHint(element: ParadoxScriptTypedElement): String {
		//优先显示最相关的类型
		val typeToShow = (element.definitionType
			?: element.configExpression
			?: element.valueType?.text
			?: ParadoxValueType.UnknownType.text)
		return typeToShow.escapeXml()
	}
	
	override fun hasAdvancedInformation(): Boolean {
		return true
	}
	
	/**
	 * 显示定义的类型（或者定义属性的类型）、值的类型。
	 */
	override fun getAdvancedInformationHint(element: ParadoxScriptTypedElement): String {
		val children = buildList {
			element.definitionType?.let { type ->
				add(makeHtmlRow(PlsDocBundle.message("title.definitionType"), type))
			}
			element.valueType?.let { valueType ->
				add(makeHtmlRow(PlsDocBundle.message("title.valueType"), valueType.text))
			}
			element.configExpression?.let { configExpression ->
				add(makeHtmlRow(PlsDocBundle.message("title.configExpression"), configExpression))
			}
		}
		return HtmlChunk.tag("table").children(children).toString()
	}
	
	private fun makeHtmlRow(titleText: String, contentText: String): HtmlChunk {
		val titleCell: HtmlChunk = HtmlChunk.tag("td")
			.attr("align", "left").attr("valign", "top")
			.style("color:" + ColorUtil.toHtmlColor(DocumentationComponent.SECTION_COLOR))
			.addText("$titleText:")
		val contentCell: HtmlChunk = HtmlChunk.tag("td").addText(contentText)
		return HtmlChunk.tag("tr").children(titleCell, contentCell)
	}
}

