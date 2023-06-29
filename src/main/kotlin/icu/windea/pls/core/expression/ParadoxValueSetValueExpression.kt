package icu.windea.pls.core.expression

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.ParadoxValueSetValueExpression.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*

/**
 * 值集值表达式。
 *
 * 语法：
 *
 * ```bnf
 * value_set_value_expression ::= value_set_value ("@" scope_field_expression)?
 * value_set_value ::= TOKEN //matching config expression "value[xxx]" or "value_set[xxx]"
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
interface ParadoxValueSetValueExpression : ParadoxComplexExpression {
    val configs: List<CwtConfig<*>>
    
    val valueSetValueNode: ParadoxValueSetValueExpressionNode
    val scopeFieldExpression: ParadoxScopeFieldExpression?
    
    companion object Resolver
}

class ParadoxValueSetValueExpressionImpl(
    override val text: String,
    override val isKey: Boolean?,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxExpressionNode>,
    override val configs: List<CwtConfig<*>>,
    override val configGroup: CwtConfigGroup
) : AbstractExpression(text), ParadoxValueSetValueExpression {
    override val quoted: Boolean = false
    
    override val valueSetValueNode: ParadoxValueSetValueExpressionNode = nodes.get(0).cast()
    override val scopeFieldExpression: ParadoxScopeFieldExpression? = nodes.getOrNull(2)?.cast()
    
    override fun validate(): List<ParadoxExpressionError> {
        val errors = mutableListOf<ParadoxExpressionError>()
        var malformed = false
        for(node in nodes) {
            when(node) {
                is ParadoxValueSetValueExpressionNode -> {
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
            val error = ParadoxMalformedValueSetValueExpressionExpressionError(rangeInExpression, PlsBundle.message("script.expression.malformedValueSetValueExpression", text))
            errors.add(0, error)
        }
        return errors
    }
    
    private fun isValid(node: ParadoxExpressionNode): Boolean {
        return when(node) {
            is ParadoxValueSetValueExpressionNode -> node.text.isExactParameterAwareIdentifier('.') //兼容点号
            else -> node.text.isExactParameterAwareIdentifier()
        }
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val keyword = context.keyword
        val startOffset = context.startOffset
        val isKey = context.isKey
        val config = context.config
        val configs = context.configs
        val scopeContext = context.scopeContext ?: ParadoxScopeHandler.getAnyScopeContext()
        
        context.config = this.configs.first()
        context.configs = this.configs
        context.scopeContext = null //don't check now
        context.isKey = null
        
        val offsetInParent = context.offsetInParent
        for(node in nodes) {
            val nodeRange = node.rangeInExpression
            val inRange = offsetInParent >= nodeRange.startOffset && offsetInParent <= nodeRange.endOffset
            if(node is ParadoxValueSetValueExpressionNode) {
                if(inRange) {
                    val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.startOffset = node.rangeInExpression.startOffset
                    ParadoxConfigHandler.completeValueSetValue(context, resultToUse)
                    break
                }
            } else if(node is ParadoxScopeFieldExpression) {
                if(inRange) {
                    val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.startOffset = node.rangeInExpression.startOffset
                    node.complete(context, resultToUse)
                    break
                }
            }
        }
        
        context.keyword = keyword
        context.startOffset = startOffset
        context.isKey = isKey
        context.config = config
        context.configs = configs
        context.scopeContext = scopeContext
    }
}

fun Resolver.resolve(text: String, textRange: TextRange, config: CwtConfig<*>, configGroup: CwtConfigGroup, isKey: Boolean? = null, canBeMismatched: Boolean = false): ParadoxValueSetValueExpression? {
    return resolve(text, textRange, config.toSingletonList(), configGroup, isKey, canBeMismatched)
}

fun Resolver.resolve(text: String, textRange: TextRange, configs: List<CwtConfig<*>>, configGroup: CwtConfigGroup, isKey: Boolean? = null, canBeMismatched: Boolean = false): ParadoxValueSetValueExpression? {
    val parameterRanges = ParadoxConfigHandler.getParameterRangesInExpression(text)
    
    val nodes = mutableListOf<ParadoxExpressionNode>()
    val offset = textRange.startOffset
    var index: Int
    var atIndex = -1
    val textLength = text.length
    while(atIndex < textLength) {
        index = atIndex + 1
        atIndex = text.indexOf('@', index)
        if(atIndex != -1 && parameterRanges.any { it.contains(atIndex) }) continue //这里需要跳过参数文本
        if(atIndex == -1) {
            atIndex = textLength
        }
        //resolve valueSetValueNode
        val nodeText = text.substring(0, atIndex)
        val nodeTextRange = TextRange.create(offset, atIndex + offset)
        val node = ParadoxValueSetValueExpressionNode.resolve(nodeText, nodeTextRange, configs, configGroup)
        if(node == null) return null //unexpected
        nodes.add(node)
        if(atIndex != textLength) {
            //resolve at token
            val atNode = ParadoxMarkerExpressionNode("@", TextRange.create(atIndex + offset, atIndex + 1 + offset))
            nodes.add(atNode)
            //resolve scope expression
            val expText = text.substring(atIndex + 1)
            val expTextRange = TextRange.create(atIndex + 1 + offset, textLength + offset)
            val expNode = ParadoxScopeFieldExpression.resolve(expText, expTextRange, configGroup, null, true)!!
            nodes.add(expNode)
        }
        break
    }
    if(!canBeMismatched && nodes.isEmpty()) return null
    return ParadoxValueSetValueExpressionImpl(text, isKey, textRange, nodes, configs, configGroup)
}
