package icu.windea.pls.lang.util

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.matchesPath
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextRenderer
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.paths.ParadoxPath
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 用于处理封装变量。
 */
object ParadoxScriptedVariableManager {
    fun isGlobalFilePath(path: ParadoxPath): Boolean {
        return "common/scripted_variables".matchesPath(path.path)
    }

    fun getNameLocalisation(name: String, contextElement: PsiElement, locale: CwtLocaleConfig): ParadoxLocalisationProperty? {
        val selector = selector(contextElement.project, contextElement).localisation().contextSensitive().preferLocale(locale)
        return ParadoxLocalisationSearch.search(name, selector).find()
    }

    fun getLocalizedName(element: ParadoxScriptScriptedVariable): String? {
        val name = element.name?.orNull() ?: return null
        val nameLocalisation = getNameLocalisation(name, element, ParadoxLocaleManager.getPreferredLocaleConfig()) ?: return null
        return ParadoxLocalisationTextRenderer().render(nameLocalisation).orNull()
    }
}
