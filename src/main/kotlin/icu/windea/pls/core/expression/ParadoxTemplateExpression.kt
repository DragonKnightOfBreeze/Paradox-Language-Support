package icu.windea.pls.core.expression

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.expression.ParadoxTemplateExpression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.script.highlighter.*

/**
 * 模版表达式。
 * 
 * 示例：
 * 
 * ```
 * job_<job>_add -> job_researcher_add
 * ```
 */
interface ParadoxTemplateExpression: ParadoxComplexExpression {
	val referenceNodes: List<ParadoxTemplateSnippetExpressionNode>
	
	val template: CwtTemplateExpression
	companion object Resolver
	
	override fun complete(context: ProcessingContext, result: CompletionResultSet) {
		val scopeMatched = context.scopeMatched ?: true
		val tailText = CwtConfigHandler.getScriptExpressionTailText(context.config)
		CwtConfigHandler.processTemplateResolveResult(template, configGroup) {name ->
			val builder = ParadoxScriptExpressionLookupElementBuilder.create(null, name)
				.withIcon(PlsIcons.Template)
				.withTailText(tailText)
				.caseInsensitive()
				.withScopeMatched(scopeMatched)
			result.addScriptExpressionElement(context, builder)
			true
		}
	}
}

class ParadoxTemplateExpressionImpl(
	override val text: String,
	override val isKey: Boolean?,
	override val rangeInExpression: TextRange,
	override val nodes: List<ParadoxTemplateSnippetExpressionNode>,
	override val configGroup: CwtConfigGroup,
	override val template: CwtTemplateExpression
): AbstractExpression(text), ParadoxTemplateExpression {
	override val quoted: Boolean = text.isLeftQuoted()
	
	override val referenceNodes: List<ParadoxTemplateSnippetExpressionNode> = nodes.filterTo(SmartList()) { it.configExpression != null }
	
	override fun getAttributesKey() = ParadoxScriptAttributesKeys.TEMPLATE_EXPRESSION_KEY
	
}

fun Resolver.resolve(text: String, textRange: TextRange, template: CwtTemplateExpression, configGroup: CwtConfigGroup, isKey: Boolean? = null): ParadoxTemplateExpression? {
	if(template.isEmpty()) return null
	
	//可以用引号包围
	val snippets = template.snippetExpressions
	if(snippets.isEmpty()) return null
	val nodes = mutableListOf<ParadoxTemplateSnippetExpressionNode>()
	var i1 = 0
	var i2 = 0
	var prevSnippet: CwtDataExpression? = null
	for(snippet in snippets) {
		if(snippet.type == CwtDataType.Constant) {
			val expressionString = snippet.expressionString
			i2 = text.indexOf(expressionString, i1)
			if(i2 == -1) return null
			if(i2 != i1 && prevSnippet != null) {
				val nodeText = text.substring(i1, i2)
				val nodeRange = TextRange.create(i1, i2)
				val node = ParadoxTemplateSnippetExpressionNode(nodeText, nodeRange, prevSnippet, configGroup)
				nodes.add(node)
			}
			i1 = i2 + expressionString.length
			val nodeText = expressionString
			val nodeRange = TextRange.create(i2, i1)
			val node = ParadoxTemplateSnippetExpressionNode(nodeText, nodeRange, null, configGroup)
			nodes.add(node)
		} else {
			prevSnippet = snippet
		}
	}
	if(i2 != i1 && prevSnippet != null) {
		val nodeText = text.substring(i1, i2)
		val nodeRange = TextRange.create(i1, i2)
		val node = ParadoxTemplateSnippetExpressionNode(nodeText, nodeRange, prevSnippet, configGroup)
		nodes.add(node)
	}
	return ParadoxTemplateExpressionImpl(text, isKey, textRange, nodes, configGroup, template)
}