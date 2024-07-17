package icu.windea.pls.model.expression.complex

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.*
import icu.windea.pls.model.expression.complex.nodes.*

/**
 * 值字段表达式。对应的CWT规则类型为[CwtDataTypeGroups.ValueField]。
 *
 * 语法：
 *
 * ```bnf
 * value_field_expression ::= scope * value_field
 * scope ::= system_link | scope_link | scope_link_from_data
 * system_link ::= TOKEN //predefined by CWT Config (see system_links.cwt)
 * scope_link ::= TOKEN //predefined by CWT Config (see links.cwt)
 * scope_link_from_data ::= scope_link_prefix scope_link_data_source //predefined by CWT Config (see links.cwt)
 * scope_link_prefix ::= TOKEN //e.g. "event_target:" while the link's prefix is "event_target:"
 * scope_link_data_source ::= expression //e.g. "some_variable" while the link's data source is "value[variable]"
 * value_field ::= value_link | value_link_from_data
 * value_link ::= TOKEN //predefined by CWT Config (see links.cwt)
 * value_link_from_data ::= value_link_prefix value_link_data_source //predefined by CWT Config (see links.cwt)
 * value_link_prefix ::= TOKEN //e.g. "value:" while the link's prefix is "value:"
 * value_link_data_source ::= expression //e.g. "some" while the link's data source is "value[variable]"
 * expression ::= data_expression | dynamic_value_expression | sv_expression //see: ParadoxDataExpression, ParadoxDynamicValueExpression
 * sv_expression ::= sv_name ("|" (param_name "|" param_value "|")+)? //e.g. value:some_sv|PARAM1|VALUE1|PARAM2|VALUE2|
 * ```
 *
 * 示例：
 *
 * * `trigger:some_trigger`
 * * `value:some_sv|PARAM1|VALUE1|PARAM2|VALUE2|`
 * * `root.owner.some_variable`
 */
