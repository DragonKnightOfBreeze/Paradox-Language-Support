package icu.windea.pls.localisation.psi

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import icu.windea.pls.core.children
import icu.windea.pls.core.childrenOfType
import icu.windea.pls.core.firstChild
import icu.windea.pls.core.internNode
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

object ParadoxLocalisationLightTreeUtil {
    fun getLocaleFromRootNode(tree: LighterAST): String? {
        val rootNode = tree.root
        val propertyListNode = rootNode.childrenOfType(tree, PROPERTY_LIST).singleOrNull() ?: return null
        return getLocaleFromPropertyListNode(propertyListNode, tree)
    }

    fun getLocaleFromPropertyListNode(node: LighterASTNode, tree: LighterAST): String? {
        return node.firstChild(tree, LOCALE)
            ?.firstChild(tree, LOCALE_TOKEN)
            ?.internNode(tree)?.toString()
    }

    fun getNameFromPropertyNode(node: LighterASTNode, tree: LighterAST): String? {
        return node.firstChild(tree, PROPERTY_KEY)
            ?.children(tree)
            ?.takeIf { it.size == 1 && it.first().tokenType == PROPERTY_KEY_TOKEN }
            ?.last()
            ?.internNode(tree)?.toString()
    }
}
