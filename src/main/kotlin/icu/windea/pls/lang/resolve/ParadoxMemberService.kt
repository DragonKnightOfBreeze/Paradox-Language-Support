package icu.windea.pls.lang.resolve

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.siblings
import icu.windea.pls.lang.analysis.ParadoxAnalysisInjector
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.selectFile
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isBlockMember

object ParadoxMemberService {
    /**
     * 得到 [element] 对应的脚本成员的 PSI（[ParadoxScriptMember]）的路径。相对于所在文件，顺序从前往后。
     * 如果 [parameterAware] 为 `false`，且包含参数，则直接返回 `null`。
     */
    fun getPath(element: PsiElement, limit: Int = 0, maxDepth: Int = 0, parameterAware: Boolean = true): ParadoxMemberPath? {
        if (element is PsiFileSystemItem) return ParadoxMemberPath.resolveEmpty()
        val member = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return ParadoxMemberPath.resolveEmpty()
        if (member !is ParadoxScriptProperty && member !is ParadoxScriptValue) return ParadoxMemberPath.resolveEmpty()
        var current: PsiElement = member
        val result = ArrayDeque<String>()
        while (current !is PsiFile) {
            val p = when {
                current is ParadoxScriptProperty -> current.name
                current is ParadoxScriptValue && current.isBlockMember() -> "-"
                else -> null
            }
            if (p != null) {
                if (maxDepth > 0 && maxDepth <= result.size) return null
                if (!parameterAware && p.isParameterized()) return null
                result.addFirst(p)
                if (limit > 0 && limit == result.size) break
            }
            current = current.parent ?: break
        }
        injectRootKeys(current, result)
        return ParadoxMemberPath.resolve(result)
    }

    /**
     * 得到 [element] 对应的脚本成员的 PSI（[ParadoxScriptMember]）的一组顶级键。相对于所在文件，顺序从前往后。
     * 如果 [parameterAware] 为 `false`，且包含参数，则直接返回 `null`。
     */
    fun getRootKeys(element: PsiElement, limit: Int = 0, maxDepth: Int = 0, parameterAware: Boolean = true): List<String>? {
        if (element is PsiFileSystemItem) return emptyList()
        val member = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return emptyList()
        if (member !is ParadoxScriptProperty && member !is ParadoxScriptValue) return emptyList()
        var current: PsiElement = member.parent ?: return emptyList()
        val result = ArrayDeque<String>()
        while (current !is PsiFile) {
            val p = when {
                current is ParadoxScriptProperty -> current.name
                current is ParadoxScriptValue && current.isBlockMember() -> "-"
                else -> null
            }
            if (p != null) {
                if (maxDepth > 0 && maxDepth <= result.size) return null
                if (!parameterAware && p.isParameterized()) return null
                result.addFirst(p)
                if (limit > 0 && limit == result.size) break
            }
            current = current.parent ?: break
        }
        injectRootKeys(current, result)
        return result
    }

    private fun injectRootKeys(current: PsiElement, result: ArrayDeque<String>) {
        if (current !is PsiFile) return
        val file = selectFile(current) ?: return
        val injectedRootKeys = ParadoxAnalysisInjector.getInjectedRootKeys(file)
        if (injectedRootKeys.isEmpty()) return
        result.addAll(0, injectedRootKeys)
    }

    /**
     * 得到 [element] 对应的脚本成员的 PSI（[ParadoxScriptMember]）的一组键前缀。顺序从前往后。
     * 如果 [parameterAware] 为 `false`，且包含参数，则直接返回 `null`。
     *
     * 找到之前紧邻的一组连续的字符串节点（忽略空白和注释），将它们转化为字符串列表（基于值）。
     */
    fun getKeyPrefixes(element: PsiElement, limit: Int = 0, maxDepth: Int = 0, parameterAware: Boolean = true): List<String>? {
        if (element is PsiFileSystemItem) return emptyList()
        val member = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return emptyList()
        if (member !is ParadoxScriptProperty && member !is ParadoxScriptValue) return emptyList()
        val siblings = member.siblings(forward = false, withSelf = false)
        val result = ArrayDeque<String>()
        for (e in siblings) {
            when (e) {
                is PsiWhiteSpace, is PsiComment -> continue
                is ParadoxScriptString -> {
                    val v = e.value.takeUnless { it.isParameterized() } ?: break
                    if (maxDepth > 0 && maxDepth <= result.size) return null
                    if (!parameterAware && v.isParameterized()) return null
                    result.addFirst(v)
                    if (limit > 0 && limit == result.size) break
                }
                else -> break
            }
        }
        return result // no optimization here
    }

    /**
     * 得到 [element] 对应的脚本成员的 PSI（[ParadoxScriptMember]）的键前缀。
     */
    fun getKeyPrefix(element: PsiElement): String? {
        return getKeyPrefixes(element, limit = 1, parameterAware = false)?.singleOrNull()
    }
}
