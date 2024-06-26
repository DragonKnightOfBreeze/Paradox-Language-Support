package icu.windea.pls.model.expression.complex

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.expression.complex.nodes.*
import icu.windea.pls.script.psi.*

/**
 * 封装值表达式。作为[ParadoxValueFieldExpression]的一部分。
 *
 * 语法：
 *
 * ```bnf
 * script_value_expression ::= script_value ("|" (param_name "|" param_value "|")+)?
 * script_value ::= TOKEN //matching config expression "<script_value>"
 * param_name ::= TOKEN //parameter name, no surrounding "$"
 * param_value ::= TOKEN //boolean, int, float or string
 * ```
 *
 * 示例：
 *
 * * `some_sv`
 * * `some_sv|PARAM|VALUE|`
 */
interface ParadoxScriptValueExpression : ParadoxComplexExpression {
    val config: CwtConfig<*>
    
    companion object Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxScriptValueExpression? =
            doResolve(expressionString, range, configGroup, config)
    }
}

//Implementations

private fun doResolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxScriptValueExpression? {
    if(expressionString.isEmpty()) return null
    
    val parameterRanges = CwtConfigHandler.getParameterRangesInExpression(expressionString)
    
    val incomplete = PlsStatus.incompleteComplexExpression.get() ?: false
    
    val nodes = mutableListOf<ParadoxComplexExpressionNode>()
    val offset = range.startOffset
    var n = 0
    var scriptValueNode: ParadoxScriptValueNode? = null
    var parameterNode: ParadoxScriptValueArgumentNode? = null
    var index: Int
    var tokenIndex = -1
    var startIndex = 0
    val textLength = expressionString.length
    while(tokenIndex < textLength) {
        index = tokenIndex + 1
        tokenIndex = expressionString.indexOf('|', index)
        if(tokenIndex != -1 && CwtConfigHandler.inParameterRanges(parameterRanges, tokenIndex)) continue //这里需要跳过参数文本
        val pipeNode = if(tokenIndex != -1) {
            val pipeRange = TextRange.create(tokenIndex + offset, tokenIndex + 1 + offset)
            ParadoxMarkerNode("|", pipeRange)
        } else {
            null
        }
        if(tokenIndex == -1) {
            tokenIndex = textLength
        }
        if(!incomplete && index == tokenIndex && tokenIndex == textLength) break
        //resolve node
        val nodeText = expressionString.substring(startIndex, tokenIndex)
        val nodeRange = TextRange.create(startIndex + offset, tokenIndex + offset)
        startIndex = tokenIndex + 1
        val node = when {
            n == 0 -> {
                ParadoxScriptValueNode.resolve(nodeText, nodeRange, config, configGroup)
                    .also { scriptValueNode = it }
            }
            n % 2 == 1 -> {
                ParadoxScriptValueArgumentNode.resolve(nodeText, nodeRange, scriptValueNode, configGroup)
                    .also { parameterNode = it }
            }
            n % 2 == 0 -> {
                ParadoxScriptValueArgumentValueNode.resolve(nodeText, nodeRange, scriptValueNode, parameterNode, configGroup)
            }
            else -> throw InternalError()
        }
        nodes.add(node)
        if(pipeNode != null) nodes.add(pipeNode)
        n++
    }
    return ParadoxScriptValueExpressionImpl(expressionString, range, nodes, configGroup, config)
}

private class ParadoxScriptValueExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup,
    override val config: CwtConfig<*>
) : ParadoxScriptValueExpression {
    override fun validate(): List<ParadoxComplexExpressionError> {
        var malformed = false
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        var pipeCount = 0
        var lastIsParameter = false
        for(node in nodes) {
            if(node is ParadoxTokenNode) {
                pipeCount++
            } else {
                if(!malformed && (node.text.isEmpty() || !isValid(node))) {
                    malformed = true
                }
                when(node) {
                    is ParadoxScriptValueArgumentNode -> lastIsParameter = true
                    is ParadoxScriptValueArgumentValueNode -> lastIsParameter = false
                }
            }
        }
        //0, 1, 3, 5, ...
        if(!malformed && pipeCount != 0 && pipeCount % 2 == 0) {
            malformed = true
        }
        if(malformed) {
            val error = ParadoxComplexExpressionErrors.malformedScriptValueExpression(rangeInExpression, text)
            errors.add(error)
        }
        if(lastIsParameter) {
            val error = ParadoxComplexExpressionErrors.missingParameterValue(rangeInExpression)
            errors.add(error)
        }
        return errors
    }
    
    private fun isValid(node: ParadoxComplexExpressionNode): Boolean {
        return when(node) {
            is ParadoxScriptValueArgumentNode -> node.text.isExactIdentifier()
            is ParadoxScriptValueArgumentValueNode -> true //兼容数字文本、字符串文本、封装变量引用等，这里直接返回true
            else -> node.text.isExactParameterAwareIdentifier()
        }
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val keyword = context.keyword
        val keywordOffset = context.keywordOffset
        val offsetInParent = context.offsetInParent!!
        val isKey = context.isKey
        val scopeContext = context.scopeContext ?: ParadoxScopeHandler.getAnyScopeContext()
        val scopeMatched = context.scopeMatched
        
        context.scopeContext = null //don't check now
        
        for(node in nodes) {
            val nodeRange = node.rangeInExpression
            val inRange = offsetInParent >= nodeRange.startOffset && offsetInParent <= nodeRange.endOffset
            if(node is ParadoxScriptValueNode) {
                if(inRange) {
                    val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    val config = context.config
                    val configs = context.configs
                    context.config = this.config
                    context.configs = emptyList()
                    ParadoxCompletionManager.completeScriptExpression(context, resultToUse)
                    context.config = config
                    context.configs = configs
                }
            } else if(node is ParadoxScriptValueArgumentNode) {
                if(inRange && scriptValueNode.text.isNotEmpty()) {
                    val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
                    val resultToUse = result.withPrefixMatcher(keywordToUse)
                    context.keyword = keywordToUse
                    context.keywordOffset = node.rangeInExpression.startOffset
                    ParadoxParameterHandler.completeArguments(context.contextElement!!, context, resultToUse)
                }
            } else if(node is ParadoxScriptValueArgumentValueNode && getSettings().inference.configContextForParameters) {
                if(inRange && scriptValueNode.text.isNotEmpty()) {
                    //尝试提示传入参数的值
                    run {
                        val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
                        val resultToUse = result.withPrefixMatcher(keywordToUse)
                        val element = context.contextElement as? ParadoxScriptStringExpressionElement ?: return@run
                        val parameterElement = node.argumentNode?.getReference(element)?.resolve() ?: return@run
                        val inferredContextConfigs = ParadoxParameterHandler.getInferredContextConfigs(parameterElement)
                        val inferredConfig = inferredContextConfigs.singleOrNull()?.castOrNull<CwtValueConfig>() ?: return@run
                        val config = context.config
                        val configs = context.configs
                        context.keyword = keywordToUse
                        context.keywordOffset = node.rangeInExpression.startOffset
                        context.config = inferredConfig
                        context.configs = emptyList()
                        ParadoxCompletionManager.completeScriptExpression(context, resultToUse)
                        context.config = config
                        context.configs = configs
                    }
                }
            }
        }
        context.keyword = keyword
        context.keywordOffset = keywordOffset
        context.isKey = isKey
        context.scopeContext = scopeContext
        context.scopeMatched = scopeMatched
    }
    
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxScriptValueExpression && text == other.text
    }
    
    override fun hashCode(): Int {
        return text.hashCode()
    }
    
    override fun toString(): String {
        return text
    }
}
