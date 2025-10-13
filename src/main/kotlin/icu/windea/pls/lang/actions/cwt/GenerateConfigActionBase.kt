package icu.windea.pls.lang.actions.cwt

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.project.DumbAwareAction

abstract class GenerateConfigActionBase : DumbAwareAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}
