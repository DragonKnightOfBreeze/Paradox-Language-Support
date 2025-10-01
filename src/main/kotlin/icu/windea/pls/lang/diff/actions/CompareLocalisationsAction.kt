package icu.windea.pls.lang.diff.actions

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.actions.impl.GoToChangePopupBuilder
import com.intellij.diff.chains.DiffRequestChain
import com.intellij.diff.chains.DiffRequestProducer
import com.intellij.diff.chains.DiffRequestSelectionChain
import com.intellij.diff.chains.SimpleDiffRequestChain
import com.intellij.diff.contents.DocumentContent
import com.intellij.diff.requests.DiffRequest
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.diff.util.DiffUserDataKeys
import com.intellij.diff.util.Side
import com.intellij.notification.NotificationType
import com.intellij.openapi.ListSelection
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.psi.PsiFile
import com.intellij.util.Consumer
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.icon
import icu.windea.pls.core.isSamePosition
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.PlsKeys
import icu.windea.pls.lang.actions.editor
import icu.windea.pls.lang.diff.FileDocumentFragmentContent
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.lang.util.psi.ParadoxPsiFinder
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.qualifiedName
import java.awt.Color
import java.util.*
import javax.swing.Icon

/**
 * 将当前本地化与包括当前本地化的只读副本在内的相同名称的本地化进行DIFF。
 *
 * - 忽略直接位于游戏或模组入口目录下的文件。
 * - TODO 按照覆盖顺序进行排序。
 */
