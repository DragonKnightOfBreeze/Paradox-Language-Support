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
import icu.windea.pls.core.editor
import icu.windea.pls.core.icon
import icu.windea.pls.core.isSamePosition
import icu.windea.pls.core.orNull
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.ide.notification.PlsNotificationGroups
import icu.windea.pls.lang.definitionCandidateInfo
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.definitionInjectionInfo
import icu.windea.pls.lang.diff.FileDocumentFragmentContent
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.search.ParadoxDefinitionInjectionSearch
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.model.ParadoxDefinitionCandidateInfo
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.constraints.ParadoxPathConstraint
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptProperty
import java.awt.Color
import javax.swing.Icon

/**
 * 对当前定义与包括其只读副本在内的拥有相同名称和主要类型的定义，或者相关注入，进行差异比较。
 *
 * - 忽略直接位于游戏或模组的根目录下的文件。
 * - 按照覆盖方式进行排序。
 * - 仅适用于支持定义注入的游戏类型，同时可能支持定义注入的定义类型。
 */
class CompareDefinitionsOrInjectionsAction : ParadoxShowDiffAction() {
    private fun findSourceFile(e: AnActionEvent): VirtualFile? {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
        if (file.isDirectory) return null
        if (file.fileType !is ParadoxScriptFileType) return null
        val fileInfo = file.fileInfo ?: return null
        if (fileInfo.rootInfo !is ParadoxRootInfo.MetadataBased) return null
        if (fileInfo.isTopFromRoot()) return null // 忽略直接位于游戏或模组的根目录下的文件
        val project = e.project ?: return null
        val psiFile = file.toPsiFile(project) ?: return null
        if (!ParadoxPsiFileMatcher.isScriptFile(psiFile, ParadoxPathConstraint.AcceptDefinitionInjection)) return null
        if (!ParadoxDefinitionInjectionManager.isSupported(fileInfo.rootInfo.gameType)) return null // 忽略游戏类型不支持的情况
        return file
    }

