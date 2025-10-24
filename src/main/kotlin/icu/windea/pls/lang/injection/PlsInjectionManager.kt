package icu.windea.pls.lang.injection

import com.intellij.injected.editor.DocumentWindow
import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.injected.Place
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.getShreds

object PlsInjectionManager {
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
     * 得到语言注入的切片列表（shreds）。
     */
    fun getShreds(file: PsiFile): Place? {
        // why it's deprecated and internal???
        // @Suppress("UnstableApiUsage", "DEPRECATION")
        // return InjectedLanguageUtilBase.getShreds(this)

        return file.viewProvider.document.castOrNull<DocumentWindow>()?.getShreds()
    }
}
