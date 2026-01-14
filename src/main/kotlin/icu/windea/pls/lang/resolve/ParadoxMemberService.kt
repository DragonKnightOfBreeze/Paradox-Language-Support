package icu.windea.pls.lang.resolve

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
import icu.windea.pls.lang.analysis.ParadoxAnalysisInjector
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.selectFile
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptLightTreeUtil
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptTokenSets
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isBlockMember

@Suppress("unused")
object ParadoxMemberService {
    /**
     * 得到 [element] 对应的脚本成员的 PSI（[ParadoxScriptMember]）的路径。相对于所在文件，顺序从前往后。
     */
    fun getPath(element: PsiElement, limit: Int = 0, maxDepth: Int = 0): ParadoxMemberPath? {
        val root = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return ParadoxMemberPath.resolveEmpty()
        if (root !is ParadoxScriptProperty && root !is ParadoxScriptValue) return ParadoxMemberPath.resolveEmpty()
        var current = element
        val result = ArrayDeque<String>()
        while (current !is PsiFile) {
            val p = when {
                current is ParadoxScriptProperty -> current.name
                current is ParadoxScriptValue && current.isBlockMember() -> "-"
                else -> null
            }
            if (p != null) {
                if (maxDepth > 0 && maxDepth <= result.size) return null
                result.addFirst(p)
                if (limit > 0 && limit == result.size) break
            }
            current = current.parent ?: break
        }
        injectRootKeys(current, result)
        return ParadoxMemberPath.resolve(result)
    }

    /**
     * 得到 [node] 对应的脚本成员的节点的路径。相对于所在文件，顺序从前往后。如果包含参数，则直接返回 `null`。
     */
    fun getPath(node: LighterASTNode, tree: LighterAST, file: VirtualFile, limit: Int = 0, maxDepth: Int = 0): ParadoxMemberPath? {
        var current = node
        val result = ArrayDeque<String>()
        while (current !is PsiFile) {
            val p = when {
                current.tokenType == PROPERTY -> ParadoxScriptLightTreeUtil.getNameFromPropertyNode(current, tree) ?: return null
                ParadoxScriptTokenSets.VALUES.contains(current.tokenType) && tree.getParent(current)?.tokenType == BLOCK -> "-"
                else -> null
            }
            if (p != null) {
                if (maxDepth > 0 && maxDepth <= result.size) return null
                result.addFirst(p)
                if (limit > 0 && limit == result.size) break
            }
            current = tree.getParent(current) ?: break
        }
        injectRootKeys(current, file, result)
        return ParadoxMemberPath.resolve(result)
    }

    /**
     * 得到 [element] 对应的脚本成员的 PSI（[ParadoxScriptMember]）的一组顶级键。相对于所在文件，顺序从前往后。
     */
    fun getRootKeys(element: PsiElement, limit: Int = 0, maxDepth: Int = 0): List<String>? {
        val root = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return emptyList()
        if (root !is ParadoxScriptProperty && root !is ParadoxScriptValue) return emptyList()
        var current = element.parent ?: return emptyList()
        val result = ArrayDeque<String>()
        while (current !is PsiFile) {
            val p = when {
                current is ParadoxScriptProperty -> current.name
                current is ParadoxScriptValue && current.isBlockMember() -> "-"
                else -> null
            }
            if (p != null) {
                if (maxDepth > 0 && maxDepth <= result.size) return null
                result.addFirst(p)
                if (limit > 0 && limit == result.size) break
            }
            current = current.parent ?: break
        }
        injectRootKeys(current, result)
        return result
    }

    /**
     * 得到 [node] 对应的脚本成员的节点的一组顶级键。相对于所在文件，顺序从前往后。如果包含参数，则直接返回 `null`。
     */
    fun getRootKeys(node: LighterASTNode, tree: LighterAST, file: VirtualFile, limit: Int = 0, maxDepth: Int = 0): List<String>? {
        var current = tree.getParent(node) ?: return emptyList()
        val result = ArrayDeque<String>()
        while (current !is PsiFile) {
            val p = when {
                current.tokenType == PROPERTY -> ParadoxScriptLightTreeUtil.getNameFromPropertyNode(current, tree) ?: return null
                ParadoxScriptTokenSets.VALUES.contains(current.tokenType) && tree.getParent(current)?.tokenType == BLOCK -> "-"
                else -> null
            }
            if (p != null) {
                if (maxDepth > 0 && maxDepth <= result.size) return null
                result.addFirst(p)
                if (limit > 0 && limit == result.size) break
            }
            current = tree.getParent(current) ?: break
        }
        injectRootKeys(current, file, result)
        return result
    }

