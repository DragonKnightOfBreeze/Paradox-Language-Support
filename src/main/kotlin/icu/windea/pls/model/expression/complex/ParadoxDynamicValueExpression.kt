package icu.windea.pls.model.expression.complex

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.expression.complex.nodes.*

/**
 * 动态值表达式。对应的CWT规则类型为[CwtDataTypeGroups.DynamicValue]。
 *
 * 语法：
 *
 * ```bnf
 * dynamic_value_expression ::= dynamic_value ("@" scope_field_expression)?
 * dynamic_value ::= TOKEN //matching config expression "value[xxx]" or "value_set[xxx]"
 * //"event_target:t1.v1@event_target:t2.v2@..." is not used in vanilla files but allowed here
 * ```
 *
 * 示例：
 *
 * * `some_variable`
 * * `some_variable@root`
 */
interface ParadoxDynamicValueExpression : ParadoxComplexExpression {
    val configs: List<CwtConfig<*>>
    
    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxDynamicValueExpression? =
            doResolve(expressionString, range, configGroup, config.toSingletonList())
        
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, configs: List<CwtConfig<*>>): ParadoxDynamicValueExpression? =
            doResolve(expressionString, range, configGroup, configs)
    }
}

//Implementations

private fun doResolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, configs: List<CwtConfig<*>>): ParadoxDynamicValueExpression? {
    if(expressionString.isEmpty()) return null
    
    val parameterRanges = CwtConfigHandler.getParameterRangesInExpression(expressionString)
    
    val incomplete = PlsStatus.incompleteComplexExpression.get() ?: false
    
    val nodes = mutableListOf<ParadoxComplexExpressionNode>()
    val offset = range.startOffset
    var index: Int
    var tokenIndex = -1
    val textLength = expressionString.length
    while(tokenIndex < textLength) {
        index = tokenIndex + 1
        tokenIndex = expressionString.indexOf('@', index)
        if(tokenIndex != -1 && CwtConfigHandler.inParameterRanges(parameterRanges, tokenIndex)) continue //这里需要跳过参数文本
        if(tokenIndex == -1) {
            tokenIndex = textLength
        }
        //resolve dynamicValueNode
        val nodeText = expressionString.substring(0, tokenIndex)
        val nodeTextRange = TextRange.create(offset, tokenIndex + offset)
        val node = ParadoxDynamicValueNode.resolve(nodeText, nodeTextRange, configs, configGroup)
        if(node == null) return null //unexpected
        nodes.add(node)
        if(tokenIndex != textLength) {
            //resolve at token
            val atNode = ParadoxMarkerNode("@", TextRange.create(tokenIndex + offset, tokenIndex + 1 + offset))
            nodes.add(atNode)
            //resolve scope expression
            val expText = expressionString.substring(tokenIndex + 1)
            val expTextRange = TextRange.create(tokenIndex + 1 + offset, textLength + offset)
            val expNode = ParadoxScopeFieldExpression.resolve(expText, expTextRange, configGroup)!!
            nodes.add(expNode)
        }
        break
    }
    //handle mismatch situation
    if(!incomplete && nodes.isEmpty()) return null
    return ParadoxDynamicValueExpressionImpl(expressionString, range, nodes, configGroup, configs)
}

private class ParadoxDynamicValueExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup,
    override val configs: List<CwtConfig<*>>
) : ParadoxDynamicValueExpression {
    override fun validate(): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        var malformed = false
        for(node in nodes) {
            when(node) {
                is ParadoxDynamicValueNode -> {
                    if(!malformed && !isValid(node)) {
                        malformed = true
                    }
                }
                is ParadoxScopeFieldExpression -> {
                    if(node.text.isEmpty()) {
                        val error = ParadoxComplexExpressionErrors.missingScopeFieldExpression(rangeInExpression)
                        errors.add(error)
                    }
                    errors.addAll(node.validate())
                }
            }
        }
        if(malformed) {
            errors.add(0, ParadoxComplexExpressionErrors.malformedDynamicValueExpression(rangeInExpression, text))
        }
        return errors
    }
    
    private fun isValid(node: ParadoxComplexExpressionNode): Boolean {
        return when(node) {
            is ParadoxDynamicValueNode -> node.text.isExactParameterAwareIdentifier('.') //兼容点号
            else -> node.text.isExactParameterAwareIdentifier()
        }
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val offsetInParent = context.offsetInParent!!
        val isKey = context.isKey
        val config = context.config
        val configs = context.configs
        val scopeContext = context.scopeContext ?: ParadoxScopeHandler.getAnyScopeContext()
        
        context.config = this.configs.first()
        context.configs = this.configs
        context.scopeContext = null //don't check now
        context.isKey = null
        
        for(node in nodes) {
            val nodeRange = node.rangeInExpression
            val inRange = offsetInParent >= nodeRange.startOffset && offsetInParent <= nodeRange.endOffset
            if(node is ParadoxDynamicValueNode) {
                if(inRange) {
                    val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    ParadoxCompletionManager.completeDynamicValue(context, resultToUse)
                    break
                }
            } else if(node is ParadoxScopeFieldExpression) {
                if(inRange) {
                    val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    node.complete(context, resultToUse)
                    break
                }
            }
        }
        
        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.isKey = isKey
        context.config = config
        context.configs = configs
        context.scopeContext = scopeContext
    }
    
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxDynamicValueExpression && text == other.text
    }
    
    override fun hashCode(): Int {
        return text.hashCode()
    }
    
    override fun toString(): String {
        return text
    }
}


