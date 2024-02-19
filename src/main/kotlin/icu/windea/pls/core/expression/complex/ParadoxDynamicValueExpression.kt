package icu.windea.pls.core.expression.complex

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.complex.errors.*
import icu.windea.pls.core.expression.complex.nodes.*
import icu.windea.pls.lang.*

/**
 * 值集值表达式。
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
 * ```
 * some_variable
 * some_variable@root
 * ```
 */
interface ParadoxDynamicValueExpression : ParadoxComplexExpression {
    val configs: List<CwtConfig<*>>
    
    companion object Resolver {
        fun resolve(expression: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxDynamicValueExpression? =
            doResolve(expression, range, configGroup, config.toSingletonList())
        
        fun resolve(expression: String, range: TextRange, configGroup: CwtConfigGroup, configs: List<CwtConfig<*>>): ParadoxDynamicValueExpression? =
            doResolve(expression, range, configGroup, configs)
    }
}

val ParadoxDynamicValueExpression.scopeFieldExpression: ParadoxScopeFieldExpression?
    get() = nodes.getOrNull(2)?.cast()
val ParadoxDynamicValueExpression.dynamicValueNode: ParadoxDynamicValueExpressionNode
    get() = nodes.get(0).cast()

//Resolve Methods

private fun doResolve(expression: String, range: TextRange, configGroup: CwtConfigGroup, configs: List<CwtConfig<*>>): ParadoxDynamicValueExpression? {
    val parameterRanges = CwtConfigHandler.getParameterRangesInExpression(expression)
    //skip if text is a parameter with unary operator prefix
    if(CwtConfigHandler.isUnaryOperatorAwareParameter(expression, parameterRanges)) return null
    
    val incomplete = PlsContext.incompleteComplexExpression.get() ?: false
    
    val nodes = mutableListOf<ParadoxExpressionNode>()
    val offset = range.startOffset
    var index: Int
    var tokenIndex = -1
    val textLength = expression.length
    while(tokenIndex < textLength) {
        index = tokenIndex + 1
        tokenIndex = expression.indexOf('@', index)
        if(tokenIndex != -1 && CwtConfigHandler.inParameterRanges(parameterRanges, tokenIndex)) continue //这里需要跳过参数文本
        if(tokenIndex == -1) {
            tokenIndex = textLength
        }
        //resolve dynamicValueNode
        val nodeText = expression.substring(0, tokenIndex)
        val nodeTextRange = TextRange.create(offset, tokenIndex + offset)
        val node = ParadoxDynamicValueExpressionNode.resolve(nodeText, nodeTextRange, configs, configGroup)
        if(node == null) return null //unexpected
        nodes.add(node)
        if(tokenIndex != textLength) {
            //resolve at token
            val atNode = ParadoxMarkerExpressionNode("@", TextRange.create(tokenIndex + offset, tokenIndex + 1 + offset))
            nodes.add(atNode)
            //resolve scope expression
            val expText = expression.substring(tokenIndex + 1)
            val expTextRange = TextRange.create(tokenIndex + 1 + offset, textLength + offset)
            val expNode = ParadoxScopeFieldExpression.resolve(expText, expTextRange, configGroup)!!
            nodes.add(expNode)
        }
        break
    }
    //handle mismatch situation
    if(!incomplete && nodes.isEmpty()) return null
    return ParadoxDynamicValueExpressionImpl(expression, range, nodes, configGroup, configs)
}

//Implementations

private class ParadoxDynamicValueExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxExpressionNode>,
    override val configGroup: CwtConfigGroup,
    override val configs: List<CwtConfig<*>>
) : ParadoxDynamicValueExpression {
    override fun validate(): List<ParadoxExpressionError> {
        val errors = mutableListOf<ParadoxExpressionError>()
        var malformed = false
        for(node in nodes) {
            when(node) {
                is ParadoxDynamicValueExpressionNode -> {
                    if(!malformed && !isValid(node)) {
                        malformed = true
                    }
                }
                is ParadoxScopeFieldExpression -> {
                    if(node.text.isEmpty()) {
                        val error = ParadoxMissingScopeFieldExpressionExpressionError(rangeInExpression, PlsBundle.message("script.expression.missingScopeFieldExpression"))
                        errors.add(error)
                    }
                    errors.addAll(node.validate())
                }
            }
        }
        if(malformed) {
            val error = ParadoxMalformedDynamicValueExpressionExpressionError(rangeInExpression, PlsBundle.message("script.expression.malformedDynamicValueExpression", text))
            errors.add(0, error)
        }
        return errors
    }
    
    private fun isValid(node: ParadoxExpressionNode): Boolean {
        return when(node) {
            is ParadoxDynamicValueExpressionNode -> node.text.isExactParameterAwareIdentifier('.') //兼容点号
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
            if(node is ParadoxDynamicValueExpressionNode) {
                if(inRange) {
                    val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    CwtConfigHandler.completeDynamicValue(context, resultToUse)
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


