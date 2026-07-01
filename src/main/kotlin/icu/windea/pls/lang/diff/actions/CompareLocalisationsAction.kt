package icu.windea.pls.lang.diff.actions

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.actions.impl.GoToChangePopupBuilder
import com.intellij.diff.chains.DiffRequestChain
import com.intellij.diff.contents.DocumentContent
import com.intellij.diff.requests.DiffRequest
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.diff.util.DiffUserDataKeys
import com.intellij.diff.util.Side
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.util.Pair
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.util.Consumer
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.editor
import icu.windea.pls.core.icon
import icu.windea.pls.core.isSamePosition
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.ide.notification.ChronicleNotificationGroups
import icu.windea.pls.lang.analysis.ParadoxAnalysisInjectionManager
import icu.windea.pls.lang.diff.FileDocumentFragmentContent
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxRootInfo
import java.awt.Color
import javax.swing.Icon

/**
 * 对当前本地化与包括其只读副本在内的拥有相同名称的本地化进行差异比较。
 *
 * - 忽略直接位于游戏或模组的根目录下的文件。
 * - 按照覆盖方式进行排序。
 */
class CompareLocalisationsAction : ParadoxShowDiffAction() {
    private fun findSourceFile(e: AnActionEvent): VirtualFile? {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
        if (file.isDirectory) return null
        if (file.fileType !is ParadoxLocalisationFileType) return null
        val fileInfo = file.fileInfo ?: return null
        if (fileInfo.rootInfo !is ParadoxRootInfo.MetadataBased) return null
        if (fileInfo.isTopFromRoot()) return null // 忽略直接位于游戏或模组的根目录下的文件
        return file
    }

    private fun findElement(e: AnActionEvent, file: VirtualFile, project: Project): ParadoxLocalisationProperty? {
        val element = e.getData(CommonDataKeys.PSI_ELEMENT)
        if (element is ParadoxLocalisationProperty && element.type != null) return element

        val editor = e.editor ?: return null
        val offset = editor.caretModel.offset
        val psiFile = file.toPsiFile(project) ?: return null
        return ParadoxPsiFileManager.findLocalisation(psiFile, offset)?.takeIf { it.type != null }
    }

    override fun update(e: AnActionEvent) {
        // 基于插件设置判断是否需要显示在编辑器悬浮工具栏中
        if (e.place == ActionPlaces.CONTEXT_TOOLBAR && !PlsSettings.getInstance().state.others.showEditorContextToolbar) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        // 出于性能原因，目前不在 update 方法中判断是否不存在重载/被重载的情况
        e.presentation.isEnabledAndVisible = false
        val project = e.project ?: return
        val sourceFile = findSourceFile(e) ?: return
        val element = findElement(e, sourceFile, project)
        e.presentation.isEnabledAndVisible = element != null
    }

