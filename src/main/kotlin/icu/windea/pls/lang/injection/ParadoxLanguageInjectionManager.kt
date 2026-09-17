package icu.windea.pls.lang.injection

import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.findFast
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.psi.ParadoxLanguageInjectionHost
import icu.windea.pls.model.injection.ParadoxParameterValueInjectionInfo
import icu.windea.pls.script.psi.ParadoxScriptNormalParameterArgument
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

@Optimized
object ParadoxLanguageInjectionManager {
    /**
     * 向上找到最顶层的作为语言注入宿主的虚拟文件，或者返回自身。
     */
    fun findTopHostFileOrThis(file: VirtualFile): VirtualFile {
        return doFindTopHostFileOrThis(file)
    }

    private tailrec fun doFindTopHostFileOrThis(file: VirtualFile): VirtualFile {
        if (file is VirtualFileWindow) return doFindTopHostFileOrThis(file.delegate)
        return file
    }

    /**
     * 向上找到最顶层的作为语言注入宿主的 PSI 元素，或者返回自身。
     */
    fun findTopHostElementOrThis(element: PsiElement, project: Project): PsiElement {
        return doFindTopHostElementOrThis(element, project)
    }

    private tailrec fun doFindTopHostElementOrThis(element: PsiElement, project: Project): PsiElement {
        val host = InjectedLanguageManager.getInstance(project).getInjectionHost(element)
        if (host == null) return element
        return doFindTopHostElementOrThis(host, project)
    }

    fun getParameterValueInjectionInfoFromInjectedFile(injectedFile: PsiFile): ParadoxParameterValueInjectionInfo? {
        if (!VirtualFileService.isInjectedFile(injectedFile.virtualFile)) return null
        val host = InjectedLanguageManager.getInstance(injectedFile.project).getInjectionHost(injectedFile)
        if (host !is ParadoxLanguageInjectionHost) return null
        return getParameterValueInjectionInfoFromHost(injectedFile, host)
    }

    fun getParameterValueInjectionInfoFromHost(injectedFile: PsiFile, host: ParadoxLanguageInjectionHost): ParadoxParameterValueInjectionInfo? {
        val injectionInfos = host.getUserData(ParadoxLanguageInjectionKeys.parameterValueInjectionInfos)
        if (injectionInfos.isNullOrEmpty()) return null
        val injectionInfo = when {
            host is ParadoxScriptStringExpressionElement -> {
                // NOTE 3.0.3 since `InjectedLanguageUtilBase.getShreds` is internal and deprecated, we use `InjectedLanguageManager.intersectWithAllEditableFragments` instead (should be equivalent)

                // val shreds = InjectedLanguageUtilBase.getShreds(injectedFile)
                // val shred = shreds?.singleOrNull()
                // val rangeInsideHost = shred?.rangeInsideHost ?: return null
                // injectionInfos.findFast { it.rangeInsideHost.startOffset in rangeInsideHost }

                val hostRange = TextRange.create(0, host.textLength)
                val editables = InjectedLanguageManager.getInstance(injectedFile.project).intersectWithAllEditableFragments(injectedFile, hostRange)
                val rangeInsideHost = editables.singleOrNull() ?: return null

                // `injectionInfo.rangeInsideHost` may not equal to `rangeInsideHost`, but inside (e.g., there are escaped double quotes)
                injectionInfos.findFast { it.rangeInsideHost in rangeInsideHost }
            }
            host is ParadoxScriptNormalParameterArgument -> {
                // just use the only one
                injectionInfos.singleOrNull()
            }
            else -> null
        }
        return injectionInfo
    }
}
