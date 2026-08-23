package icu.windea.pls.lang.diff.actions

import com.intellij.diff.DiffDialogHints
import com.intellij.diff.DiffManager
import com.intellij.diff.chains.DiffRequestChain
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import icu.windea.pls.base.settings.ChronicleSettings
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.policies.ParadoxDiffGroupingPolicy

// com.intellij.diff.actions.BaseShowDiffAction

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

    // protected fun hasContent(file: VirtualFile): Boolean {
    //     return !DiffUtil.isFileWithoutContent(file)
    // }

    protected abstract fun getDiffRequestChain(e: AnActionEvent): DiffRequestChain?

    protected fun getDefaultIndex(producers: List<ParadoxDiffRequestProducer>, currentIndex: Int): Int {
        val settings = ChronicleSettings.getInstance().state.diff
        return when (settings.defaultDiffGroup) {
            ParadoxDiffGroupingPolicy.Current -> currentIndex
            ParadoxDiffGroupingPolicy.Vanilla -> getVanillaIndex(producers)
            ParadoxDiffGroupingPolicy.First -> 0
            ParadoxDiffGroupingPolicy.Last -> producers.lastIndex
        }
    }

    private fun getVanillaIndex(producers: List<ParadoxDiffRequestProducer>): Int {
        val i = producers.indexOfFirst { it.otherFile.fileInfo?.rootInfo is ParadoxRootInfo.Game }
        if (i == -1) return 0
        return i
    }
}
