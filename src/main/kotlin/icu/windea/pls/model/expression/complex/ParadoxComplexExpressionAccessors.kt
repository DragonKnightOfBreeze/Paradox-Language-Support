package icu.windea.pls.model.expression.complex

import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.model.expression.complex.nodes.*

//val ParadoxComplexExpressionNode.parentExpression: ParadoxComplexExpression
//    get() {
//        var current = this
//        while(true) {
//            if(current is ParadoxComplexExpression) return current
//            current = current.parentNode ?: throw IllegalStateException()
//        }
//    }
//val ParadoxComplexExpressionNode.configGroup: CwtConfigGroup
//    get() = parentExpression.configGroup

val ParadoxDynamicValueExpression.scopeFieldExpression: ParadoxScopeFieldExpression?
    get() = nodes.getOrNull(2)?.cast()
val ParadoxDynamicValueExpression.dynamicValueNode: ParadoxDynamicValueNode
    get() = nodes.get(0).cast()

val ParadoxScopeFieldExpression.scopeNodes: List<ParadoxScopeFieldNode>
    get() = nodes.filterIsInstance<ParadoxScopeFieldNode>()

val ParadoxScriptValueExpression.scriptValueNode: ParadoxScriptValueNode
    get() = nodes.first().cast()
val ParadoxScriptValueExpression.argumentNodes: List<Tuple2<ParadoxScriptValueArgumentNode, ParadoxScriptValueArgumentValueNode?>>
    get() = buildList {
        var argumentNode: ParadoxScriptValueArgumentNode? = null
        for(node in nodes) {
            if(node is ParadoxScriptValueArgumentNode) {
                argumentNode = node
            } else if(node is ParadoxScriptValueArgumentValueNode && argumentNode != null) {
                add(tupleOf(argumentNode, node))
                argumentNode = null
            }
        }
        if(argumentNode != null) {
            add(tupleOf(argumentNode, null))
        }
    }

val ParadoxValueFieldExpression.scopeNodes: List<ParadoxScopeFieldNode>
    get() = nodes.filterIsInstance<ParadoxScopeFieldNode>()
val ParadoxValueFieldExpression.valueFieldNode: ParadoxValueFieldNode
    get() = nodes.last().cast()
val ParadoxValueFieldExpression.scriptValueExpression: ParadoxScriptValueExpression?
    get() = this.valueFieldNode.castOrNull<ParadoxValueLinkFromDataNode>()
        ?.dataSourceNode?.nodes?.findIsInstance<ParadoxScriptValueExpression>()

val ParadoxVariableFieldExpression.scopeNodes: List<ParadoxScopeFieldNode>
    get() = nodes.filterIsInstance<ParadoxScopeFieldNode>()
val ParadoxVariableFieldExpression.variableNode: ParadoxDataSourceNode
    get() = nodes.last().cast()