class ParadoxValueFieldExpression private constructor(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpression.Base() {
    val scopeNodes: List<ParadoxScopeFieldNode>
        get() = nodes.filterIsInstance<ParadoxScopeFieldNode>()
    val valueFieldNode: ParadoxValueFieldNode
        get() = nodes.last().cast()
    val scriptValueExpression: ParadoxScriptValueExpression?
        get() = this.valueFieldNode.castOrNull<ParadoxValueLinkFromDataNode>()
            ?.dataSourceNode?.nodes?.findIsInstance<ParadoxScriptValueExpression>()
    
    override fun validate(): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        var malformed = false
        for((i, node) in nodes.withIndex()) {
            val isLast = i == nodes.lastIndex
            when(node) {
                is ParadoxScopeFieldNode -> {
                    if(node.text.isEmpty()) {
                        if(!malformed) {
                            malformed = true
                        }
                    } else {
                        if(node is ParadoxScopeLinkFromDataNode) {
                            val dataSourceNode = node.dataSourceNode
                            for(dataSourceChildNode in dataSourceNode.nodes) {
                                when(dataSourceChildNode) {
                                    is ParadoxDataSourceNode -> {
                                        if(dataSourceChildNode.text.isEmpty()) {
                                            if(!malformed) {
                                                malformed = true
                                            }
                                        } else if(!malformed && !isValid(dataSourceChildNode)) {
                                            malformed = true
                                        }
                                    }
                                    is ParadoxScopeFieldNode -> {
                                        if(dataSourceChildNode.text.isEmpty()) {
                                            if(isLast) {
                                                val error = ParadoxComplexExpressionErrors.missingScopeField(rangeInExpression)
                                                errors.add(error)
                                            } else if(!malformed) {
                                                malformed = true
                                            }
                                        } else {
                                            if(dataSourceChildNode is ParadoxScopeLinkFromDataNode) {
                                                val nestedDataSourceNode = dataSourceChildNode.dataSourceNode
                                                for(nestedDataSourceChildNode in nestedDataSourceNode.nodes) {
                                                    when(nestedDataSourceChildNode) {
                                                        is ParadoxDataSourceNode -> {
                                                            if(nestedDataSourceChildNode.text.isEmpty()) {
                                                                if(isLast) {
                                                                    val expect = nestedDataSourceChildNode.linkConfigs.mapNotNullTo(mutableSetOf()) { it.expression }.joinToString()
                                                                    val error = ParadoxComplexExpressionErrors.missingScopeLinkDataSource(rangeInExpression, expect)
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
                                                        is ParadoxErrorTokenNode -> {
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
                                    is ParadoxErrorTokenNode -> {
                                        malformed = true
                                    }
                                }
                            }
                        }
                    }
                }
                is ParadoxValueFieldNode -> {
                    if(node.text.isEmpty()) {
                        if(isLast) {
                            val error = ParadoxComplexExpressionErrors.missingValueField(rangeInExpression)
                            errors.add(error)
                        } else if(!malformed) {
                            malformed = true
                        }
                    } else {
                        if(node is ParadoxValueLinkFromDataNode) {
                            val dataSourceNode = node.dataSourceNode
                            for(dataSourceChildNode in dataSourceNode.nodes) {
                                when(dataSourceChildNode) {
                                    is ParadoxDataSourceNode -> {
                                        if(dataSourceChildNode.text.isEmpty()) {
                                            if(isLast) {
                                                val expect = dataSourceChildNode.linkConfigs.mapNotNullTo(mutableSetOf()) { it.expression }.joinToString()
                                                val error = ParadoxComplexExpressionErrors.missingValueLinkDataSource(rangeInExpression, expect)
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
                                    is ParadoxErrorTokenNode -> {
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
            val error = ParadoxComplexExpressionErrors.malformedValueFieldExpression(rangeInExpression, text)
            errors.add(0, error)
        }
        return errors
    }
    
    private fun isValid(node: ParadoxComplexExpressionNode): Boolean {
        return node.text.isParameterAwareIdentifier()
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val contextElement = context.contextElement!!
        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val scopeContext = context.scopeContext ?: ParadoxScopeHandler.getAnyScopeContext()
        val isKey = context.isKey
        
        context.isKey = null
        
        val offset = context.offsetInParent!! - context.expressionOffset
        if(offset < 0) return //unexpected
        var scopeContextInExpression = scopeContext
        for((i, node) in nodes.withIndex()) {
            val inRange = offset >= node.rangeInExpression.startOffset && offset <= node.rangeInExpression.endOffset
            if(!inRange) {
                //如果光标位置之前存在无法解析的scope（除非被解析为scopeLinkFromData，例如，"event_target:xxx"），不要进行补全
                if(node is ParadoxErrorNode || node.text.isEmpty()) break
            }
            if(node is ParadoxScopeFieldNode) {
                if(inRange) {
                    context.scopeContext = scopeContextInExpression
                    completeForScopeNode(node, context, result)
                    break
                } else {
                    val inExpression = i != 0
                    scopeContextInExpression = ParadoxScopeHandler.getScopeContext(contextElement, node, scopeContextInExpression, inExpression)
                }
            } else if(node is ParadoxValueFieldNode) {
                if(inRange) {
                    context.scopeContext = scopeContextInExpression
                    val scopeNode = ParadoxScopeFieldNode.resolve(node.text, node.rangeInExpression, configGroup)
                    val afterPrefix = completeForScopeNode(scopeNode, context, result)
                    if(afterPrefix) break
                    completeForValueNode(node, context, result)
                    break
                }
            }
        }
        
        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.scopeContext = scopeContext
        context.isKey = isKey
    }
    
    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup): ParadoxValueFieldExpression? {
            if(expressionString.isEmpty()) return null
            
            //skip if text is a number
            if(isNumber(expressionString)) return null
            
            val parameterRanges = expressionString.getParameterRanges()
            
            //skip if text is a parameter with unary operator prefix
            if(ParadoxExpressionHandler.isUnaryOperatorAwareParameter(expressionString, parameterRanges)) return null
            
            val incomplete = PlsStatus.incompleteComplexExpression.get() ?: false
            
            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            val offset = range.startOffset
            var isLast = false
            var index: Int
            var tokenIndex = -1
            var startIndex = 0
            val textLength = expressionString.length
            while(tokenIndex < textLength) {
                index = tokenIndex + 1
                tokenIndex = expressionString.indexOf('.', index)
                if(tokenIndex != -1 && parameterRanges.any { tokenIndex in it }) continue //这里需要跳过参数文本
                if(tokenIndex != -1 && expressionString.indexOf('@', index).let { it != -1 && it < tokenIndex && !parameterRanges.any { it in it } }) tokenIndex = -1
                if(tokenIndex != -1 && expressionString.indexOf('|', index).let { it != -1 && it < tokenIndex && !parameterRanges.any { it in it } }) tokenIndex = -1
                val dotNode = if(tokenIndex != -1) {
                    val dotRange = TextRange.create(tokenIndex + offset, tokenIndex + 1 + offset)
                    ParadoxOperatorNode(".", dotRange)
                } else {
                    null
                }
                if(tokenIndex == -1) {
                    tokenIndex = textLength
                    isLast = true
                }
                //resolve node
                val nodeText = expressionString.substring(startIndex, tokenIndex)
                val nodeTextRange = TextRange.create(startIndex + offset, tokenIndex + offset)
                startIndex = tokenIndex + 1
                val node = when {
                    isLast -> ParadoxValueFieldNode.resolve(nodeText, nodeTextRange, configGroup)
                    else -> ParadoxScopeFieldNode.resolve(nodeText, nodeTextRange, configGroup)
                }
                //handle mismatch situation
                if(!incomplete && nodes.isEmpty() && node is ParadoxErrorNode) return null
                nodes.add(node)
                if(dotNode != null) nodes.add(dotNode)
            }
            return ParadoxValueFieldExpression(expressionString, range, nodes, configGroup)
        }
        
        private fun isNumber(text: String): Boolean {
            return ParadoxDataExpression.resolve(text).type.let { it == ParadoxType.Int || it == ParadoxType.Float }
        }
    }
}