class CompareLocalisationsAction : ParadoxShowDiffAction() {
    private fun findFile(e: AnActionEvent): VirtualFile? {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
            ?: return null
        if (file.isDirectory) return null
        if (file.fileType !is ParadoxLocalisationFileType) return null
        val fileInfo = file.fileInfo ?: return null
        if (fileInfo.rootInfo !is ParadoxRootInfo.MetadataBased) return null
        if (fileInfo.path.length <= 1) return null // 忽略直接位于游戏或模组入口目录下的文件
        // val gameType = fileInfo.rootInfo.gameType
        // val path = fileInfo.path.path
        return file
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationProperty? {
        return ParadoxPsiFinder.findLocalisation(file, offset)?.takeIf { it.type != null }
    }

    private fun findElement(e: AnActionEvent): ParadoxLocalisationProperty? {
        val element = e.getData(CommonDataKeys.PSI_ELEMENT)
        if (element is ParadoxLocalisationProperty && element.type != null) return element
        return null
    }

    override fun update(e: AnActionEvent) {
        // 基于插件设置判断是否需要显示在编辑器悬浮工具栏中
        if (e.place == ActionPlaces.CONTEXT_TOOLBAR && !PlsFacade.getSettings().others.showEditorContextToolbar) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        // 出于性能原因，目前不在update方法中判断是否不存在重载/被重载的情况
        val presentation = e.presentation
        presentation.isVisible = false
        presentation.isEnabled = false
        val project = e.project ?: return
        val file = findFile(e) ?: return
        var localisation = findElement(e)
        if (localisation == null) {
            presentation.isVisible = true
            val editor = e.editor ?: return
            val offset = editor.caretModel.offset
            val psiFile = file.toPsiFile(project) ?: return
            localisation = findElement(psiFile, offset)
        }
        presentation.isEnabledAndVisible = localisation != null
    }

    override fun getDiffRequestChain(e: AnActionEvent): DiffRequestChain? {
        var localisation = findElement(e)
        if (localisation == null) {
            val project = e.project ?: return null
            val file = findFile(e) ?: return null
            val editor = e.editor ?: return null
            val offset = editor.caretModel.offset
            val psiFile = file.toPsiFile(project) ?: return null
            localisation = findElement(psiFile, offset)
        }
        if (localisation == null) return null
        val psiFile = localisation.containingFile
        val file = psiFile.virtualFile
        val project = psiFile.project
        val localisationName = localisation.name
        val localisations = Collections.synchronizedList(mutableListOf<ParadoxLocalisationProperty>())
        runWithModalProgressBlocking(project, PlsBundle.message("diff.compare.localisations.collect.title")) {
            readAction {
                val selector = selector(project, file).localisation()
                val result = ParadoxLocalisationSearch.search(localisationName, selector).findAll()
                localisations.addAll(result)
            }
        }
        if (localisations.size <= 1) {
            // unexpected, should not be empty here
            PlsCoreManager.createNotification(
                NotificationType.INFORMATION,
                PlsBundle.message("diff.compare.localisations.content.title.info.1")
            ).notify(project)
            return null
        }

        val editor = e.editor
        val contentFactory = DiffContentFactory.getInstance()

        val windowTitle = getWindowsTitle(localisation) ?: return null
        val contentTitle = getContentTitle(localisation) ?: return null
        val documentContent = contentFactory.createDocument(project, file) ?: return null
        val content = createContent(contentFactory, project, documentContent, localisation)

        var index = 0
        var currentIndex = 0
        val producers = runReadAction {
            localisations.mapNotNull { otherLocalisation ->
                val otherPsiFile = otherLocalisation.containingFile ?: return@mapNotNull null
                val locale = selectLocale(otherPsiFile) ?: return@mapNotNull null
                val otherFile = otherPsiFile.virtualFile ?: return@mapNotNull null
                val isSamePosition = localisation isSamePosition otherLocalisation
                val otherContentTitle = when {
                    isSamePosition -> getContentTitle(otherLocalisation, true)
                    else -> getContentTitle(otherLocalisation)
                } ?: return@mapNotNull null
                var isCurrent = false
                var readonly = false
                val otherContent = when {
                    isSamePosition -> {
                        isCurrent = true
                        readonly = true
                        val otherDocument = EditorFactory.getInstance().createDocument(documentContent.document.text)
                        val otherDocumentContent = contentFactory.create(project, otherDocument, content.highlightFile)
                        createContent(contentFactory, project, otherDocumentContent, localisation)
                    }
                    else -> {
                        val otherDocumentContent = contentFactory.createDocument(project, otherFile) ?: return@mapNotNull null
                        createContent(contentFactory, project, otherDocumentContent, otherLocalisation)
                    }
                }
                if (isCurrent) currentIndex = index
                if (readonly) otherContent.putUserData(DiffUserDataKeys.FORCE_READ_ONLY, true)
                index++
                val icon = otherLocalisation.icon
                val request = SimpleDiffRequest(windowTitle, content, otherContent, contentTitle, otherContentTitle)
                // 窗口定位到当前光标位置
                if (editor != null) {
                    val currentLine = editor.caretModel.logicalPosition.line
                    request.putUserData(DiffUserDataKeys.SCROLL_TO_LINE, Pair.create(Side.LEFT, currentLine))
                }
                MyRequestProducer(request, otherLocalisation.name, locale, otherFile, icon, isCurrent)
            }
        }
        val defaultIndex = getDefaultIndex(producers, currentIndex)
        return MyDiffRequestChain(producers, defaultIndex)
    }

    private fun createContent(contentFactory: DiffContentFactory, project: Project, documentContent: DocumentContent, localisation: ParadoxLocalisationProperty): DocumentContent {
        return createTempContent(contentFactory, project, documentContent, localisation)
            ?: createFragment(contentFactory, project, documentContent, localisation)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun createTempContent(contentFactory: DiffContentFactory, project: Project, documentContent: DocumentContent, localisation: ParadoxLocalisationProperty): DocumentContent? {
        // 创建临时文件
        // val file = localisation.containingFile ?: return null
        val fileInfo = documentContent.highlightFile?.fileInfo ?: return null
        val localeConfig = selectLocale(localisation) ?: ParadoxLocaleManager.getPreferredLocaleConfig()
        val text = localisation.text
        val tempFile = runWriteAction { ParadoxFileManager.createLightFile(UUID.randomUUID().toString(), text, fileInfo) }
        tempFile.putUserData(PlsKeys.injectedLocaleConfig, localeConfig)
        // return contentFactory.createDocument(project, tempFile)
        return FileDocumentFragmentContent(project, documentContent, localisation.textRange, tempFile)
    }

    private fun createFragment(contentFactory: DiffContentFactory, project: Project, documentContent: DocumentContent, localisation: ParadoxLocalisationProperty): DocumentContent {
        return contentFactory.createFragment(project, documentContent, localisation.textRange)
    }

    private fun getWindowsTitle(localisation: ParadoxLocalisationProperty): String? {
        val name = localisation.name
        val file = localisation.containingFile ?: return null
        val fileInfo = file.fileInfo ?: return null
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
        return PlsBundle.message("diff.compare.localisations.dialog.title", name, fileInfo.path, rootInfo.qualifiedName, rootInfo.entryPath)
    }

    private fun getContentTitle(localisation: ParadoxLocalisationProperty, original: Boolean = false): String? {
        val name = localisation.name
        val file = localisation.containingFile ?: return null
        val fileInfo = file.fileInfo ?: return null
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
        return when {
            original -> PlsBundle.message("diff.compare.localisations.originalContent.title", name, fileInfo.path, rootInfo.qualifiedName, rootInfo.entryPath)
            else -> PlsBundle.message("diff.compare.localisations.content.title", name, fileInfo.path, rootInfo.qualifiedName, rootInfo.entryPath)
        }
    }

    class MyDiffRequestChain(
        producers: List<DiffRequestProducer>,
        defaultIndex: Int = 0
    ) : UserDataHolderBase(), DiffRequestSelectionChain, GoToChangePopupBuilder.Chain {
        private val listSelection = ListSelection.createAt(producers, defaultIndex)

        override fun getListSelection() = listSelection

        override fun createGoToChangeAction(onSelected: Consumer<in Int>, defaultSelection: Int): AnAction {
            return MyGotoChangePopupAction(this, onSelected, defaultSelection)
        }
    }

    class MyRequestProducer(
        request: DiffRequest,
        val otherLocalisationName: String,
        val locale: CwtLocaleConfig,
        val otherFile: VirtualFile,
        val icon: Icon,
        val isCurrent: Boolean
    ) : SimpleDiffRequestChain.DiffRequestProducerWrapper(request) {
        override fun getName(): String {
            val fileInfo = otherFile.fileInfo ?: return super.getName()
            val rootInfo = fileInfo.rootInfo
            if (rootInfo !is ParadoxRootInfo.MetadataBased) return super.getName()
            return PlsBundle.message("diff.compare.localisations.popup.name", otherLocalisationName, locale.id, fileInfo.path, rootInfo.qualifiedName, rootInfo.entryPath)
        }
    }

    class MyGotoChangePopupAction(
        val chain: MyDiffRequestChain,
        val onSelected: Consumer<in Int>,
        val defaultSelection: Int
    ) : GoToChangePopupBuilder.BaseGoToChangePopupAction() {
        override fun canNavigate(): Boolean {
            return chain.requests.size > 1
        }

        override fun createPopup(e: AnActionEvent): JBPopup {
            return JBPopupFactory.getInstance().createListPopup(Popup())
        }

        private inner class Popup : BaseListPopupStep<DiffRequestProducer>(
            PlsBundle.message("diff.compare.localisations.popup.title"),
            chain.requests
        ) {
            init {
                defaultOptionIndex = defaultSelection
            }

            override fun getIconFor(value: DiffRequestProducer) = (value as MyRequestProducer).icon

            override fun getTextFor(value: DiffRequestProducer) = value.name

            // com.intellij.find.actions.ShowUsagesTableCellRenderer.getTableCellRendererComponent L205
            @Suppress("UseJBColor")
            override fun getBackgroundFor(value: DiffRequestProducer) = if ((value as MyRequestProducer).isCurrent) Color(0x808080) else null

            override fun isSpeedSearchEnabled() = true

            override fun onChosen(selectedValue: DiffRequestProducer, finalChoice: Boolean) = doFinalStep {
                val selectedIndex = chain.requests.indexOf(selectedValue)
                onSelected.consume(selectedIndex)
            }
        }
    }
}