    private fun injectRootKeys(current: PsiElement, result: ArrayDeque<String>) {
        if (current !is PsiFile) return
        val file = selectFile(current) ?: return
        val injectedRootKeys = ParadoxAnalysisInjector.getInjectedRootKeys(file)
        if (injectedRootKeys.isEmpty()) return
        result.addAll(0, injectedRootKeys)
    }

    private fun injectRootKeys(current: LighterASTNode, file: VirtualFile, result: ArrayDeque<String>) {
        if (current.tokenType != ParadoxScriptFile.ELEMENT_TYPE) return
        val injectedRootKeys = ParadoxAnalysisInjector.getInjectedRootKeys(file)
        if (injectedRootKeys.isEmpty()) return
        result.addAll(0, injectedRootKeys)
    }

    /**
     * 得到 [element] 对应的脚本成员的 PSI（[ParadoxScriptMember]）的一组键前缀。顺序从前往后。
     *
     * 找到之前紧邻的一组连续的字符串节点（忽略空白和注释），将它们转化为字符串列表（基于值）。
     */
    fun getKeyPrefixes(element: PsiElement, limit: Int = 0, maxDepth: Int = 0): List<String>? {
        val root = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return emptyList()
        if (root !is ParadoxScriptProperty && root !is ParadoxScriptValue) return emptyList()
        val siblings = element.siblings(forward = false, withSelf = false)
        val result = ArrayDeque<String>()
        for (e in siblings) {
            when (e) {
                is PsiWhiteSpace, is PsiComment -> continue
                is ParadoxScriptString -> {
                    val v = e.value.takeUnless { it.isParameterized() } ?: break
                    if (maxDepth > 0 && maxDepth <= result.size) return null
                    result.addFirst(v)
                    if (limit > 0 && limit == result.size) break
                }
                else -> break
            }
        }
        return result // no optimization here
    }

    /**
     * 得到 [node] 对应的脚本成员的节点的一组键前缀。顺序从前往后。如果包含参数，则直接返回 `null`。
     *
     * 找到之前紧邻的一组连续的字符串节点（忽略空白和注释），将它们转化为字符串列表（基于值）。
     */
    fun getKeyPrefixes(node: LighterASTNode, tree: LighterAST, limit: Int = 0, maxDepth: Int = 0): List<String>? {
        val root = node.parent(tree) ?: return emptyList()
        val siblings = root.children(tree)
        if (siblings.isEmpty()) return emptyList()
        val result = ArrayDeque<String>()
        var flag = false
        for (i in siblings.lastIndex downTo 0) {
            val n = siblings[i]
            if (flag) {
                val tokenType = n.tokenType
                when (tokenType) {
                    TokenType.WHITE_SPACE, COMMENT -> continue
                    STRING -> {
                        val v = ParadoxScriptLightTreeUtil.getValueFromStringNode(n, tree) ?: return null
                        if (maxDepth > 0 && maxDepth <= result.size) return null
                        result.addFirst(v)
                        if (limit > 0 && limit == result.size) break
                    }
                    else -> break
                }
            } else {
                if (n == node) flag = true // 这里需要使用值相等
            }
        }
        return result // no optimization here
    }

    /**
     * 得到 [element] 对应的脚本成员的 PSI（[ParadoxScriptMember]）的键前缀。
     */
    fun getKeyPrefix(element: PsiElement): String? {
        return getKeyPrefixes(element, limit = 1)?.singleOrNull()
    }

    /**
     * 得到 [node] 对应的脚本成员的节点的键前缀。如果包含参数，则直接返回 `null`。
     */
    fun getKeyPrefix(node: LighterASTNode, tree: LighterAST): String? {
        return getKeyPrefixes(node, tree, limit = 1)?.singleOrNull()
    }
}
