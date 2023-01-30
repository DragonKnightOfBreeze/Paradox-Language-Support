package icu.windea.pls.core.diff.actions

import cn.yiiguxing.plugin.translate.util.*
import com.intellij.diff.*
import com.intellij.diff.actions.*
import com.intellij.diff.actions.impl.*
import com.intellij.diff.chains.*
import com.intellij.diff.requests.*
import com.intellij.diff.util.*
import com.intellij.openapi.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.diff.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import java.util.*
import javax.swing.*

/**
 * 将当前文件与包括当前文件的只读副本在内的相同路径的文件进行DIFF。
 *
 * TODO 按照覆盖顺序进行排序。
 */
@Suppress("ComponentNotRegistered")
class ParadoxCompareFilesAction : ParadoxShowDiffAction() {
    override fun isAvailable(e: AnActionEvent): Boolean {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
            ?: e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.singleOrNull()
            ?: return false
        if(file.isDirectory) return false
        val fileInfo = file.fileInfo ?: return false
        //忽略游戏或模组根目录下的文件
        return fileInfo.path.length > 1
    }
    
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
    
    override fun getDiffRequestChain(e: AnActionEvent): DiffRequestChain? {
        val project = e.project ?: return null
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        val path = fileInfo.path.path
        val selector = fileSelector().gameType(gameType)
        val files = Collections.synchronizedList(mutableListOf<VirtualFile>())
        ProgressManager.getInstance().runProcessWithProgressSynchronously({
            //need read action here
            com.intellij.openapi.application.runReadAction {
                val result = ParadoxFilePathSearch.search(path, project, selector = selector).findAll()
                files.addAll(result)
            }
        }, PlsBundle.message("diff.compare.files.collect.title"), true, project)
        if(files.size <= 1) return null
        
        val contentFactory = DiffContentFactory.getInstance()
        
        val windowTitle = getWindowsTitle(file) ?: return null
        val contentTitle = getContentTitle(file) ?: return null
        val content = contentFactory.createDocument(project, file) ?: return null
        
        val producers = files.mapNotNull { otherFile ->
            val otherContentTitle = when {
                file == otherFile -> getContentTitle(otherFile, true)
                else -> getContentTitle(otherFile)
            } ?: return@mapNotNull null
            val otherContent = when {
                file == otherFile -> {
                    val document = EditorFactory.getInstance().createDocument(content.document.text)
                    contentFactory.create(project, document, content.highlightFile).apply {
                        putUserData(DiffUserDataKeys.FORCE_READ_ONLY, true)
                    }
                }
                else -> contentFactory.createDocument(project, otherFile) ?: return@mapNotNull null
            }
            val request = SimpleDiffRequest(windowTitle, content, otherContent, contentTitle, otherContentTitle)
            FileRequestProducer(request, otherFile)
        }
        val chain = FileDiffRequestChain(producers)
        
        //如果打开了编辑器，左窗口定位到当前光标位置
        val editor = e.editor
        if(editor != null) {
            val currentLine = editor.caretModel.logicalPosition.line
            chain.putUserData(DiffUserDataKeys.SCROLL_TO_LINE, Pair.create(Side.RIGHT, currentLine))
        }
        return chain
    }
    
    private fun getWindowsTitle(file: VirtualFile): String? {
        val fileInfo = file.fileInfo ?: return null
        return PlsBundle.message("diff.compare.files.dialog.title", fileInfo.path, fileInfo.rootPath)
    }
    
    private fun getContentTitle(file: VirtualFile, original: Boolean = false): String? {
        val fileInfo = file.fileInfo ?: return null
        return when {
            original -> PlsBundle.message("diff.compare.files.originalContent.title", fileInfo.path, fileInfo.rootPath)
            else -> PlsBundle.message("diff.compare.files.content.title", fileInfo.path, fileInfo.rootPath)
        }
    }
    
    class FileDiffRequestChain(
        producers: List<DiffRequestProducer>
    ) : UserDataHolderBase(), DiffRequestSelectionChain, GoToChangePopupBuilder.Chain {
        private val listSelection = ListSelection.createAt(producers, 0)
        
        override fun getListSelection() = listSelection
        
        override fun createGoToChangeAction(onSelected: Consumer<in Int>, defaultSelection: Int): AnAction {
            return FileGotoChangePopupAction(this, onSelected, defaultSelection)
        }
    }
    
    class FileRequestProducer(
        request: DiffRequest,
        val otherFile: VirtualFile
    ) : SimpleDiffRequestChain.DiffRequestProducerWrapper(request) {
        override fun getName(): String {
            val fileInfo = otherFile.fileInfo ?: return super.getName()
            return PlsBundle.message("diff.compare.files.popup.name", fileInfo.path, fileInfo.rootPath)
        }
    }
    
    class FileGotoChangePopupAction(
        val chain: DiffRequestChain,
        val onSelected: Consumer<in Int>,
        val defaultSelection: Int
    ) : GoToChangePopupBuilder.BaseGoToChangePopupAction() {
        override fun canNavigate(): Boolean {
            return chain.requests.size > 1
        }
        
        override fun createPopup(e: AnActionEvent): JBPopup {
            return JBPopupFactory.getInstance().createListPopup(Popup())
        }
        
        private inner class Popup : BaseListPopupStep<DiffRequestProducer>(PlsBundle.message("diff.compare.files.popup.title"), chain.requests) {
            init {
                defaultOptionIndex = defaultSelection
            }
            
            override fun getIconFor(value: DiffRequestProducer) = (value as FileRequestProducer).otherFile.fileType.icon
    
            override fun getTextFor(value: DiffRequestProducer) = value.name
            
            override fun isSpeedSearchEnabled() = true
            
            override fun onChosen(selectedValue: DiffRequestProducer, finalChoice: Boolean) = doFinalStep {
                onSelected.consume(chain.requests.indexOf(selectedValue))
            }
        }
    }
}