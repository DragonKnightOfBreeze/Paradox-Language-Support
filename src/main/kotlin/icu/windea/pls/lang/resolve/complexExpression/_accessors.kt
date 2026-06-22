@file:Suppress("unused")

package icu.windea.pls.lang.resolve.complexExpression

import icu.windea.pls.core.cast
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.lang.resolve.complexExpression.nodes.*

val ParadoxCommandExpression.scopeNodes: List<ParadoxCommandScopeNode>
    get() = nodes.filterIsInstance<ParadoxCommandScopeNode>()
val ParadoxCommandExpression.fieldNode: ParadoxCommandFieldNode
    get() = nodes.last().cast()

val ParadoxDatabaseObjectExpression.typeNode: ParadoxDatabaseObjectTypeNode?
    get() = nodes.getOrNull(0)?.castOrNull()
val ParadoxDatabaseObjectExpression.valueNode: ParadoxDatabaseObjectNode?
    get() = nodes.getOrNull(2)?.castOrNull()

val ParadoxDefineReferenceExpression.namespaceNode: ParadoxDefineNamespaceNode?
    get() = nodes.getOrNull(1)?.cast()
val ParadoxDefineReferenceExpression.variableNode: ParadoxDefineVariableNode?
    get() = nodes.getOrNull(3)?.cast()

val ParadoxDynamicValueExpression.dynamicValueNode: ParadoxDynamicValueNode
    get() = nodes.get(0).cast()
val ParadoxDynamicValueExpression.nestedScopeFieldExpression: ParadoxScopeFieldExpression?
    get() = nodes.getOrNull(2)?.cast()

val ParadoxLinkedExpression.linkNodes: List<ParadoxLinkNode>
    get() = nodes.filterIsInstance<ParadoxLinkNode>()

val ParadoxScopeFieldExpression.scopeNodes: List<ParadoxScopeNode>
    get() = nodes.filterIsInstance<ParadoxScopeNode>()

val ParadoxScriptValueReferenceExpression.scriptValueNode: ParadoxScriptValueNode
    get() = nodes.first().cast()
val ParadoxScriptValueReferenceExpression.argumentNodes: List<Pair<ParadoxScriptValueArgumentNode, ParadoxScriptValueArgumentValueNode?>>
    get() = buildList {
        var argumentNode: ParadoxScriptValueArgumentNode? = null
        for (node in nodes) {
            if (node is ParadoxScriptValueArgumentNode) {
                argumentNode = node
            } else if (node is ParadoxScriptValueArgumentValueNode && argumentNode != null) {
                add(tupleOf(argumentNode, node))
                argumentNode = null
            }
        }
        if (argumentNode != null) {
            add(tupleOf(argumentNode, null))
        }
    }

val ParadoxValueFieldExpression.scopeNodes: List<ParadoxScopeNode>
    get() = nodes.filterIsInstance<ParadoxScopeNode>()
val ParadoxValueFieldExpression.fieldNode: ParadoxValueFieldNode
    get() = nodes.last().cast()
val ParadoxValueFieldExpression.nestedScriptValueReferenceExpression: ParadoxScriptValueReferenceExpression?
    get() = fieldNode.castOrNull<ParadoxDynamicValueFieldNode>()?.valueNode?.nodes?.findIsInstance<ParadoxScriptValueReferenceExpression>()

val ParadoxVariableFieldExpression.scopeNodes: List<ParadoxScopeNode>
    get() = nodes.filterIsInstance<ParadoxScopeNode>()
val ParadoxVariableFieldExpression.variableNode: ParadoxDataSourceNode
    get() = nodes.last().cast()
