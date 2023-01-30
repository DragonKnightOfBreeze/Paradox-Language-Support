package icu.windea.pls.core.diff.actions

import com.intellij.diff.*
import com.intellij.diff.actions.BlankDiffWindowUtil.createBlankDiffRequestChain
import com.intellij.diff.chains.*
import com.intellij.diff.util.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.diff.*
import com.intellij.openapi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*

@Suppress("ComponentNotRegistered")
class ParadoxCompareFilesAction: ParadoxShowDiffAction() {
    override fun isAvailable(e: AnActionEvent): Boolean {
        val project = e.project ?: return false
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return false
        if(file.isDirectory) return false
        val fileInfo = file.fileInfo ?: return false
        val gameType = fileInfo.rootInfo.gameType
        val path = fileInfo.path.path
        val selector = fileSelector().gameType(gameType)
        val hasOverridden = ParadoxFilePathSearch.search(path, project, selector = selector).findFirst() != null
        return hasOverridden
    }
    
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
    
    override fun getDiffRequestChain(e: AnActionEvent): DiffRequestChain? {
        val project = e.project ?: return null
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        val path = fileInfo.path.path
        
        val contentFactory = DiffContentFactory.getInstance()
        val requestFactory = DiffRequestFactory.getInstance()
        val content1 = contentFactory.createDocument(project, file) ?: return null
        val content2 = contentFactory.createDocument(project, file) ?: return null
        
        val chain = createBlankDiffRequestChain(content1, content2, null)
        chain.windowTitle = PlsBundle.message("diff.compare.files.dialog.title", fileInfo.path, fileInfo.rootPath)
        chain.title1 = PlsBundle.message("diff.compare.files.content.title", fileInfo.path, fileInfo.rootPath)
        chain.title2 = PlsBundle.message("diff.compare.files.content.title", fileInfo.path, fileInfo.rootPath)
        
        val editor = e.editor
        if(editor != null) {
            val currentLine = editor.caretModel.logicalPosition.line
            chain.putRequestUserData(DiffUserDataKeys.SCROLL_TO_LINE, Pair.create(Side.RIGHT, currentLine))
        }
        return chain
    }
}