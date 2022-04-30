package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.documentation.*
import com.intellij.lang.*
import com.intellij.openapi.util.text.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

//com.intellij.codeInsight.hint.JavaTypeProvider

/**
 * 脚本文件的类型提供器。用于显示类型信息（`View > Type Info`）。
 *
 * 支持的PSI元素：
 * * 变量（variable） - 显示变量的值的类型。
 * * 属性（property） - 显示定义的类型（如果是定义），或者属性的值的类型。
 * * 值（value） - 显示定义元素的类型（如果是定义元素），或者值的类型。
 */
class ParadoxScriptTypeProvider : ExpressionTypeProvider<ParadoxScriptExpression>() {
	override fun getExpressionsAt(elementAt: PsiElement): List<ParadoxScriptExpression> {
		//for: variable, property, value
		val element = elementAt.parentOfTypes(ParadoxScriptExpression::class)
		return element.toSingletonListOrEmpty()
	}
	
	override fun getErrorHint(): String {
		return PlsBundle.message("noExpressionFound")
	}
	
	/**
	 * 显示定义的类型，或者定义属性的类型，或者值的类型。
	 */
	override fun getInformationHint(element: ParadoxScriptExpression): String {
		//优先显示最相关的类型
		return element.type ?: element.valueType?.text ?: ParadoxValueType.UnknownType.text
	}
	
	override fun hasAdvancedInformation(): Boolean {
		return true
	}
	
	/**
	 * 显示定义的类型（或者定义属性的类型）、值的类型。
	 */
	override fun getAdvancedInformationHint(element: ParadoxScriptExpression): String {
		val children = buildList {
			element.type?.let { type ->
				add(makeHtmlRow(PlsBundle.message("title.type"), type))
			}
			element.valueType?.let { valueType ->
				add(makeHtmlRow(PlsBundle.message("title.valueType"), valueType.text))
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

