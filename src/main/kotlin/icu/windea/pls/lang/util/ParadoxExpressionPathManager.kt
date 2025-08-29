package icu.windea.pls.lang.util

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.TokenType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.siblings
import icu.windea.pls.core.children
import icu.windea.pls.core.childrenOfType
import icu.windea.pls.core.firstChild
import icu.windea.pls.core.internNode
import icu.windea.pls.core.parent
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.PlsKeys
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.selectFile
import icu.windea.pls.model.paths.ParadoxExpressionPath
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.BLOCK
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.COMMENT
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PROPERTY
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PROPERTY_KEY
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PROPERTY_KEY_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.STRING
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.STRING_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMemberElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptTokenSets
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isBlockMember

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
        val originalSubPaths = ArrayDeque<String>()
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
            if (maxDepth >= 0 && maxDepth < depth) return null
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
        val originalSubPaths = ArrayDeque<String>()
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
            if (maxDepth >= 0 && maxDepth < depth) return null
            current = tree.getParent(current) ?: break
        }
        if (current.tokenType == ParadoxScriptFile.ELEMENT_TYPE) {
            val virtualFile = file
            val injectedElementPathPrefix = virtualFile.getUserData(PlsKeys.injectedElementPathPrefix)
            if (injectedElementPathPrefix != null && injectedElementPathPrefix.isNotEmpty()) {
                originalSubPaths.addAll(0, injectedElementPathPrefix.subPaths)
            }
        }
        return ParadoxExpressionPath.resolve(originalSubPaths)
    }

    /**
     * 得到指定的属性或值对应的PSI的键前缀。
     *
     * 找到[element]对应的[ParadoxScriptProperty]或[ParadoxScriptValue]，
     * 接着找到直接在其前面的连续的一组[ParadoxScriptString]（忽略空白和注释），
     * 最后将它们转化为字符串列表（基于值，顺序从后往前）。
     */
    fun getKeyPrefixes(element: PsiElement): List<String> {
        val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = true) ?: return emptyList()
        if (memberElement !is ParadoxScriptProperty && memberElement !is ParadoxScriptValue) return emptyList()
        var result: MutableList<String>? = null
        memberElement.siblings(forward = false, withSelf = false).forEach f@{ e ->
            when (e) {
                is PsiWhiteSpace, is PsiComment -> return@f
                is ParadoxScriptString -> {
                    val v = e.value.takeUnless { it.isParameterized() } ?: return result ?: emptyList()
                    if (result == null) result = mutableListOf()
                    result += v
                }
                else -> return result ?: emptyList()
            }
        }
        return result ?: emptyList()
    }

    /**
     * 得到指定的属性对应的节点的键前缀。
     */
    fun getKeyPrefixes(node: LighterASTNode, tree: LighterAST): List<String> {
        val parent = node.parent(tree) ?: return emptyList()
        val siblings = parent.children(tree)
        if (siblings.isEmpty()) return emptyList()
        var flag = false
        var result: MutableList<String>? = null
        for (i in siblings.lastIndex downTo 0) {
            val n = siblings[i]
            if (flag) {
                val tokenType = n.tokenType
                when (tokenType) {
                    TokenType.WHITE_SPACE, COMMENT -> continue
                    STRING -> {
                        val v = getValueFromStringNode(n, tree) ?: return result ?: emptyList()
                        if (result == null) result = mutableListOf()
                        result += v
                    }
                    else -> return result ?: emptyList()
                }
            } else {
                if (n == node) flag = true //这里需要使用值相等
            }
        }
        return result ?: emptyList()
    }

    private fun getValueFromStringNode(node: LighterASTNode, tree: LighterAST): String? {
        return node.childrenOfType(tree, STRING_TOKEN).singleOrNull()
            ?.internNode(tree)?.toString()?.unquote()
    }
}
