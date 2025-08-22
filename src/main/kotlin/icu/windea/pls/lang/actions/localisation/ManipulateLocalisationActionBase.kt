package icu.windea.pls.lang.actions.localisation

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.ui.locale.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.dataFlow.*
import icu.windea.pls.lang.util.manipulators.*
import icu.windea.pls.localisation.*
import kotlinx.coroutines.*

/**
 * 用于处理本地化的一类动作。
 *
 * * 应当支持在多个级别批量处理。
 * * 应当在开始处理之前弹出对话框，以确认是否真的要处理。
 */
abstract class ManipulateLocalisationActionBase<C> : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project
        if (project == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }
        if (!isAvailable(e, project)) {
            e.presentation.isEnabledAndVisible = false
            return
        }
        val hasFiles = hasFiles(e, project)
        e.presentation.isEnabledAndVisible = hasFiles
        if (!hasFiles) return
        val hasElements = hasElements(e, project)
        e.presentation.isEnabled = hasElements
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val files = findFiles(e, project)
        if (files.isEmpty()) return
        beforeInvokeAll(e, project, files)
        doInvokeAll(e, project, files)
    }

    protected open fun isAvailable(e: AnActionEvent, project: Project): Boolean {
        return true
    }

    protected open fun isValidFile(file: VirtualFile): Boolean {
        if (file.fileType !is ParadoxLocalisationFileType) return false
        if (file.fileInfo == null) return false
        if (PlsVfsManager.isLightFile(file)) return false
        return true
    }

    protected open fun hasFiles(e: AnActionEvent, project: Project): Boolean {
        var r: PsiFile? = null
        PlsVfsManager.processFiles(e, deep = true) p@{ file ->
            if (!isValidFile(file)) return@p true
            r = file.toPsiFile(project) ?: return@p true
            false
        }
        return r != null
    }

    protected open fun hasElements(e: AnActionEvent, project: Project): Boolean {
        var r: PsiElement? = null
        PlsVfsManager.processFiles(e, deep = true) p@{ file ->
            if (!isValidFile(file)) return@p true
            val psiFile = file.toPsiFile(project) ?: return@p true
            r = findElements(psiFile).firstOrNull()
            false
        }
        return r != null
    }

    protected open fun findFiles(e: AnActionEvent, project: Project): List<PsiFile> {
        val project = e.project ?: return emptyList()
        val files = PlsVfsManager.findFiles(e, deep = true) { file -> isValidFile(file) }
        return files.mapNotNull { it.toPsiFile(project) }
    }

    protected open fun findElements(psiFile: PsiFile): ParadoxLocalisationSequence {
        return ParadoxLocalisationManipulator.buildSequence(psiFile)
    }

    protected open fun beforeInvokeAll(e: AnActionEvent, project: Project, files: List<PsiFile>): Boolean {
        //弹出对话框，以确认是否真的要处理本地化
        val actionName = e.presentation.text
        val toProcess = files.size
        val title = PlsBundle.message("manipulation.confirm.title")
        val message = PlsBundle.message("manipulation.confirm.message", actionName, toProcess)
        return MessageDialogBuilder.okCancel(title, message).ask(project)
    }

    protected abstract fun doInvokeAll(e: AnActionEvent, project: Project, files: List<PsiFile>)

    protected fun doHandleAllAsync(e: AnActionEvent, project: Project, context: C) {
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            doHandleAll(e, project, context)
        }
    }

    protected abstract suspend fun doHandleAll(e: AnActionEvent, project: Project, context: C)

    abstract class Default : ManipulateLocalisationActionBase<Default.Context>() {
        final override fun doInvokeAll(e: AnActionEvent, project: Project, files: List<PsiFile>) {
            doHandleAllAsync(e, project, Context(files))
        }

        data class Context(
            val files: List<PsiFile>
        )
    }

    abstract class WithLocalePopup : ManipulateLocalisationActionBase<WithLocalePopup.Context>() {
        protected open fun createLocalePopup(e: AnActionEvent, project: Project): ParadoxLocaleListPopup {
            val allLocales = ParadoxLocaleManager.getLocaleConfigs()
            return ParadoxLocaleListPopup(allLocales)
        }

        final override fun doInvokeAll(e: AnActionEvent, project: Project, files: List<PsiFile>) {
            val localePopup = createLocalePopup(e, project)
            localePopup.doFinalStep action@{
                val selected = localePopup.selectedLocale ?: return@action
                doHandleAllAsync(e, project, Context(files, selected))
            }
            JBPopupFactory.getInstance().createListPopup(localePopup).showInBestPositionFor(e.dataContext)
        }

        data class Context(
            val files: List<PsiFile>,
            val selectedLocale: CwtLocaleConfig
        )
    }

    abstract class WithPopup<T> : ManipulateLocalisationActionBase<WithPopup.Context<T>>() {
        protected abstract fun createPopup(e: AnActionEvent, project: Project, callback: (T) -> Unit): JBPopup?

        final override fun doInvokeAll(e: AnActionEvent, project: Project, files: List<PsiFile>) {
            val popup = createPopup(e, project) {
                doHandleAllAsync(e, project, Context(files, it))
            }
            if (popup != null) {
                popup.showInBestPositionFor(e.dataContext)
            } else {
                doHandleAllAsync(e, project, Context(files, null))
            }
        }

        data class Context<T>(
            val files: List<PsiFile>,
            val data: T?
        )
    }

    abstract class WithLocalePopupAndPopup<T> : ManipulateLocalisationActionBase<WithLocalePopupAndPopup.Context<T>>() {
        protected open fun createLocalePopup(e: AnActionEvent, project: Project): ParadoxLocaleListPopup {
            val allLocales = ParadoxLocaleManager.getLocaleConfigs()
            return ParadoxLocaleListPopup(allLocales)
        }

        protected abstract fun createPopup(e: AnActionEvent, project: Project, callback: (T) -> Unit): JBPopup?

        final override fun doInvokeAll(e: AnActionEvent, project: Project, files: List<PsiFile>) {
            val localePopup = createLocalePopup(e, project)
            localePopup.doFinalStep action@{
                val selected = localePopup.selectedLocale ?: return@action
                val popup = createPopup(e, project) {
                    doHandleAllAsync(e, project, Context(files, selected, it))
                }
                if (popup != null) {
                    popup.showInBestPositionFor(e.dataContext)
                } else {
                    doHandleAllAsync(e, project, Context(files, selected, null))
                }
            }
            JBPopupFactory.getInstance().createListPopup(localePopup).showInBestPositionFor(e.dataContext)
        }

        data class Context<T>(
            val files: List<PsiFile>,
            val selectedLocale: CwtLocaleConfig,
            val data: T?
        )
    }

    object Messages {
        fun success(processed: Int) = PlsBundle.message("action.manipulateLocalisation.status.0", processed)
        fun failed(processed: Int) = PlsBundle.message("action.manipulateLocalisation.status.1", processed)
        fun partialSuccess(processed: Int) = PlsBundle.message("action.manipulateLocalisation.status.2", processed)
    }
}
