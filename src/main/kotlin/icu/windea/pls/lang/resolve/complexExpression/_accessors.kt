@file:Suppress("unused")

package icu.windea.pls.lang.resolve.complexExpression

import icu.windea.pls.core.cast
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDatabaseObjectNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDatabaseObjectTypeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDefineNamespaceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDefineVariableNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueArgumentNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueArgumentValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxValueFieldNode

val ParadoxCommandExpression.scopeNodes: List<ParadoxCommandScopeLinkNode>
    get() = nodes.filterIsInstance<ParadoxCommandScopeLinkNode>()
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

val ParadoxScopeFieldExpression.scopeNodes: List<ParadoxScopeLinkNode>
    get() = nodes.filterIsInstance<ParadoxScopeLinkNode>()

val ParadoxScriptValueExpression.scriptValueNode: ParadoxScriptValueNode
    get() = nodes.first().cast()
val ParadoxScriptValueExpression.argumentNodes: List<Pair<ParadoxScriptValueArgumentNode, ParadoxScriptValueArgumentValueNode?>>
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

val ParadoxValueFieldExpression.scopeNodes: List<ParadoxScopeLinkNode>
    get() = nodes.filterIsInstance<ParadoxScopeLinkNode>()
val ParadoxValueFieldExpression.fieldNode: ParadoxValueFieldNode
    get() = nodes.last().cast()
val ParadoxValueFieldExpression.nestedScriptValueExpression: ParadoxScriptValueExpression?
    get() = fieldNode.castOrNull<ParadoxDynamicValueFieldNode>()?.valueNode?.nodes?.findIsInstance<ParadoxScriptValueExpression>()

val ParadoxVariableFieldExpression.scopeNodes: List<ParadoxScopeLinkNode>
    get() = nodes.filterIsInstance<ParadoxScopeLinkNode>()
val ParadoxVariableFieldExpression.variableNode: ParadoxDataSourceNode
    get() = nodes.last().cast()
