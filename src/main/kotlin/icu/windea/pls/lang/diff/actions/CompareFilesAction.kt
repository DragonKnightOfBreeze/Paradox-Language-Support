package icu.windea.pls.lang.diff.actions

import com.intellij.diff.*
import com.intellij.diff.actions.impl.*
import com.intellij.diff.chains.*
import com.intellij.diff.contents.*
import com.intellij.diff.requests.*
import com.intellij.diff.util.*
import com.intellij.notification.*
import com.intellij.openapi.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.core.diff.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.actions.*
import icu.windea.pls.lang.diff.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import java.awt.*
import java.util.*
import javax.swing.*

/**
 * 将当前文件与包括当前文件的只读副本在内的相同路径的文件进行DIFF。如果是本地化文件的话也忽略路径中的语言区域。
 *
 * * 可以用于比较二进制文件。（如DDS图片）
 * * TODO 按照覆盖顺序进行排序。
 */
@Suppress("ComponentNotRegistered", "DEPRECATION")
class CompareFilesAction : ParadoxShowDiffAction() {
    private fun findFile(e: AnActionEvent): VirtualFile? {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.singleOrNull()
            ?: e.getData(CommonDataKeys.VIRTUAL_FILE)
            ?: return null
        if(file.isDirectory) return null
        val fileInfo = file.fileInfo ?: return null
        if(fileInfo.pathToEntry.length <= 1) return null //忽略直接位于游戏或模组入口目录下的文件
        //val gameType = fileInfo.rootInfo.gameType
        //val path = fileInfo.path.path
        return file
    }
    
    override fun update(e: AnActionEvent) {
        //基于插件设置判断是否需要显示在编辑器悬浮工具栏中
        if(e.place == ActionPlaces.CONTEXT_TOOLBAR && !getSettings().others.showEditorContextToolbar) {
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
        ProgressManager.getInstance().runProcessWithProgressSynchronously({
            runReadAction {
                val selector = fileSelector(project, file)
                val result = ParadoxFilePathSearch.search(path, null, selector, ignoreLocale = true).findAll()
                virtualFiles.addAll(result)
            }
        }, PlsBundle.message("diff.compare.files.collect.title"), true, project)
        if(virtualFiles.size <= 1) {
            NotificationGroupManager.getInstance().getNotificationGroup("pls").createNotification(
                PlsBundle.message("diff.compare.files.content.title.info.1"),
                NotificationType.INFORMATION
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
        if(binary) content.putUserData(DiffUserDataKeys.FORCE_READ_ONLY, true)
        
        var index = 0
        var currentIndex = 0
        val producers = runReadAction {
            virtualFiles.mapNotNull { otherFile ->
                if(file.fileType != otherFile.fileType) return@mapNotNull null
                val isSameFile = file == otherFile
                val otherContentTitle = when {
                    isSameFile -> getContentTitle(otherFile, true)
                    else -> getContentTitle(otherFile)
                } ?: return@mapNotNull null
                var isCurrent = false
                var readonly = false
                val otherContent = when {
                    binary -> {
                        if(isSameFile) isCurrent = true
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
                if(isCurrent) currentIndex = index
                if(readonly) otherContent.putUserData(DiffUserDataKeys.FORCE_READ_ONLY, true)
                index++
                val icon = otherFile.fileType.icon
                val request = SimpleDiffRequest(windowTitle, content, otherContent, contentTitle, otherContentTitle)
                //窗口定位到当前光标位置
                if(!binary && editor != null) {
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
        return PlsBundle.message("diff.compare.files.dialog.title", fileInfo.path, fileInfo.rootInfo.qualifiedName, fileInfo.rootInfo.gameRootPath)
    }
    
    private fun getContentTitle(file: VirtualFile, original: Boolean = false): String? {
        val fileInfo = file.fileInfo ?: return null
        return when {
            original -> PlsBundle.message("diff.compare.files.originalContent.title", fileInfo.path, fileInfo.rootInfo.qualifiedName, fileInfo.rootInfo.gameRootPath)
            else -> PlsBundle.message("diff.compare.files.content.title", fileInfo.path, fileInfo.rootInfo.qualifiedName, fileInfo.rootInfo.gameRootPath)
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
            return PlsBundle.message("diff.compare.files.popup.name", fileInfo.path, fileInfo.rootInfo.qualifiedName, fileInfo.rootInfo.gameRootPath)
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
            override fun getBackgroundFor(value: DiffRequestProducer) =
                if((value as MyRequestProducer).isCurrent) Color(0x808080) else null
            
            override fun isSpeedSearchEnabled() = true
            
            override fun onChosen(selectedValue: DiffRequestProducer, finalChoice: Boolean) = doFinalStep {
                val selectedIndex = chain.requests.indexOf(selectedValue)
                onSelected.consume(selectedIndex)
            }
        }
    }
}
