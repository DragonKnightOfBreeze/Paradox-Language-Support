@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.documentation.actions

import com.intellij.codeInsight.hint.HintManagerImpl.*
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.platform.ide.documentation.DOCUMENTATION_BROWSER
import icu.windea.pls.lang.ParadoxLanguage
import icu.windea.pls.lang.codeInsight.documentation.targetElement
import icu.windea.pls.lang.ui.ParadoxLocaleListPopup
import icu.windea.pls.lang.util.ParadoxLocaleManager

// cn.yiiguxing.plugin.translate.action.ToggleQuickDocTranslationAction
// com.intellij.codeInsight.documentation.actions.CopyQuickDocAction

class ChangeQuickDocLocalisationLocaleAction : AnAction(), ActionToIgnore {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    // use local-cached locale (on documentationTarget.element)

    override fun update(e: AnActionEvent) {
        var isVisible = false
        var isEnabled = false
        run {
            val browser = e.getData(DOCUMENTATION_BROWSER)
            val targetElement = browser?.targetPointer?.dereference()?.targetElement
            if (targetElement == null) return@run
            if (targetElement.language !is ParadoxLanguage) return@run
            isVisible = true
            isEnabled = true
        }
        e.presentation.isEnabled = isEnabled
        e.presentation.isVisible = isVisible
    }

    override fun actionPerformed(e: AnActionEvent) {
        val browser = e.getData(DOCUMENTATION_BROWSER)
        val targetElement = browser?.targetPointer?.dereference()?.targetElement
        if (targetElement == null) return
        if (targetElement.language !is ParadoxLanguage) return
        val allLocales = ParadoxLocaleManager.getLocaleConfigs(withAuto = true)
        val localePopup = ParadoxLocaleListPopup(allLocales)
        localePopup.doFinalStep action@{
            val selected = localePopup.selectedLocale ?: return@action
            targetElement.putUserData(ParadoxLocaleManager.Keys.documentationLocale, selected.id)
            browser.reload()
        }
        JBPopupFactory.getInstance().createListPopup(localePopup).showInBestPositionFor(e.dataContext)
    }
}

