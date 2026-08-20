package icu.windea.pls.lang.resolve

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.siblings
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.lang.analysis.ParadoxAnalysisInjectionManager
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isDirectValue

@Optimized
object ParadoxMemberService {
    /**
     * 使用 [element] 作为上下文，为 [result] 注入一组顶级键。
     */
    fun injectRootKeys(element: PsiElement, result: MutableList<String>) {
        if (element !is ParadoxScriptFile) return
        val vFile = selectFile(element) ?: return
        val injectedRootKeys = ParadoxAnalysisInjectionManager.getInjectedRootKeys(vFile)
        if (injectedRootKeys.isEmpty()) return
        result.addAll(0, injectedRootKeys)
    }

    /**
     * 得到 [element] 对应的脚本成员（[ParadoxScriptMember]）的路径。相对于所在文件，顺序从前往后。
     * 如果 [parameterAware] 为 `false`，且包含参数，则直接返回 `null`。
     */
    fun getPath(element: PsiElement, limit: Int = 0, maxDepth: Int = 0, parameterAware: Boolean = true): ParadoxMemberPath? {
        if (element.language !== ParadoxScriptLanguage) return null
        if (element is PsiFileSystemItem) return ParadoxMemberPath.resolveEmpty()
        val member = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return ParadoxMemberPath.resolveEmpty()
        if (member !is ParadoxScriptProperty && member !is ParadoxScriptValue) return ParadoxMemberPath.resolveEmpty()
        return getPathFromPsi(member, limit, maxDepth, parameterAware)
    }

    private fun getPathFromPsi(element: ParadoxScriptMember, limit: Int, maxDepth: Int, parameterAware: Boolean): ParadoxMemberPath? {
        // resolve from PSI
        var current: PsiElement = element
        val deque = ArrayDeque<String>()
        while (current !is PsiFile) {
            // 3.0.1 optimize: get and cache parent first
            val parent = current.parent ?: break
            val p = when {
                current is ParadoxScriptProperty -> current.name
                current is ParadoxScriptValue && current.isDirectValue(parent) -> "-"
                else -> null
            }
            if (p != null) {
                if (maxDepth > 0 && maxDepth <= deque.size) return null
                if (!parameterAware && p.isParameterized()) return null
                deque.addFirst(p)
                if (limit > 0 && limit == deque.size) break
            }
            current = parent
        }
        injectRootKeys(current, deque)
        return ParadoxMemberPath.resolve(deque)
    }

    /**
     * 得到 [element] 对应的脚本成员（[ParadoxScriptMember]）的一组顶级键。相对于所在文件，顺序从前往后。
     * 如果 [parameterAware] 为 `false`，且包含参数，则直接返回 `null`。
     */
    fun getRootKeys(element: PsiElement, limit: Int = 0, maxDepth: Int = 0, parameterAware: Boolean = true): List<String>? {
        if (element.language !== ParadoxScriptLanguage) return null
        if (element is PsiFileSystemItem) return emptyList()
        val member = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return emptyList()
        if (member !is ParadoxScriptProperty && member !is ParadoxScriptValue) return emptyList()
        return getRootKeysFromPsi(member, limit, maxDepth, parameterAware)
    }

    private fun getRootKeysFromPsi(element: ParadoxScriptMember, limit: Int, maxDepth: Int, parameterAware: Boolean): List<String>? {
        // resolve from PSI
        var current: PsiElement = element.parent ?: return emptyList()
        val deque = ArrayDeque<String>()
        while (current !is PsiFile) {
            // 3.0.1 optimize: get and cache parent first
            val parent = current.parent ?: break
            val p = when {
                current is ParadoxScriptProperty -> current.name
                current is ParadoxScriptValue && current.isDirectValue(parent) -> "-"
                else -> null
            }
            if (p != null) {
                if (maxDepth > 0 && maxDepth <= deque.size) return null
                if (!parameterAware && p.isParameterized()) return null
                deque.addFirst(p)
                if (limit > 0 && limit == deque.size) break
            }
            current = parent
        }
        injectRootKeys(current, deque)
        if (deque.isEmpty()) return emptyList()
        return deque
    }

    /**
     * 得到 [element] 对应的脚本成员（[ParadoxScriptMember]）的一组键前缀。顺序从前往后。
     * 如果 [parameterAware] 为 `false`，且包含参数，则直接返回 `null`。
     *
     * 找到之前紧邻的一组连续的字符串节点（忽略空白和注释），将它们转化为字符串列表（基于值）。
     */
    fun getKeyPrefixes(element: PsiElement, limit: Int = 0, maxDepth: Int = 0, parameterAware: Boolean = true): List<String>? {
        if (element.language !== ParadoxScriptLanguage) return null
        if (element is PsiFileSystemItem) return emptyList()
        val member = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return emptyList()
        if (member !is ParadoxScriptProperty && member !is ParadoxScriptValue) return emptyList()
        return getKeyPrefixesFromPsi(member, limit, maxDepth, parameterAware)
    }

    private fun getKeyPrefixesFromPsi(element: ParadoxScriptMember, limit: Int, maxDepth: Int, parameterAware: Boolean): List<String>? {
        // resolve from PSI
        val deque = ArrayDeque<String>()
        val siblings = element.siblings(forward = false, withSelf = false)
        for (e in siblings) {
            when (e) {
                is PsiWhiteSpace, is PsiComment -> continue
                is ParadoxScriptString -> {
                    val v = e.value.takeUnless { it.isParameterized() } ?: break
                    if (maxDepth > 0 && maxDepth <= deque.size) return null
                    if (!parameterAware && v.isParameterized()) return null
                    deque.addFirst(v)
                    if (limit > 0 && limit == deque.size) break
                }
                else -> break
            }
        }
        if (deque.isEmpty()) return emptyList()
        return deque
    }

    /**
     * 得到 [element] 对应的脚本成员（[ParadoxScriptMember]）的键前缀。
     */
    fun getKeyPrefix(element: PsiElement): String? {
        return getKeyPrefixes(element, limit = 1, parameterAware = false)?.singleOrNull()
    }

    /**
     * 得到 [element] 的类型键。
     *
     * 说明：
     * - 如果定义来自脚本文件 ，则使用去除扩展名后的文件名。
     * - 如果定义来自脚本属性，则使用属性名（需要检查是否合法）。
     */
    fun getTypeKey(element: ParadoxDefinitionElement, elementName: String = element.name): String? {
        if (elementName.isEmpty()) return null // empty -> unexpected
        return when (element) {
            is ParadoxScriptFile -> {
                elementName.substringBeforeLast('.') // trim file extension
            }
            is ParadoxScriptProperty -> {
                // if (!elementName.isIdentifier(".-")) return null // #369 can also be any (valid) string literals
                if (elementName.isParameterized()) return null // skip if is parameterized
                if (ParadoxInlineScriptManager.isMatched(elementName, element)) return null // skip if is inline script usage
                if (ParadoxDefinitionInjectionManager.isMatched(elementName, element)) return null // skip if is definition injection usage
                elementName
            }
            else -> elementName
        }
    }
}
