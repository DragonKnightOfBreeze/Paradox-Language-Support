package icu.windea.pls.tools.actions.localisation

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.ui.locale.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import kotlinx.coroutines.*

/**
 * 用于处理本地化的一类操作。
 *
 * * 应当支持在多个级别批量处理。
 * * 应当在开始处理之前弹出对话框，以确认是否真的要处理。
 */
abstract class ManipulateLocalisationActionBase : AnAction() {
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
        doInvokeAll(e, project, files)
    }

    protected open fun isAvailable(e: AnActionEvent, project: Project): Boolean {
        return true
    }

    protected open fun isValidFile(file: VirtualFile): Boolean {
        if (file.fileType !is ParadoxLocalisationFileType) return false
        if (file.fileInfo == null) return false
        if (PlsFileManager.isLightFile(file)) return false
        return true
    }

    protected open fun hasFiles(e: AnActionEvent, project: Project): Boolean {
        var r: PsiFile? = null
        PlsFileManager.processFiles(e, deep = true) p@{ file ->
            if (!isValidFile(file)) return@p true
            r = file.toPsiFile(project) ?: return@p true
            false
        }
        return r != null
    }

    protected open fun hasElements(e: AnActionEvent, project: Project): Boolean {
        var r: PsiElement? = null
        PlsFileManager.processFiles(e, deep = true) p@{ file ->
            if (!isValidFile(file)) return@p true
            val psiFile = file.toPsiFile(project) ?: return@p true
            if (psiFile !is ParadoxLocalisationFile) return@p true
            r = psiFile.children().filterIsInstance<ParadoxLocalisationPropertyList>().firstNotNullOfOrNull { propertyList ->
                propertyList.children().findIsInstance<ParadoxLocalisationProperty>()
            }
            false
        }
        return r != null
    }

    protected open fun findFiles(e: AnActionEvent, project: Project): List<PsiFile> {
        val project = e.project ?: return emptyList()
        val files = PlsFileManager.findFiles(e, deep = true) { file -> isValidFile(file) }
        return files.mapNotNull { it.toPsiFile(project) }
    }

    protected abstract fun doInvokeAll(e: AnActionEvent, project: Project, files: List<PsiFile>)

    abstract class Default : ManipulateLocalisationActionBase() {
        final override fun doInvokeAll(e: AnActionEvent, project: Project, files: List<PsiFile>) {
            val coroutineScope = PlsFacade.getCoroutineScope(project)
            coroutineScope.launch {
                doHandleAll(e, project, files)
            }
        }

        protected abstract suspend fun doHandleAll(e: AnActionEvent, project: Project, files: List<PsiFile>)
    }

    abstract class WithLocalePopup : ManipulateLocalisationActionBase() {
        protected open fun createLocalePopup(e: AnActionEvent, project: Project): ParadoxLocaleListPopup {
            val allLocales = ParadoxLocaleManager.getLocaleConfigs()
            return ParadoxLocaleListPopup(allLocales)
        }

        final override fun doInvokeAll(e: AnActionEvent, project: Project, files: List<PsiFile>) {
            val localePopup = createLocalePopup(e, project)
            localePopup.doFinalStep action@{
                val selected = localePopup.selectedLocale ?: return@action
                doInvokeAll(e, project, files, selected)
            }
            JBPopupFactory.getInstance().createListPopup(localePopup).showInBestPositionFor(e.dataContext)
        }

        private fun doInvokeAll(e: AnActionEvent, project: Project, files: List<PsiFile>, selectedLocale: CwtLocaleConfig) {
            val coroutineScope = PlsFacade.getCoroutineScope(project)
            coroutineScope.launch {
                doHandleAll(e, project, files, selectedLocale)
            }
        }

        protected abstract suspend fun doHandleAll(e: AnActionEvent, project: Project, files: List<PsiFile>, selectedLocale: CwtLocaleConfig)
    }

    abstract class WithPopup<T> : ManipulateLocalisationActionBase() {
        protected abstract fun createPopup(e: AnActionEvent, project: Project, callback: (T) -> Unit): JBPopup?

        final override fun doInvokeAll(e: AnActionEvent, project: Project, files: List<PsiFile>) {
            val popup = createPopup(e, project) {
                doInvokeAll(e, project, files, it)
            }
            if (popup != null) {
                popup.showInBestPositionFor(e.dataContext)
            } else {
                doInvokeAll(e, project, files, null)
            }
        }

        private fun doInvokeAll(e: AnActionEvent, project: Project, files: List<PsiFile>, data: T?) {
            val coroutineScope = PlsFacade.getCoroutineScope(project)
            coroutineScope.launch {
                doHandleAll(e, project, files, data)
            }
        }

        protected abstract suspend fun doHandleAll(e: AnActionEvent, project: Project, files: List<PsiFile>, data: T?)
    }

    abstract class WithLocalePopupAndPopup<T> : ManipulateLocalisationActionBase() {
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
                    doInvokeAll(e, project, files, selected, it)
                }
                if (popup != null) {
                    popup.showInBestPositionFor(e.dataContext)
                } else {
                    doInvokeAll(e, project, files, selected, null)
                }
            }
            JBPopupFactory.getInstance().createListPopup(localePopup).showInBestPositionFor(e.dataContext)
        }

        private fun doInvokeAll(e: AnActionEvent, project: Project, files: List<PsiFile>, selected: CwtLocaleConfig, data: T?) {
            val coroutineScope = PlsFacade.getCoroutineScope(project)
            coroutineScope.launch {
                doHandleAll(e, project, files, selected, data)
            }
        }

        protected abstract suspend fun doHandleAll(e: AnActionEvent, project: Project, files: List<PsiFile>, selectedLocale: CwtLocaleConfig, data: T?)
    }
}
