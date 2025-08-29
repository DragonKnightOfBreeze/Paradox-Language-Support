@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.documentation.actions

import com.intellij.codeInsight.hint.HintManagerImpl
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.platform.ide.documentation.DOCUMENTATION_BROWSER
import icu.windea.pls.lang.ParadoxBaseLanguage
import icu.windea.pls.lang.PlsKeys
import icu.windea.pls.lang.documentation.targetElement
import icu.windea.pls.lang.util.ParadoxLocaleManager

//cn.yiiguxing.plugin.translate.action.ToggleQuickDocTranslationAction
//com.intellij.codeInsight.documentation.actions.CopyQuickDocAction

class ResetQuickDocLocalisationLocaleAction : AnAction(), HintManagerImpl.ActionToIgnore {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    //use local-cached locale (on documentationTarget.element)

    override fun update(e: AnActionEvent) {
        var isVisible = false
        var isEnabled = false
        run {
            val browser = e.getData(DOCUMENTATION_BROWSER)
            val targetElement = browser?.targetPointer?.dereference()?.targetElement
            if (targetElement == null) return@run
            if (targetElement.language !is ParadoxBaseLanguage) return@run
            val locale = ParadoxLocaleManager.getLocaleConfigInDocumentation(targetElement)
            isVisible = true
            isEnabled = locale != null
        }
        e.presentation.isEnabled = isEnabled
        e.presentation.isVisible = isVisible
    }

    override fun actionPerformed(e: AnActionEvent) {
        val browser = e.getData(DOCUMENTATION_BROWSER)
        val targetElement = browser?.targetPointer?.dereference()?.targetElement
        if (targetElement == null) return
        if (targetElement.language !is ParadoxBaseLanguage) return
        val locale = ParadoxLocaleManager.getLocaleConfigInDocumentation(targetElement)
        if (locale == null) return
        targetElement.putUserData(PlsKeys.documentationLocale, null)
        browser.reload()
    }
}
