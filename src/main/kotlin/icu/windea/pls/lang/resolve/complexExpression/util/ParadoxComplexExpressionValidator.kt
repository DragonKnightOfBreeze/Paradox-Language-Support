package icu.windea.pls.lang.resolve.complexExpression.util

import com.intellij.util.Processor
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.lang.isIdentifier
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDatabaseObjectExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDefineReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDynamicValueExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxLinkedExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScriptValueExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.StellarisNameFormatExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDatabaseObjectDataNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDefineNamespaceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDefineVariableNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorTokenNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxLinkPrefixNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxLinkValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueArgumentNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueArgumentValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxTokenNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNameFormatDefinitionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNameFormatLocalisationNode
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionErrorBuilder as ErrorBuilder

@Suppress("unused")
object ParadoxComplexExpressionValidator {
    fun validate(expression: ParadoxDynamicValueExpression, element: ParadoxExpressionElement? = null): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        val result = validateAllNodes(expression, errors) {
            when {
                it is ParadoxDynamicValueNode -> it.text.isParameterAwareIdentifier(".") // 兼容点号
                else -> true
            }
        }
        val malformed = !result
        if (malformed) errors += ErrorBuilder.malformedDynamicValueExpression(expression.rangeInExpression, expression.text)
        return errors
    }

    fun validate(expression: ParadoxScopeFieldExpression, element: ParadoxExpressionElement? = null): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        val result = validateAllNodes(expression, errors) {
            when {
                it is ParadoxDataSourceNode -> it.text.isParameterAwareIdentifier()
                else -> true
            }
        }
        val malformed = !result
        if (malformed) errors += ErrorBuilder.malformedScopeFieldExpression(expression.rangeInExpression, expression.text)
        processLinkedExpression(expression, element, errors)
        return errors
    }

    fun validate(expression: ParadoxValueFieldExpression, element: ParadoxExpressionElement? = null): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        val result = validateAllNodes(expression, errors) {
            when {
                it is ParadoxDataSourceNode -> it.text.isParameterAwareIdentifier()
                else -> true
            }
        }
        val malformed = !result
        if (malformed) errors += ErrorBuilder.malformedValueFieldExpression(expression.rangeInExpression, expression.text)
        processLinkedExpression(expression, element, errors)
        return errors
    }

    fun validate(expression: ParadoxVariableFieldExpression, element: ParadoxExpressionElement? = null): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        val result = validateAllNodes(expression, errors) {
            when {
                it is ParadoxDataSourceNode -> it.text.isParameterAwareIdentifier()
                else -> true
            }
        }
        val malformed = !result
        if (malformed) errors += ErrorBuilder.malformedVariableFieldExpression(expression.rangeInExpression, expression.text)
        processLinkedExpression(expression, element, errors)
        return errors
    }

    fun validate(expression: ParadoxScriptValueExpression, element: ParadoxExpressionElement? = null): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        val result = validateAllNodes(expression, errors) {
            when {
                it is ParadoxScriptValueNode -> it.text.isParameterAwareIdentifier()
                it is ParadoxScriptValueArgumentNode -> it.text.isIdentifier()
                it is ParadoxScriptValueArgumentValueNode -> true
                else -> true
            }
        }
        var malformed = !result
        if (!malformed) {
            // check whether pipe count is valid
            val pipeNodeCount = expression.nodes.count { it is ParadoxTokenNode && it.text == "|" }
            if (pipeNodeCount == 1 || (pipeNodeCount != 0 && pipeNodeCount % 2 == 0)) {
                malformed = true
            }
        }
        if (malformed) errors += ErrorBuilder.malformedScriptValueExpression(expression.rangeInExpression, expression.text)
        return errors
    }

    fun validate(expression: ParadoxDatabaseObjectExpression, element: ParadoxExpressionElement? = null): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        val config = expression.typeNode?.config
        val result = validateAllNodes(expression, errors) {
            when {
                it is ParadoxDatabaseObjectDataNode -> {
                    when {
                        config?.localisation != null -> it.text.isParameterAwareIdentifier(".-'")
                        else -> it.text.isParameterAwareIdentifier()
                    }
                }
                else -> true
            }
        }
        val malformed = !result || (expression.nodes.size != 3 && expression.nodes.size != 5)
        if (malformed) errors += ErrorBuilder.malformedDatabaseObjectExpression(expression.rangeInExpression, expression.text)
        return errors
    }

    fun validate(expression: ParadoxDefineReferenceExpression, element: ParadoxExpressionElement? = null): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        val result = validateAllNodes(expression, errors) {
            when {
                it is ParadoxDefineNamespaceNode -> it.text.isParameterAwareIdentifier()
                it is ParadoxDefineVariableNode -> it.text.isParameterAwareIdentifier()
                else -> true
            }
        }
        val malformed = !result || expression.nodes.size != 4
        if (malformed) errors += ErrorBuilder.malformedDefineReferenceExpression(expression.rangeInExpression, expression.text)
        return errors
    }

    fun validate(expression: StellarisNameFormatExpression, element: ParadoxExpressionElement? = null): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        val result = validateAllNodes(expression, errors) {
            when (it) {
                is StellarisNameFormatDefinitionNode -> it.text.isParameterAwareIdentifier()
                is StellarisNameFormatLocalisationNode -> it.text.isParameterAwareIdentifier(".-'")
                else -> true
            }
        }
        val malformed = !result
        if (malformed) errors += ErrorBuilder.malformedStellarisNameFormatExpression(expression.rangeInExpression, expression.text)
        return errors
    }

    fun validate(expression: ParadoxCommandExpression, element: ParadoxExpressionElement? = null): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        val result = validateAllNodes(expression, errors) {
            when {
                it is ParadoxDataSourceNode -> it.text.isParameterAwareIdentifier()
                else -> true
            }
        }
        val malformed = !result
        if (malformed) errors += ErrorBuilder.malformedCommandExpression(expression.rangeInExpression, expression.text)
        processLinkedExpression(expression, element, errors)
        return errors
    }

    private fun validateAllNodes(expression: ParadoxComplexExpression, errors: MutableList<ParadoxComplexExpressionError>, processor: Processor<ParadoxComplexExpressionNode>): Boolean {
        var result = true
        expression.accept(object : ParadoxComplexExpressionVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                if (node is ParadoxComplexExpression && node !== expression) {
                    errors += node.getErrors()
                    return true
                }
                if (node is ParadoxErrorTokenNode || node.text.isEmpty()) {
                    result = false
                }
                if (result) result = processor.process(node)
                return super.visit(node)
            }
        })
        return result
    }

    private fun processLinkedExpression(expression: ParadoxLinkedExpression, element: ParadoxExpressionElement?, errors: MutableList<ParadoxComplexExpressionError>) {
        // 如果链式表达式使用包含前缀的嵌套的链式表达式和前缀作为其传参，则需要用双引号括起
        checkQuotes(element, expression, errors)
    }

    private fun checkQuotes(
        element: ParadoxExpressionElement?,
        expression: ParadoxLinkedExpression,
        errors: MutableList<ParadoxComplexExpressionError>
    ) {
        if (expression is ParadoxCommandExpression) return // 2.1.1 目前直接跳过
        if (element == null || !element.text.let { !it.isLeftQuoted() || !it.isRightQuoted() }) return
        val r = expression.linkNodes.process p@{ linkNode ->
            val prefixNode = linkNode.nodes.getOrNull(0)?.takeIf { it is ParadoxLinkPrefixNode }
            if (prefixNode == null) return@p true
            val leftParNode = linkNode.nodes.getOrNull(1)?.takeIf { it is ParadoxMarkerNode && it.text == "(" }
            if (leftParNode == null) return@p true
            val valueNode = linkNode.nodes.getOrNull(2)?.takeIf { it is ParadoxLinkValueNode }
            if (valueNode == null) return@p true
            ":" !in valueNode.text
        }
        if (!r) errors += ErrorBuilder.notQuotedNestedWithPrefixes(expression.rangeInExpression)
    }
}
