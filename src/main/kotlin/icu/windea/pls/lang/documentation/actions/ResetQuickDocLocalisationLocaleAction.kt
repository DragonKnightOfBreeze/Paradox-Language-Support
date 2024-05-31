@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.documentation.actions

import com.intellij.codeInsight.hint.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.ui.popup.*
import com.intellij.platform.ide.documentation.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.lang.documentation.*
import icu.windea.pls.lang.ui.locale.*
import icu.windea.pls.lang.util.*

//cn.yiiguxing.plugin.translate.action.ToggleQuickDocTranslationAction
//com.intellij.codeInsight.documentation.actions.CopyQuickDocAction

class ResetQuickDocLocalisationLocaleAction: AnAction(), HintManagerImpl.ActionToIgnore {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
    
    //use local-cached locale (on documentationTarget.element)
    
    override fun update(e: AnActionEvent) {
        var isVisible = false
        var isEnabled = false
        run {
            val browser = e.getData(DOCUMENTATION_BROWSER) ?: return@run
            val target = browser.targetPointer.dereference() ?: return@run
            val targetElement = target.targetElement ?: return@run
            val locale = ParadoxLocaleHandler.getLocaleInDocumentation(targetElement)
            if(locale == null) return@run
            isVisible = true
            isEnabled = true
        }
        e.presentation.isEnabled = isEnabled
        e.presentation.isVisible = isVisible
    }
    
    override fun actionPerformed(e: AnActionEvent) {
        val browser = e.getData(DOCUMENTATION_BROWSER) ?: return
        val target = browser.targetPointer.dereference() ?: return
        val targetElement = target.targetElement ?: return
        val locale = ParadoxLocaleHandler.getLocaleInDocumentation(targetElement)
        if(locale == null) return
        targetElement.putUserData(PlsKeys.documentationLocale, null)
        browser.reload()
    }
}