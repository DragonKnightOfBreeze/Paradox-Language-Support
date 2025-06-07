@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.documentation.actions

import com.intellij.codeInsight.hint.*
import com.intellij.openapi.actionSystem.*
import com.intellij.platform.ide.documentation.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.documentation.*
import icu.windea.pls.lang.util.*

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
