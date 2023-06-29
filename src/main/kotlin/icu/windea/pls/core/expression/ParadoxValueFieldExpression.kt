package icu.windea.pls.core.expression

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.ParadoxValueFieldExpression.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.model.*

/**
 * 值字段表达式。
 *
 * 语法：
 *
 * ```bnf
 * value_field_expression ::= scope * value_field
 * scope ::= system_link | scope_link | scope_link_from_data
 * system_link ::= TOKEN //predefined by CWT Config (in system_links.pls.cwt)
 * scope_link ::= TOKEN //predefined by CWT Config (in links.cwt)
 * scope_link_from_data ::= scope_link_prefix scope_link_data_source //predefined by CWT Config (in links.cwt)
 * scope_link_prefix ::= TOKEN //e.g. "event_target:" while the link's prefix is "event_target:"
 * scope_link_data_source ::= expression //e.g. "some_variable" while the link's data source is "value[variable]"
 * value_field ::= value_link | value_link_from_data
 * value_link ::= TOKEN //predefined by CWT Config (in links.cwt, from_data = false, type = both | value)
 * value_link_from_data ::= value_link_prefix value_link_data_source //predefined by CWT Config (in links.cwt, from_data = true, type = both | value)
 * value_link_prefix ::= TOKEN //e.g. "value:" while the link's prefix is "value:"
 * value_link_data_source ::= expression //e.g. "some" while the link's data source is "value[variable]"
 * expression ::= data_expression | value_set_value_expression | sv_expression //see: ParadoxDataExpression, ParadoxValueSetValueExpression
 * sv_expression ::= sv_name ("|" (param_name "|" param_value "|")+)? //e.g. value:some_sv|PARAM1|VALUE1|PARAM2|VALUE2|
 * ```
 *
 * 示例：
 *
 * ```
 * trigger:some_trigger
 * value:some_sv|PARAM1|VALUE1|PARAM2|VALUE2|
 * root.owner.some_variable
 * ```
 */
interface ParadoxValueFieldExpression : ParadoxComplexExpression {
    val scopeNodes: List<ParadoxScopeFieldExpressionNode>
    
    val valueFieldNode: ParadoxValueFieldExpressionNode
    
    companion object Resolver
}

val ParadoxValueFieldExpression.scriptValueExpression
    get() = this.valueFieldNode.castOrNull<ParadoxValueLinkFromDataExpressionNode>()
        ?.dataSourceNode?.nodes?.findIsInstance<ParadoxScriptValueExpression>()

