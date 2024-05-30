@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.documentation.actions

import com.intellij.codeInsight.hint.HintManagerImpl.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.ui.popup.*
import com.intellij.platform.ide.documentation.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.documentation.*
import icu.windea.pls.lang.ui.locale.*
import icu.windea.pls.lang.util.*

//cn.yiiguxing.plugin.translate.action.ToggleQuickDocTranslationAction
//com.intellij.codeInsight.documentation.actions.CopyQuickDocAction

class ChangeQuickDocLocalisationLocaleAction : AnAction(), ActionToIgnore {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
    
    override fun update(e: AnActionEvent) {
        var isVisible = false
        var isEnabled = false
        run {
            val browser = e.getData(DOCUMENTATION_BROWSER) ?: return@run
            val target = browser.targetPointer.dereference().castOrNull<LocaleAwareDocumentationTarget>() ?: return@run
            val usedLocale = target.getTargetLocaleConfig()
            if(usedLocale == null) return@run
            isVisible = true
            isEnabled = true
        }
        e.presentation.isEnabled = isEnabled
        e.presentation.isVisible = isVisible
    }
    
    override fun actionPerformed(e: AnActionEvent) {
        val browser = e.getData(DOCUMENTATION_BROWSER) ?: return
        val target = browser.targetPointer.dereference().castOrNull<LocaleAwareDocumentationTarget>() ?: return
        val usedLocale = target.getTargetLocaleConfig() ?: return
        val allLocales = mutableListOf<CwtLocalisationLocaleConfig>()
        allLocales += CwtLocalisationLocaleConfig.AUTO
        allLocales += ParadoxLocaleHandler.getLocaleConfigs()
        val onChosen = { selected: CwtLocalisationLocaleConfig ->
            target.setTargetLocaleConfig(selected)
            browser.reload()
        }
        val localePopup = ParadoxLocaleListPopup(usedLocale, allLocales, onChosen = onChosen)
        JBPopupFactory.getInstance().createListPopup(localePopup).showInBestPositionFor(e.dataContext)
    }
}