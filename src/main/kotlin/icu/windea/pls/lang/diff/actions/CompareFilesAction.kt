package icu.windea.pls.lang.diff.actions

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.actions.impl.GoToChangePopupBuilder
import com.intellij.diff.chains.DiffRequestChain
import com.intellij.diff.chains.DiffRequestProducer
import com.intellij.diff.chains.DiffRequestSelectionChain
import com.intellij.diff.chains.SimpleDiffRequestChain
import com.intellij.diff.contents.DocumentContent
import com.intellij.diff.contents.FileContent
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
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.util.Consumer
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.actions.editor
import icu.windea.pls.lang.diff.FileDocumentReadonlyContent
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.selector.file
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.qualifiedName
import java.awt.Color
import java.util.*
import javax.swing.Icon

/**
 * 将当前文件与包括当前文件的只读副本在内的相同路径的文件进行DIFF。如果是本地化文件的话也忽略路径中的语言环境。
 *
 * - 可以用于比较二进制文件。（如DDS图片）
 * - TODO 按照覆盖顺序进行排序。
 */
class CompareFilesAction : ParadoxShowDiffAction() {
    private fun findFile(e: AnActionEvent): VirtualFile? {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.singleOrNull()
            ?: e.getData(CommonDataKeys.VIRTUAL_FILE)
            ?: return null
        if (file.isDirectory) return null
        val fileInfo = file.fileInfo ?: return null
        if (fileInfo.rootInfo !is ParadoxRootInfo.MetadataBased) return null
        if (fileInfo.path.length <= 1) return null //忽略直接位于游戏或模组入口目录下的文件
        //val gameType = fileInfo.rootInfo.gameType
        //val path = fileInfo.path.path
        return file
    }

    override fun update(e: AnActionEvent) {
        //基于插件设置判断是否需要显示在编辑器悬浮工具栏中
        if (e.place == ActionPlaces.CONTEXT_TOOLBAR && !PlsFacade.getSettings().others.showEditorContextToolbar) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        //出于性能原因，目前不在update方法中判断是否不存在重载/被重载的情况
        val presentation = e.presentation
        presentation.isEnabledAndVisible = false
        val file = findFile(e)
        presentation.isEnabledAndVisible = file != null
    }

    override fun getDiffRequestChain(e: AnActionEvent): DiffRequestChain? {
        val project = e.project ?: return null
        val file = findFile(e) ?: return null
        val path = file.fileInfo?.path?.path ?: return null
        val virtualFiles = Collections.synchronizedList(mutableListOf<VirtualFile>())
        runWithModalProgressBlocking<Unit>(project, PlsBundle.message("diff.compare.files.collect.title")) {
            readAction {
                val selector = selector(project, file).file()
                val result = ParadoxFilePathSearch.search(path, null, selector, ignoreLocale = true).findAll()
                virtualFiles.addAll(result)
            }
        }
        if (virtualFiles.size <= 1) {
            //unexpected, should not be empty here
            PlsCoreManager.createNotification(
                NotificationType.INFORMATION,
                PlsBundle.message("diff.compare.files.content.title.info.1")
            ).notify(project)
            return null
        }

        val editor = e.editor
        val contentFactory = DiffContentFactory.getInstance()

        val windowTitle = getWindowsTitle(file) ?: return null
        val contentTitle = getContentTitle(file) ?: return null
        val binary = file.fileType.isBinary
        val content = when {
            binary -> createBinaryContent(contentFactory, project, file)
            else -> createContent(contentFactory, project, file)
        } ?: return null
        if (binary) content.putUserData(DiffUserDataKeys.FORCE_READ_ONLY, true)

        var index = 0
        var currentIndex = 0
        val producers = runReadAction {
            virtualFiles.mapNotNull { otherFile ->
                if (file.fileType != otherFile.fileType) return@mapNotNull null
                val isSameFile = file == otherFile
                val otherContentTitle = when {
                    isSameFile -> getContentTitle(otherFile, true)
                    else -> getContentTitle(otherFile)
                } ?: return@mapNotNull null
                var isCurrent = false
                var readonly = false
                val otherContent = when {
                    binary -> {
                        if (isSameFile) isCurrent = true
                        readonly = true
                        createBinaryContent(contentFactory, project, otherFile)
                    }
                    isSameFile -> {
                        isCurrent = true
                        readonly = true
                        createTempContent(contentFactory, project, file)
                    }
                    else -> {
                        createContent(contentFactory, project, otherFile)
                    }
                } ?: return@mapNotNull null
                if (isCurrent) currentIndex = index
                if (readonly) otherContent.putUserData(DiffUserDataKeys.FORCE_READ_ONLY, true)
                index++
                val icon = otherFile.fileType.icon
                val request = SimpleDiffRequest(windowTitle, content, otherContent, contentTitle, otherContentTitle)
                //窗口定位到当前光标位置
                if (!binary && editor != null) {
                    val currentLine = editor.caretModel.logicalPosition.line
                    request.putUserData(DiffUserDataKeys.SCROLL_TO_LINE, Pair.create(Side.LEFT, currentLine))
                }
                MyRequestProducer(request, otherFile, icon, isCurrent)
            }
        }
        val defaultIndex = getDefaultIndex(producers, currentIndex)
        return MyDiffRequestChain(producers, defaultIndex)
    }

