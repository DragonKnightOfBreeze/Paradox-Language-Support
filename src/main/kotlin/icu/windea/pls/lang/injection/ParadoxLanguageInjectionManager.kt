package icu.windea.pls.lang.injection

import com.intellij.injected.editor.DocumentWindow
import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.injected.Place
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.findFast
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.getShreds
import icu.windea.pls.lang.psi.ParadoxLanguageInjectionHost
import icu.windea.pls.model.injection.ParadoxParameterValueInjectionInfo
import icu.windea.pls.script.psi.ParadoxScriptNormalParameterArgument
import icu.windea.pls.script.psi.ParadoxScriptString

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

    /**
     * 得到语言注入的切片列表。
     */
    fun getShreds(injectedFile: PsiFile): Place? {
        // we just need `shred.rangeInsideHost` atm, nothing more, but:
        // - `com.intellij.psi.impl.source.tree.injected.DocumentWindowImpl.getShreds` is internal
        // - `com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtilBase.getShreds(com.intellij.psi.PsiFile)` is internal and deprecated
        return injectedFile.viewProvider.document.castOrNull<DocumentWindow>()?.getShreds()
    }

    /**
     * 是否是来自脚本文件的注入的文件（如：内联脚本的参数值对应的注入的文件）。
     */
    fun isInjectedFileFromScriptFile(file: PsiFile): Boolean {
        if (!VirtualFileService.isInjectedFile(file.virtualFile)) return false
        val host = InjectedLanguageManager.getInstance(file.project).getInjectionHost(file)
        if (host !is ParadoxLanguageInjectionHost) return false
        return true
    }

    fun getParameterValueInjectionInfoFromInjectedFile(injectedFile: PsiFile): ParadoxParameterValueInjectionInfo? {
        if (!VirtualFileService.isInjectedFile(injectedFile.virtualFile)) return null
        val host = InjectedLanguageManager.getInstance(injectedFile.project).getInjectionHost(injectedFile)
        if (host !is ParadoxLanguageInjectionHost) return null
        val injectionInfos = host.getUserData(ParadoxLanguageInjectionKeys.parameterValueInjectionInfos)
        if (injectionInfos.isNullOrEmpty()) return null
        val injectionInfo = when {
            host is ParadoxScriptString -> {
                // `injectionInfo.rangeInsideHost` may not equal to `rangeInsideHost`, but inside (e.g., there are escaped double quotes)
                val shreds = getShreds(injectedFile)
                val shred = shreds?.singleOrNull()
                val rangeInsideHost = shred?.rangeInsideHost ?: return null
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