    private fun findElement(e: AnActionEvent, file: VirtualFile, project: Project): ParadoxScriptProperty? {
        val element = e.getData(CommonDataKeys.PSI_ELEMENT)
        if (element is ParadoxScriptProperty && element.definitionInfo != null) return element
        if (element is ParadoxScriptProperty && element.definitionInjectionInfo != null) return element

        e.presentation.isVisible = true
        val editor = e.editor ?: return null
        val offset = editor.caretModel.offset
        val psiFile = file.toPsiFile(project) ?: return null
        ParadoxPsiFileManager.findDefinition(psiFile, offset)?.let { if (it is ParadoxScriptProperty) return it }
        ParadoxPsiFileManager.findDefinitionInjection(psiFile, offset)?.let { return it }
        return null
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
        val definitionCandidates = mutableListOf<ParadoxScriptProperty>()
        val definitionInjectionInfo = element.definitionInjectionInfo
        val definitionInfo = if (definitionInjectionInfo != null) null else element.definitionInfo
        val definitionCandidateInfo = definitionInjectionInfo ?: definitionInfo ?: return null
        runWithModalProgressBlocking(project, ChronicleBundle.message("diff.compare.definitionsOrInjections.collect.title")) {
            val definitionName = definitionInjectionInfo?.target?.orNull() ?: definitionInfo?.name?.orNull()
            val definitionType = definitionInjectionInfo?.type?.orNull() ?: definitionInfo?.type?.orNull()
            if (definitionName != null && definitionType != null) {
                readAction {
                    val selector = ParadoxDefinitionSearch.selector(project, file)
                    // pass main type only
                    val result = ParadoxDefinitionSearch.searchElement(definitionName, definitionType, selector).findAll().filterIsInstance<ParadoxScriptProperty>()
                    definitionCandidates.addAll(result)
                }
                readAction {
                    val selector = ParadoxDefinitionInjectionSearch.selector(project, file)
                    val result = ParadoxDefinitionInjectionSearch.searchElement(null, definitionName, definitionType, selector).findAll()
                    definitionCandidates.addAll(result)
                }
            }
        }
        if (definitionCandidates.size <= 1) {
            // unexpected
            val content = ChronicleBundle.message("diff.compare.definitionsOrInjections.content.notification.empty")
            PlsNotificationGroups.diff().createNotification(content, NotificationType.INFORMATION).notify(project)
            return null
        }

        val editor = e.editor
        val contentFactory = DiffContentFactory.getInstance()

        val windowTitle = getWindowsTitle(element, definitionCandidateInfo) ?: return null
        val contentTitle = getContentTitle(element, definitionCandidateInfo) ?: return null
        val documentContent = contentFactory.createDocument(project, file) ?: return null
        val content = createContent(contentFactory, project, documentContent, element)

        var index = 0
        var currentIndex = 0
        val producers = runSmartReadAction {
            definitionCandidates.mapNotNull { otherDefinition ->
                val otherDefinitionCandidateInfo = otherDefinition.definitionCandidateInfo ?: return@mapNotNull null
                val otherPsiFile = otherDefinition.containingFile ?: return@mapNotNull null
                val otherFile = otherPsiFile.virtualFile ?: return@mapNotNull null

                val isSamePosition = element isSamePosition otherDefinition
                val isCurrent = isSamePosition
                val isReadonly = isSamePosition

                val otherContentTitle = when {
                    isSamePosition -> getContentTitle(otherDefinition, otherDefinitionCandidateInfo, true)
                    else -> getContentTitle(otherDefinition, otherDefinitionCandidateInfo)
                } ?: return@mapNotNull null
                val otherContent = when {
                    isSamePosition -> {
                        val otherDocument = EditorFactory.getInstance().createDocument(documentContent.document.text)
                        val otherDocumentContent = contentFactory.create(project, otherDocument, content.highlightFile)
                        createContent(contentFactory, project, otherDocumentContent, element)
                    }
                    else -> {
                        val otherDocumentContent = contentFactory.createDocument(project, otherFile) ?: return@mapNotNull null
                        createContent(contentFactory, project, otherDocumentContent, otherDefinition)
                    }
                }
                if (isCurrent) currentIndex = index
                if (isReadonly) otherContent.putUserData(DiffUserDataKeys.FORCE_READ_ONLY, true)
                index++
                val icon = otherDefinition.icon
                val request = SimpleDiffRequest(windowTitle, content, otherContent, contentTitle, otherContentTitle)
                // 窗口定位到当前光标位置
                if (editor != null) {
                    val currentLine = editor.caretModel.logicalPosition.line
                    request.putUserData(DiffUserDataKeys.SCROLL_TO_LINE, Pair.create(Side.LEFT, currentLine))
                }
                MyRequestProducer(request, otherDefinitionCandidateInfo, otherFile, icon, isCurrent)
            }
        }
        val defaultIndex = getDefaultIndex(producers, currentIndex)
        return MyDiffRequestChain(producers, defaultIndex)
    }

