package icu.windea.pls.core.diff.actions

import com.intellij.diff.*
import com.intellij.diff.chains.*
import com.intellij.diff.util.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.vfs.*

//com.intellij.diff.actions.BaseShowDiffAction

abstract class ParadoxShowDiffAction : AnAction() {
    init {
        isEnabledInModalContext = true
    }
    
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
    
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val chain = getDiffRequestChain(e) ?: return
        DiffManager.getInstance().showDiff(project, chain, DiffDialogHints.DEFAULT)
    }
    
    protected fun hasContent(file: VirtualFile): Boolean {
        return !DiffUtil.isFileWithoutContent(file)
    }
    
    protected abstract fun getDiffRequestChain(e: AnActionEvent): DiffRequestChain?
}
