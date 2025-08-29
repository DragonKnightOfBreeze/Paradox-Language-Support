package icu.windea.pls.lang.diff.actions

import com.intellij.diff.DiffDialogHints
import com.intellij.diff.DiffManager
import com.intellij.diff.chains.DiffRequestChain
import com.intellij.diff.chains.DiffRequestProducer
import com.intellij.diff.util.DiffUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.settings.PlsStrategies.DiffGroup as DiffGroupStrategy

//com.intellij.diff.actions.BaseShowDiffAction

abstract class ParadoxShowDiffAction : AnAction() {
    init {
        isEnabledInModalContext = true
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT


    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val chain = getDiffRequestChain(e) ?: return
        DiffManager.getInstance().showDiff(project, chain, DiffDialogHints.DEFAULT)
    }

    protected fun hasContent(file: VirtualFile): Boolean {
        return !DiffUtil.isFileWithoutContent(file)
    }

    protected abstract fun getDiffRequestChain(e: AnActionEvent): DiffRequestChain?

    protected fun getDefaultIndex(producers: List<DiffRequestProducer>, currentIndex: Int): Int {
        val defaultDiffGroup = PlsFacade.getSettings().others.defaultDiffGroup
        return when (defaultDiffGroup) {
            DiffGroupStrategy.VsCopy -> currentIndex
            DiffGroupStrategy.First -> 0
            DiffGroupStrategy.Last -> producers.lastIndex
        }
    }
}
