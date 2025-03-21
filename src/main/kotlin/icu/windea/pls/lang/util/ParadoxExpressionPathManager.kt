package icu.windea.pls.lang.util

import com.intellij.lang.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import java.util.*

/**
 * @see ParadoxExpressionPath
 */
object ParadoxExpressionPathManager {
    /**
     * 得到指定的属性或值对应的PSI的相对于所属文件的表达式路径。
     */
    fun get(element: PsiElement, maxDepth: Int = -1): ParadoxExpressionPath? {
        var current: PsiElement = element
        var depth = 0
        val originalSubPaths = LinkedList<String>()
        while (current !is PsiFile) {
            when {
                current is ParadoxScriptProperty -> {
                    val p = current.propertyKey.text
                    originalSubPaths.addFirst(p)
                    depth++
                }
                current is ParadoxScriptValue && current.isBlockMember() -> {
                    originalSubPaths.addFirst("-")
                    depth++
                }
            }
            //如果发现深度超出指定的最大深度，则直接返回null
            if (maxDepth > 0 && maxDepth < depth) return null
            current = current.parent ?: break
        }
        if (current is PsiFile) {
            val virtualFile = selectFile(current)
            val injectedElementPathPrefix = virtualFile?.getUserData(PlsKeys.injectedElementPathPrefix)
            if (injectedElementPathPrefix != null && injectedElementPathPrefix.isNotEmpty()) {
                originalSubPaths.addAll(0, injectedElementPathPrefix.subPaths)
            }
        }
        return ParadoxExpressionPath.resolve(originalSubPaths)
    }

    /**
     * 得到指定的属性或值对应的节点的相对于所属文件的表达式路径。
     */
    fun get(node: LighterASTNode, tree: LighterAST, file: VirtualFile, maxDepth: Int = -1): ParadoxExpressionPath? {
        var current: LighterASTNode = node
        var depth = 0
        val originalSubPaths = LinkedList<String>()
        while (current !is PsiFile) {
            when {
                current.tokenType == PROPERTY -> {
                    val p = current.firstChild(tree, PROPERTY_KEY)
                        ?.firstChild(tree, PROPERTY_KEY_TOKEN)
                        ?.internNode(tree)?.toString() ?: return null
                    originalSubPaths.addFirst(p)
                    depth++
                }
                ParadoxScriptTokenSets.VALUES.contains(current.tokenType) && tree.getParent(current)?.tokenType == BLOCK -> {
                    originalSubPaths.addFirst("-")
                    depth++
                }
            }
            //如果发现深度超出指定的最大深度，则直接返回null
            if (maxDepth > 0 && maxDepth < depth) return null
            current = tree.getParent(current) ?: break
        }
        if (current.tokenType == ParadoxScriptStubElementTypes.FILE) {
            val virtualFile = file
            val injectedElementPathPrefix = virtualFile.getUserData(PlsKeys.injectedElementPathPrefix)
            if (injectedElementPathPrefix != null && injectedElementPathPrefix.isNotEmpty()) {
                originalSubPaths.addAll(0, injectedElementPathPrefix.subPaths)
            }
        }
        return ParadoxExpressionPath.resolve(originalSubPaths)
    }

    /**
     * 得到指定的属性对应的PSI的键前缀。
     *
     * 找到[element]对应的[ParadoxScriptProperty]，
     * 接着找到直接在其前面的连续的一组[ParadoxScriptString]（忽略空白和注释），
     * 最后将它们转化为字符串列表（基于值，顺序由前往后）。
     */
    fun getKeyPrefixes(element: PsiElement): List<String> {
        val property = element.parentOfType<ParadoxScriptProperty>(withSelf = true) ?: return emptyList()
        var result: MutableList<String>? = null
        property.siblings(forward = false, withSelf = false).forEach f@{ e ->
            if(e is PsiWhiteSpace || e is PsiComment) return@f
            if(e is ParadoxScriptString) {
                if(result == null) result = mutableListOf()
                result!! += e.value
            }
            return result ?: emptyList()
        }
        return emptyList()
    }

    /**
     * 得到指定的属性对应的节点的键前缀。
     */
    fun getKeyPrefixes(node: LighterASTNode, tree: LighterAST, file: VirtualFile): List<String> {
        TODO()
    }
}
