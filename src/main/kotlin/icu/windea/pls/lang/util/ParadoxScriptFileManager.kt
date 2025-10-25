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
import icu.windea.pls.core.parent
import icu.windea.pls.lang.PlsKeys
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.selectFile
import icu.windea.pls.model.paths.ParadoxElementPath
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.BLOCK
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.COMMENT
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PROPERTY
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.STRING
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptLightTreeUtil
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptTokenSets
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isBlockMember

object ParadoxScriptFileManager {
    /**
     * 得到 [element] 对应的脚本成员的 PSI（[ParadoxScriptMember]）的相对于所在文件的路径。
     *
     * @param maxDepth 指定的最大深度。如果为正数且深度超出指定的最大深度，则直接返回 null。
     */
    fun getElementPath(element: PsiElement, maxDepth: Int = -1): ParadoxElementPath? {
        var current: PsiElement = element
        var depth = 0
        val subPaths = ArrayDeque<String>()
        while (current !is PsiFile) {
            when {
                current is ParadoxScriptProperty -> {
                    val p = current.name
                    subPaths.addFirst(p)
                    depth++
                }
                current is ParadoxScriptValue && current.isBlockMember() -> {
                    subPaths.addFirst("-")
                    depth++
                }
            }
            if (maxDepth >= 0 && maxDepth < depth) return null // 如果深度超出指定的最大深度，则直接返回 null
            current = current.parent ?: break
        }
        if (current is PsiFile) {
            val virtualFile = selectFile(current)
            val injectedElementPathPrefix = virtualFile?.getUserData(PlsKeys.injectedElementPathPrefix)
            if (injectedElementPathPrefix != null && injectedElementPathPrefix.isNotEmpty()) {
                subPaths.addAll(0, injectedElementPathPrefix.subPaths)
            }
        }
        return ParadoxElementPath.resolve(subPaths)
    }

    /**
     * 得到 [node] 对应的脚本成员的节点的相对于所在文件的路径。
     *
     * @param maxDepth 指定的最大深度。如果为正数且深度超出指定的最大深度，则直接返回 null。
     */
    fun getElementPath(node: LighterASTNode, tree: LighterAST, file: VirtualFile, maxDepth: Int = -1): ParadoxElementPath? {
        var current: LighterASTNode = node
        var depth = 0
        val subPaths = ArrayDeque<String>()
        while (current !is PsiFile) {
            when {
                current.tokenType == PROPERTY -> {
                    val p = ParadoxScriptLightTreeUtil.getNameFromPropertyNode(current, tree) ?: return null
                    subPaths.addFirst(p)
                    depth++
                }
                ParadoxScriptTokenSets.VALUES.contains(current.tokenType) && tree.getParent(current)?.tokenType == BLOCK -> {
                    subPaths.addFirst("-")
                    depth++
                }
            }
            if (maxDepth >= 0 && maxDepth < depth) return null // 如果深度超出指定的最大深度，则直接返回 null
            current = tree.getParent(current) ?: break
        }
        if (current.tokenType == ParadoxScriptFile.ELEMENT_TYPE) {
            val virtualFile = file
            val injectedElementPathPrefix = virtualFile.getUserData(PlsKeys.injectedElementPathPrefix)
            if (injectedElementPathPrefix != null && injectedElementPathPrefix.isNotEmpty()) {
                subPaths.addAll(0, injectedElementPathPrefix.subPaths)
            }
        }
        return ParadoxElementPath.resolve(subPaths)
    }

    /**
     * 得到 [element] 对应的脚本成员的 PSI（[ParadoxScriptMember]）的一组键前缀。
     *
     * 找到 [element] 对应的 [ParadoxScriptMember]，
     * 接着找到直接在其前面的连续的一组 [ParadoxScriptString]（忽略空白和注释），
     * 最后将它们转化为字符串列表（基于值，顺序从后往前）。
     */
    fun getKeyPrefixes(element: PsiElement): List<String> {
        val memberElement = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return emptyList()
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
     * 得到 [node] 对应的脚本成员的节点的一组键前缀。
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
                        val v = ParadoxScriptLightTreeUtil.getValueFromStringNode(n, tree) ?: return result ?: emptyList()
                        if (result == null) result = mutableListOf()
                        result += v
                    }
                    else -> return result ?: emptyList()
                }
            } else {
                if (n == node) flag = true // 这里需要使用值相等
            }
        }
        return result ?: emptyList()
    }
}
