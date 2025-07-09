package icu.windea.pls.tools.actions.localisation

import com.intellij.openapi.actionSystem.*
import com.intellij.psi.*
import icu.windea.pls.localisation.psi.*

/**
 * 用于处理本地化的一类操作。
 *
 * * 应当支持在多个级别批量处理。
 * * 应当在开始处理之前弹出对话框，以确认是否真的要处理。
 */
abstract class ManipulateLocalisationActionBase : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false //TODO 2.0.0-dev
        //e.presentation.isEnabledAndVisible = hasFiles(e)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val files = findFiles(e)
    }

    protected open fun hasFiles(e: AnActionEvent): Boolean {
        return findFiles(e).isNotEmpty()
    }

    protected abstract fun findFiles(e: AnActionEvent): List<PsiFile>

    protected open fun hasElements(e: AnActionEvent, file: PsiFile): Boolean {
        return findElements(e, file).isNotEmpty()
    }

    protected abstract fun findElements(e: AnActionEvent, file: PsiFile): List<ParadoxLocalisationProperty>

    protected abstract fun doInvoke(e: AnActionEvent, files: List<PsiFile>)

    abstract class Default: ManipulateLocalisationActionBase() {
        override fun findFiles(e: AnActionEvent): List<PsiFile> {
            TODO("Not yet implemented") //TODO 2.0.0-dev
        }

        override fun findElements(e: AnActionEvent, file: PsiFile): List<ParadoxLocalisationProperty> {
            TODO("Not yet implemented") //TODO 2.0.0-dev
        }
    }
}