    override fun getDiffRequestChain(e: AnActionEvent): DiffRequestChain? {
        val project = e.project ?: return null
        val sourceFile = findSourceFile(e) ?: return null
        val element = findElement(e, sourceFile, project) ?: return null
        val file = element.containingFile?.virtualFile ?: return null
        val localisationName = element.name
        val localisations = mutableListOf<ParadoxLocalisationProperty>()
        runWithModalProgressBlocking(project, ChronicleBundle.message("diff.compare.localisations.collect.title")) {
            readAction {
                val selector = ParadoxLocalisationSearch.selector(project, file)
                val result = ParadoxLocalisationSearch.searchNormal(localisationName, selector).findAll()
                localisations.addAll(result)
            }
        }
        if (localisations.size <= 1) {
            // unexpected
            val content = ChronicleBundle.message("diff.compare.localisations.content.notification.empty")
            ChronicleNotificationGroups.diff().createNotification(content, NotificationType.INFORMATION).notify(project)
            return null
        }

        val editor = e.editor
        val contentFactory = DiffContentFactory.getInstance()

        val windowTitle = getWindowsTitle(element) ?: return null
        val contentTitle = getContentTitle(element) ?: return null
        val documentContent = contentFactory.createDocument(project, file) ?: return null
        val content = createContent(contentFactory, project, documentContent, element)

        var index = 0
        var currentIndex = 0
        val producers = runSmartReadAction {
            localisations.mapNotNull { otherLocalisation ->
                val otherPsiFile = otherLocalisation.containingFile ?: return@mapNotNull null
                val locale = selectLocale(otherPsiFile) ?: return@mapNotNull null
                val otherFile = otherPsiFile.virtualFile ?: return@mapNotNull null

                val isSamePosition = element isSamePosition otherLocalisation
                val isCurrent = isSamePosition
                val isReadonly = isSamePosition

                val otherContentTitle = when {
                    isSamePosition -> getContentTitle(otherLocalisation, true)
                    else -> getContentTitle(otherLocalisation)
                } ?: return@mapNotNull null
                val otherContent = when {
                    isSamePosition -> {
                        val otherDocument = EditorFactory.getInstance().createDocument(documentContent.document.text)
                        val otherDocumentContent = contentFactory.create(project, otherDocument, content.highlightFile)
                        createContent(contentFactory, project, otherDocumentContent, element)
                    }
                    else -> {
                        val otherDocumentContent = contentFactory.createDocument(project, otherFile) ?: return@mapNotNull null
                        createContent(contentFactory, project, otherDocumentContent, otherLocalisation)
                    }
                }
                if (isCurrent) currentIndex = index
                if (isReadonly) otherContent.putUserData(DiffUserDataKeys.FORCE_READ_ONLY, true)
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
        val file = documentContent.highlightFile ?: return null
        val fileInfo = file.fileInfo ?: return null
        val localeConfig = selectLocale(localisation) ?: ParadoxLocaleManager.getPreferredLocaleConfig()
        val text = localisation.text
        val tempFile = runWriteAction { ParadoxFileManager.createLightFile(file.name, text, fileInfo) }
        ParadoxAnalysisInjectionManager.injectLocaleConfig(tempFile, localeConfig)
        // return contentFactory.createDocument(project, tempFile)
        return FileDocumentFragmentContent(project, documentContent, localisation.textRange, tempFile)
    }

    private fun createFragment(contentFactory: DiffContentFactory, project: Project, documentContent: DocumentContent, localisation: ParadoxLocalisationProperty): DocumentContent {
        return contentFactory.createFragment(project, documentContent, localisation.textRange)
    }

    private fun getWindowsTitle(localisation: ParadoxLocalisationProperty): String? {
        val file = localisation.containingFile ?: return null
        val fileInfo = file.fileInfo ?: return null
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
        val path = fileInfo.path
        val qualifiedName = rootInfo.qualifiedName
        // NOTE 2.1.2 目前的方案：仅显示本地化的名字、路径信息、游戏或模组的名字和版本信息
        val name = localisation.name
        return ChronicleBundle.message("diff.compare.localisations.dialog.title", name, path, qualifiedName)
    }

    private fun getContentTitle(localisation: ParadoxLocalisationProperty, original: Boolean = false): String? {
        val file = localisation.containingFile ?: return null
        val fileInfo = file.fileInfo ?: return null
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
        val path = fileInfo.path
        val qualifiedName = rootInfo.qualifiedName
        // NOTE 2.1.2 目前的方案：仅显示本地化的名字、路径信息、游戏或模组的名字和版本信息
        val name = localisation.name
        return when {
            original -> ChronicleBundle.message("diff.compare.localisations.originalContent.title", name, path, qualifiedName)
            else -> ChronicleBundle.message("diff.compare.localisations.content.title", name, path, qualifiedName)
        }
    }

    class MyDiffRequestChain(
        producers: List<ParadoxDiffRequestProducer>,
        defaultIndex: Int = 0
    ) : ParadoxDiffRequestChain(producers, defaultIndex) {
        override fun createGoToChangeAction(onSelected: Consumer<in Int>, defaultSelection: Int): AnAction {
            return MyGotoChangePopupAction(this, onSelected, defaultSelection)
        }
    }

    class MyRequestProducer(
        request: DiffRequest,
        val otherLocalisationName: String,
        val locale: CwtLocaleConfig,
        otherFile: VirtualFile,
        icon: Icon,
        isCurrent: Boolean
    ) : ParadoxDiffRequestProducer(request, otherFile, icon, isCurrent) {
        override fun getName(): String {
            return doGetName() ?: super.name
        }

        private fun doGetName(): String? {
            val fileInfo = otherFile.fileInfo ?: return null
            val rootInfo = fileInfo.rootInfo
            if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
            val path = fileInfo.path
            val qualifiedName = rootInfo.qualifiedName
            // NOTE 2.1.2 目前的方案：仅显示本地化的名字、路径信息、游戏或模组的名字和版本信息（这里还会显示语言环境信息）
            val name = otherLocalisationName
            val localeId = locale.id
            return ChronicleBundle.message("diff.compare.localisations.popup.name", name, localeId, path, qualifiedName)
        }
    }

    class MyGotoChangePopupAction(
        val chain: ParadoxDiffRequestChain,
        val onSelected: Consumer<in Int>,
        val defaultSelection: Int
    ) : GoToChangePopupBuilder.BaseGoToChangePopupAction() {
        override fun canNavigate(): Boolean {
            return chain.requests.size > 1
        }

        override fun createPopup(e: AnActionEvent): JBPopup {
            return JBPopupFactory.getInstance().createListPopup(Popup())
        }

        private inner class Popup : BaseListPopupStep<ParadoxDiffRequestProducer>(
            ChronicleBundle.message("diff.compare.localisations.popup.title"),
            chain.requests
        ) {
            init {
                defaultOptionIndex = defaultSelection
            }

            override fun getIconFor(value: ParadoxDiffRequestProducer) = value.icon

            override fun getTextFor(value: ParadoxDiffRequestProducer) = value.name

            // com.intellij.find.actions.ShowUsagesTableCellRenderer.getTableCellRendererComponent L205
            @Suppress("UseJBColor")
            override fun getBackgroundFor(value: ParadoxDiffRequestProducer) = if (value.isCurrent) Color(0x808080) else null

            override fun isSpeedSearchEnabled() = true

            override fun onChosen(selectedValue: ParadoxDiffRequestProducer, finalChoice: Boolean) = doFinalStep {
                val selectedIndex = chain.requests.indexOf(selectedValue)
                onSelected.consume(selectedIndex)
            }
        }
    }
}