    private fun createBinaryContent(contentFactory: DiffContentFactory, project: Project, file: VirtualFile): FileContent? {
        return contentFactory.createFile(project, file)
    }

    private fun createContent(contentFactory: DiffContentFactory, project: Project, file: VirtualFile): DocumentContent? {
        return contentFactory.createDocument(project, file)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun createTempContent(contentFactory: DiffContentFactory, project: Project, file: VirtualFile): DocumentContent {
        //创建临时文件作为只读副本
        val tempFile = runWriteAction { ParadoxFileManager.createLightFile(UUID.randomUUID().toString(), file, project) }
        val document = runReadAction { FileDocumentManager.getInstance().getDocument(tempFile) }!!
        return FileDocumentReadonlyContent(project, document, tempFile, file)
    }

    private fun getWindowsTitle(file: VirtualFile): String? {
        val fileInfo = file.fileInfo ?: return null
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
        return PlsBundle.message("diff.compare.files.dialog.title", fileInfo.path, rootInfo.qualifiedName, rootInfo.entryPath)
    }

    private fun getContentTitle(file: VirtualFile, original: Boolean = false): String? {
        val fileInfo = file.fileInfo ?: return null
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
        return when {
            original -> PlsBundle.message("diff.compare.files.originalContent.title", fileInfo.path, rootInfo.qualifiedName, rootInfo.entryPath)
            else -> PlsBundle.message("diff.compare.files.content.title", fileInfo.path, rootInfo.qualifiedName, rootInfo.entryPath)
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
        val otherFile: VirtualFile,
        val icon: Icon,
        val isCurrent: Boolean
    ) : SimpleDiffRequestChain.DiffRequestProducerWrapper(request) {
        override fun getName(): String {
            val fileInfo = otherFile.fileInfo ?: return super.getName()
            val rootInfo = fileInfo.rootInfo
            if (rootInfo !is ParadoxRootInfo.MetadataBased) return super.getName()
            return PlsBundle.message("diff.compare.files.popup.name", fileInfo.path, rootInfo.qualifiedName, rootInfo.entryPath)
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
            PlsBundle.message("diff.compare.files.popup.title"),
            chain.requests
        ) {
            init {
                defaultOptionIndex = defaultSelection
            }

            override fun getIconFor(value: DiffRequestProducer) = (value as MyRequestProducer).icon

            override fun getTextFor(value: DiffRequestProducer) = value.name

            //com.intellij.find.actions.ShowUsagesTableCellRenderer.getTableCellRendererComponent L205
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
