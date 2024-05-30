package icu.windea.pls.model.expression.complex

import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.model.expression.complex.nodes.*

val ParadoxDynamicValueExpression.scopeFieldExpression: ParadoxScopeFieldExpression?
    get() = nodes.getOrNull(2)?.cast()
val ParadoxDynamicValueExpression.dynamicValueNode: ParadoxDynamicValueExpressionNode
    get() = nodes.get(0).cast()

val ParadoxScopeFieldExpression.scopeNodes: List<ParadoxScopeFieldExpressionNode>
    get() = nodes.filterIsInstance<ParadoxScopeFieldExpressionNode>()

val ParadoxScriptValueExpression.scriptValueNode: ParadoxScriptValueExpressionNode
    get() = nodes.first().cast()
val ParadoxScriptValueExpression.argumentNodes: List<Tuple2<ParadoxScriptValueArgumentExpressionNode, ParadoxScriptValueArgumentValueExpressionNode?>>
    get() = buildList {
        var argumentNode: ParadoxScriptValueArgumentExpressionNode? = null
        for(node in nodes) {
            if(node is ParadoxScriptValueArgumentExpressionNode) {
                argumentNode = node
            } else if(node is ParadoxScriptValueArgumentValueExpressionNode && argumentNode != null) {
                add(tupleOf(argumentNode, node))
                argumentNode = null
            }
        }
        if(argumentNode != null) {
            add(tupleOf(argumentNode, null))
        }
    }

val ParadoxValueFieldExpression.scopeNodes: List<ParadoxScopeFieldExpressionNode>
    get() = nodes.filterIsInstance<ParadoxScopeFieldExpressionNode>()
val ParadoxValueFieldExpression.valueFieldNode: ParadoxValueFieldExpressionNode
    get() = nodes.last().cast()
val ParadoxValueFieldExpression.scriptValueExpression: ParadoxScriptValueExpression?
    get() = this.valueFieldNode.castOrNull<ParadoxValueLinkFromDataExpressionNode>()
        ?.dataSourceNode?.nodes?.findIsInstance<ParadoxScriptValueExpression>()

val ParadoxVariableFieldExpression.scopeNodes: List<ParadoxScopeFieldExpressionNode>
    get() = nodes.filterIsInstance<ParadoxScopeFieldExpressionNode>()
val ParadoxVariableFieldExpression.variableNode: ParadoxDataExpressionNode
    get() = nodes.last().cast()