    private fun createContent(contentFactory: DiffContentFactory, project: Project, documentContent: DocumentContent, definitionCandidate: ParadoxScriptProperty): DocumentContent {
        return createTempContent(contentFactory, project, documentContent, definitionCandidate)
            ?: createFragment(contentFactory, project, documentContent, definitionCandidate)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun createTempContent(contentFactory: DiffContentFactory, project: Project, documentContent: DocumentContent, definitionCandidate: ParadoxScriptProperty): DocumentContent? {
        // 创建临时文件
        val file = documentContent.highlightFile ?: return null
        val fileInfo = file.fileInfo ?: return null
        val text = definitionCandidate.text
        val tempFile = runWriteAction { ParadoxFileManager.createLightFile(file.name, text, fileInfo) }
        // 这里目前并不需要注入 rootKeys，因为定义注入只能位于文件顶层
        // ParadoxAnalysisInjectionManager.injectRootKeys(tempFile, emptyList())
        // return contentFactory.createDocument(project, tempFile)
        return FileDocumentFragmentContent(project, documentContent, definitionCandidate.textRange, tempFile)
    }

    private fun createFragment(contentFactory: DiffContentFactory, project: Project, documentContent: DocumentContent, definitionCandidate: ParadoxScriptProperty): DocumentContent {
        return contentFactory.createFragment(project, documentContent, definitionCandidate.textRange)
    }

    private fun getWindowsTitle(definitionCandidate: ParadoxScriptProperty, definitionCandidateInfo: ParadoxDefinitionCandidateInfo): String? {
        val file = definitionCandidate.containingFile ?: return null
        val fileInfo = file.fileInfo ?: return null
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
        val path = fileInfo.path
        val qualifiedName = rootInfo.qualifiedName
        when (definitionCandidateInfo) {
            is ParadoxDefinitionInfo -> {
                // NOTE 2.1.2 目前的方案：仅显示定义的名字、类型（不包括子类型）、路径信息、游戏或模组的名字和版本信息
                val name = definitionCandidateInfo.name.or.anonymous()
                val type = definitionCandidateInfo.type
                return ChronicleBundle.message("diff.compare.definitionsOrInjections.dialog.title", name, type, path, qualifiedName)
            }
            is ParadoxDefinitionInjectionInfo -> {
                // NOTE 2.1.2 目前的方案：仅显示定义注入的表达式、类型（不包括子类型）、路径信息、游戏或模组的名字和版本信息
                val expression = definitionCandidateInfo.expression
                val type = definitionCandidateInfo.type.orEmpty()
                return ChronicleBundle.message("diff.compare.definitionsOrInjections.dialog.title", expression, type, path, qualifiedName)
            }
        }
    }

    private fun getContentTitle(definitionCandidate: ParadoxScriptProperty, definitionCandidateInfo: ParadoxDefinitionCandidateInfo, original: Boolean = false): String? {
        val file = definitionCandidate.containingFile ?: return null
        val fileInfo = file.fileInfo ?: return null
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
        val path = fileInfo.path
        val qualifiedName = rootInfo.qualifiedName
        when (definitionCandidateInfo) {
            is ParadoxDefinitionInfo -> {
                val name = definitionCandidateInfo.name.or.anonymous()
                val type = definitionCandidateInfo.type
                // NOTE 2.1.2 目前的方案：仅显示定义的名字、类型（不包括子类型）、路径信息、游戏或模组的名字和版本信息
                return when {
                    original -> ChronicleBundle.message("diff.compare.definitionsOrInjections.originalContent.title", name, type, path, qualifiedName)
                    else -> ChronicleBundle.message("diff.compare.definitionsOrInjections.content.title", name, type, path, qualifiedName)
                }
            }
            is ParadoxDefinitionInjectionInfo -> {
                // NOTE 2.1.2 目前的方案：仅显示定义注入的表达式、类型（不包括子类型）、路径信息、游戏或模组的名字和版本信息
                val expression = definitionCandidateInfo.expression
                val type = definitionCandidateInfo.type.orEmpty()
                return when {
                    original -> ChronicleBundle.message("diff.compare.definitionsOrInjections.originalContent.title", expression, type, path, qualifiedName)
                    else -> ChronicleBundle.message("diff.compare.definitionsOrInjections.content.title", expression, type, path, qualifiedName)
                }
            }
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
        val otherDefinitionCandidateInfo: ParadoxDefinitionCandidateInfo,
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
            when (otherDefinitionCandidateInfo) {
                is ParadoxDefinitionInfo -> {
                    // NOTE 2.1.2 目前的方案：仅显示定义的名字、类型（不包括子类型）、路径信息、游戏或模组的名字和版本信息
                    val name = otherDefinitionCandidateInfo.name.or.anonymous()
                    val type = otherDefinitionCandidateInfo.type
                    return ChronicleBundle.message("diff.compare.definitionsOrInjections.popup.name", name, type, path, qualifiedName)
                }
                is ParadoxDefinitionInjectionInfo -> {
                    // NOTE 2.1.2 目前的方案：仅显示定义注入的表达式、类型（不包括子类型）、路径信息、游戏或模组的名字和版本信息
                    val expression = otherDefinitionCandidateInfo.expression
                    val type = otherDefinitionCandidateInfo.type.orEmpty()
                    return ChronicleBundle.message("diff.compare.definitionsOrInjections.popup.name", expression, type, path, qualifiedName)
                }
            }
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
            ChronicleBundle.message("diff.compare.definitionsOrInjections.popup.title"),
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