class ParadoxValueFieldExpressionImpl(
    override val text: String,
    override val isKey: Boolean?,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxExpressionNode>,
    override val configGroup: CwtConfigGroup
) : AbstractExpression(text), ParadoxValueFieldExpression {
    override val quoted: Boolean = false
    
    override val scopeNodes: List<ParadoxScopeFieldExpressionNode> = nodes.filterIsInstance<ParadoxScopeFieldExpressionNode>()
    
    override val valueFieldNode: ParadoxValueFieldExpressionNode = nodes.last().cast()
    
    override fun validate(): List<ParadoxExpressionError> {
        val errors = mutableListOf<ParadoxExpressionError>()
        var malformed = false
        for((index, node) in nodes.withIndex()) {
            val isLast = index == nodes.lastIndex
            when(node) {
                is ParadoxScopeFieldExpressionNode -> {
                    if(node.text.isEmpty()) {
                        if(!malformed) {
                            malformed = true
                        }
                    } else {
                        if(node is ParadoxScopeLinkFromDataExpressionNode) {
                            val dataSourceNode = node.dataSourceNode
                            for(dataSourceChildNode in dataSourceNode.nodes) {
                                when(dataSourceChildNode) {
                                    is ParadoxDataExpressionNode -> {
                                        if(dataSourceChildNode.text.isEmpty()) {
                                            if(!malformed) {
                                                malformed = true
                                            }
                                        } else if(!malformed && !isValid(dataSourceChildNode)) {
                                            malformed = true
                                        }
                                    }
                                    is ParadoxScopeFieldExpressionNode -> {
                                        if(dataSourceChildNode.text.isEmpty()) {
                                            if(isLast) {
                                                val error = ParadoxMissingScopeExpressionError(rangeInExpression, PlsBundle.message("script.expression.missingScope"))
                                                errors.add(error)
                                            } else if(!malformed) {
                                                malformed = true
                                            }
                                        } else {
                                            if(dataSourceChildNode is ParadoxScopeLinkFromDataExpressionNode) {
                                                val nestedDataSourceNode = dataSourceChildNode.dataSourceNode
                                                for(nestedDataSourceChildNode in nestedDataSourceNode.nodes) {
                                                    when(nestedDataSourceChildNode) {
                                                        is ParadoxDataExpressionNode -> {
                                                            if(nestedDataSourceChildNode.text.isEmpty()) {
                                                                if(isLast) {
                                                                    val expect = nestedDataSourceChildNode.linkConfigs.mapNotNullTo(mutableSetOf()) { it.expression }.joinToString()
                                                                    val error = ParadoxMissingScopeLinkDataSourceExpressionError(rangeInExpression, PlsBundle.message("script.expression.missingScopeLinkDataSource", expect))
                                                                    errors.add(error)
                                                                } else if(!malformed) {
                                                                    malformed = true
                                                                }
                                                            } else if(!malformed && !isValid(nestedDataSourceChildNode)) {
                                                                malformed = true
                                                            }
                                                        }
                                                        is ParadoxComplexExpression -> {
                                                            errors.addAll(nestedDataSourceChildNode.validate())
                                                        }
                                                        is ParadoxErrorTokenExpressionNode -> {
                                                            malformed = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    is ParadoxComplexExpression -> {
                                        errors.addAll(dataSourceChildNode.validate())
                                    }
                                    is ParadoxErrorTokenExpressionNode -> {
                                        malformed = true
                                    }
                                }
                            }
                        }
                    }
                }
                is ParadoxValueFieldExpressionNode -> {
                    if(node.text.isEmpty()) {
                        if(isLast) {
                            val error = ParadoxMissingValueFieldExpressionError(rangeInExpression, PlsBundle.message("script.expression.missingValueField"))
                            errors.add(error)
                        } else if(!malformed) {
                            malformed = true
                        }
                    } else {
                        if(node is ParadoxValueLinkFromDataExpressionNode) {
                            val dataSourceNode = node.dataSourceNode
                            for(dataSourceChildNode in dataSourceNode.nodes) {
                                when(dataSourceChildNode) {
                                    is ParadoxDataExpressionNode -> {
                                        if(dataSourceChildNode.text.isEmpty()) {
                                            if(isLast) {
                                                val expect = dataSourceChildNode.linkConfigs.mapNotNullTo(mutableSetOf()) { it.expression }.joinToString()
                                                val error = ParadoxMissingValueLinkDataSourceExpressionError(rangeInExpression, PlsBundle.message("script.expression.missingValueLinkDataSource", expect))
                                                errors.add(error)
                                            } else if(!malformed) {
                                                malformed = true
                                            }
                                        } else if(!malformed && !isValid(dataSourceChildNode)) {
                                            malformed = true
                                        }
                                    }
                                    is ParadoxComplexExpression -> {
                                        errors.addAll(dataSourceChildNode.validate())
                                    }
                                    is ParadoxErrorTokenExpressionNode -> {
                                        malformed = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if(malformed) {
            val error = ParadoxMalformedValueFieldExpressionExpressionError(rangeInExpression, PlsBundle.message("script.expression.malformedValueFieldExpression", text))
            errors.add(0, error)
        }
        return errors
    }
    
    private fun isValid(node: ParadoxExpressionNode): Boolean {
        return node.text.isExactParameterAwareIdentifier()
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val keyword = context.keyword
        val startOffset = context.startOffset
        val offsetInParent = context.offsetInParent
        val isKey = context.isKey
        val scopeContext = context.scopeContext ?: ParadoxScopeHandler.getAnyScopeContext()
        
        context.isKey = null
        
        var scopeContextInExpression = scopeContext
        for((i, node) in nodes.withIndex()) {
            val nodeRange = node.rangeInExpression
            val inRange = offsetInParent >= nodeRange.startOffset && offsetInParent <= nodeRange.endOffset
            if(!inRange) {
                //如果光标位置之前存在无法解析的scope（除非被解析为scopeLinkFromData，例如，"event_target:xxx"），不要进行补全
                if(node is ParadoxErrorExpressionNode || node.text.isEmpty()) break
            }
            if(node is ParadoxScopeFieldExpressionNode) {
                if(inRange) {
					context.scopeContext = scopeContextInExpression
                    completeForScopeExpressionNode(node, context, result)
                    break
                } else {
                    val inExpression = i != 0
                    scopeContextInExpression = ParadoxScopeHandler.getScopeContext(node, scopeContextInExpression, inExpression)
                }
            } else if(node is ParadoxValueFieldExpressionNode) {
                if(inRange) {
                    context.scopeContext = scopeContextInExpression
                    val scopeExpressionNode = ParadoxScopeFieldExpressionNode.resolve(node.text, node.rangeInExpression, configGroup)
                    val afterPrefix = completeForScopeExpressionNode(scopeExpressionNode, context, result)
                    if(afterPrefix) break
                    completeForValueExpressionNode(node, context, result)
                    break
                }
            }
        }
        
        context.keyword = keyword
        context.startOffset = startOffset
        context.isKey = isKey
        context.scopeContext = scopeContext
    }
}

fun Resolver.resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, isKey: Boolean? = null, canBeMismatched: Boolean = false): ParadoxValueFieldExpression? {
    //skip if text represents an int or float
    if(isNumber(text)) return null
    
    val parameterRanges = ParadoxConfigHandler.getParameterRangesInExpression(text)
    //skip if text is a parameter with unary operator prefix
    if(isUnaryOperatorAwareParameter(text, parameterRanges)) return null
    
    val nodes = mutableListOf<ParadoxExpressionNode>()
    val offset = textRange.startOffset
    var isLast = false
    var index: Int
    var dotIndex = -1
    val textLength = text.length
    while(dotIndex < textLength) {
        index = dotIndex + 1
        dotIndex = text.indexOf('.', index)
        if(dotIndex != -1 && parameterRanges.any { it.contains(dotIndex) }) continue //这里需要跳过参数文本
        if(dotIndex != -1 && text.indexOf('@', index).let { it != -1 && dotIndex > it }) dotIndex = -1
        if(dotIndex != -1 && text.indexOf('|', index).let { it != -1 && dotIndex > it }) dotIndex = -1
        val dotNode = if(dotIndex != -1) {
            val dotRange = TextRange.create(dotIndex + offset, dotIndex + 1 + offset)
            ParadoxOperatorExpressionNode(".", dotRange)
        } else {
            null
        }
        if(dotIndex == -1) {
            dotIndex = textLength
            isLast = true
        }
        //resolve node
        val nodeText = text.substring(index, dotIndex)
        val nodeTextRange = TextRange.create(index + offset, dotIndex + offset)
        val node = when {
            isLast -> ParadoxValueFieldExpressionNode.resolve(nodeText, nodeTextRange, configGroup)
            else -> ParadoxScopeFieldExpressionNode.resolve(nodeText, nodeTextRange, configGroup)
        }
        //handle mismatch situation
        if(!canBeMismatched && index == 0 && node is ParadoxErrorExpressionNode) {
            return null
        }
        nodes.add(node)
        if(dotNode != null) nodes.add(dotNode)
    }
    return ParadoxValueFieldExpressionImpl(text, isKey, textRange, nodes, configGroup)
}

private fun isNumber(text: String): Boolean {
    return ParadoxDataExpression.resolve(text).type.let { it == ParadoxType.Int || it == ParadoxType.Float }
}

private fun isUnaryOperatorAwareParameter(text: String, parameterRanges: List<TextRange>): Boolean {
    return text.firstOrNull()?.let { it == '+' || it == '-' } == true
        && parameterRanges.singleOrNull()?.let { it.startOffset == 1 && it.endOffset == text.length } == true
}
