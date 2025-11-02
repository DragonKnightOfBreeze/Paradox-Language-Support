package icu.windea.pls.script.psi

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import icu.windea.pls.core.children
import icu.windea.pls.core.childrenOfType
import icu.windea.pls.core.firstChild
import icu.windea.pls.core.internNode
import icu.windea.pls.core.unquote
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

object ParadoxScriptLightTreeUtil {
    fun getNameFromScriptedVariableNode(node: LighterASTNode, tree: LighterAST): String? {
        // 如果带有参数，则直接返回 null
        return node.firstChild(tree, SCRIPTED_VARIABLE_NAME)
            ?.children(tree)
            ?.takeIf { it.size == 2 && it.first().tokenType == AT }
            ?.last()
            ?.internNode(tree)?.toString()
    }

    fun getNameFromPropertyNode(node: LighterASTNode, tree: LighterAST): String? {
        // 如果带有参数，则直接返回 null
        return node.firstChild(tree, PROPERTY_KEY)
            ?.childrenOfType(tree, PROPERTY_KEY_TOKEN)?.singleOrNull()
            ?.internNode(tree)?.toString()?.unquote()
    }

    fun getStringValueFromPropertyNode(node: LighterASTNode, tree: LighterAST): String? {
        // 如果带有参数，则直接返回 null
        return node.firstChild(tree, STRING)
            ?.childrenOfType(tree, STRING_TOKEN)?.singleOrNull()
            ?.internNode(tree)?.toString()?.unquote()
    }

    fun findPropertyFromPropertyNode(node: LighterASTNode, tree: LighterAST, name: String, ignoreCase: Boolean = true): LighterASTNode? {
        return node.firstChild(tree, BLOCK)
            ?.firstChild(tree) { it.tokenType == PROPERTY && getNameFromPropertyNode(it, tree).let { n -> n != null && n.equals(name, ignoreCase) } }
    }

    // fun findPropertyFromPropertyNode(node: LighterASTNode, tree: LighterAST, predicate: (String?) -> Boolean): LighterASTNode? {
    //     return node.firstChild(tree, BLOCK)
    //         ?.firstChild(tree) { it.tokenType == PROPERTY && predicate(getNameFromPropertyNode(it, tree)) }
    // }

    fun getValueFromStringNode(node: LighterASTNode, tree: LighterAST): String? {
        return node.childrenOfType(tree, STRING_TOKEN).singleOrNull()
            ?.internNode(tree)?.toString()?.unquote()
    }
}
